// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.DataMaps;
import advRocketry.multiblock.PlacedMultiblock;
import advRocketry.space.GalaxyData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ProcessingBlockEntity extends MultiblockMachine implements MenuProvider {
  private ArmorSealingRecipe activeSealing;
  private ItemStack sealingInput = ItemStack.EMPTY;
  private ResourceLocation recipeId;
  private ProcessingRecipe dynamicRecipe;
  private ProcessingRecipe.Rolled pendingOutputs = ProcessingRecipe.Rolled.EMPTY;
  private int progress;
  private int duration = 1;
  private boolean running;
  private boolean renderChanged;
  private ItemStack syncedInput = ItemStack.EMPTY;
  private ItemStack syncedOutput = ItemStack.EMPTY;

  public ProcessingBlockEntity(BlockPos pos, BlockState state) {
    super(ProcessingRegistry.BLOCK_ENTITY.get(), pos, state);
  }

  public MachineType machine() {
    return ((ProcessingBlock) getBlockState().getBlock()).machine();
  }

  public boolean running() {
    return running;
  }

  public int progressTicks() {
    return progress;
  }

  public int durationTicks() {
    return duration;
  }

  public ItemStack displayedOutput() {
    if (level != null && level.isClientSide) return syncedOutput;
    return pendingOutputs.items().isEmpty()
        ? ItemStack.EMPTY
        : pendingOutputs.items().getFirst().copy();
  }

  public ItemStack displayedInput() {
    if (level != null && level.isClientSide) return syncedInput;
    ProcessingRecipe recipe = currentRecipe();
    if (recipe == null) return ItemStack.EMPTY;
    List<SizedIngredient> shown = new ArrayList<>(recipe.inputs());
    shown.addAll(recipe.catalysts());
    for (var input : shown)
      for (ItemStack stack : input.getItems()) if (!stack.isEmpty()) return stack.copy();
    return ItemStack.EMPTY;
  }

  public float progress(float partialTick) {
    return Math.min(1, (progress + (running ? partialTick : 0)) / Math.max(1f, duration));
  }

  @Override
  protected String multiblockId() {
    return machine().id();
  }

  @Override
  public boolean hideParts() {
    return machine() != MachineType.ELECTRIC_ARC_FURNACE;
  }

  @Override
  public Component getDisplayName() {
    return getBlockState().getBlock().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new MachineMenu(id, inventory, worldPosition, menuPorts());
  }

  public void tick() {
    if (level.isClientSide) {
      if (running && progress < duration) progress++;
      ProcessingRegistry.clientMachineTick.accept(this);
      return;
    }
    boolean wasRunning = running;
    running = false;
    if (level.getGameTime() % 20 == 0 && structureChunksLoaded()) scanStructure();
    if (getBlockState().getValue(ProcessingBlock.FORMED) && structureChunksLoaded()) process();
    if (wasRunning != running || renderChanged) {
      renderChanged = false;
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
  }

  private ProcessingRecipe currentRecipe() {
    if (recipeId == null)
      return AdvancedRocketryConfig.makeMaterialsForOtherMods() ? dynamicRecipe : null;
    return level
        .getRecipeManager()
        .byKey(recipeId)
        .map(RecipeHolder::value)
        .filter(recipe -> recipe instanceof ProcessingRecipe)
        .map(recipe -> (ProcessingRecipe) recipe)
        .filter(recipe -> recipe.machine() == machine() && ProcessingRecipe.enabled(recipeId))
        .orElse(null);
  }

  private void process() {
    double maximumGravity = AdvancedRocketryConfig.crystallizerMaximumGravity();
    if (machine() == MachineType.CRYSTALLIZER
        && maximumGravity > 0
        && level instanceof ServerLevel serverLevel
        && GalaxyData.get(serverLevel.getServer()).planet(serverLevel).gravity >= maximumGravity)
      return;
    if (machine() == MachineType.CHEMICAL_REACTOR
        && AdvancedRocketryConfig.enableOxygen()
        && recipeId == null
        && processSealing()) return;
    ProcessingRecipe recipe = currentRecipe();
    if (recipe == null && recipeId != null) reset();
    if (recipe == null && dynamicRecipe != null) reset();
    if (recipe == null) {
      if (level.getGameTime() % 20 != 0) return;
      ProcessingInput input = input();
      for (RecipeHolder<ProcessingRecipe> holder :
          level.getRecipeManager().getAllRecipesFor(ProcessingRegistry.RECIPE_TYPE.get())) {
        if (ProcessingRecipe.enabled(holder.id()) && holder.value().matches(input, level)) {
          recipe = holder.value();
          recipeId = holder.id();
          pendingOutputs = recipe.roll(level.random);
          duration = modifiedDuration(recipe);
          renderChanged = true;
          setChanged();
          break;
        }
      }
      if (recipe == null && AdvancedRocketryConfig.makeMaterialsForOtherMods()) {
        for (var port : getItemInTiles()) {
          for (int slot = 0; slot < port.inventory.getSlots(); slot++) {
            ProcessingRecipe candidate =
                MaterialRecipes.candidate(machine(), port.inventory.getStackInSlot(slot));
            if (candidate == null || !candidate.matches(input(), level)) continue;
            dynamicRecipe = candidate;
            recipe = candidate;
            pendingOutputs = candidate.roll(level.random);
            duration = modifiedDuration(candidate);
            renderChanged = true;
            setChanged();
            break;
          }
          if (recipe != null) break;
        }
      }
    }
    if (recipe == null) return;
    // Missing ingredients pause the selected batch instead of discarding its chance roll.
    if (!ProcessingRecipe.available(
        recipe.requiredItems(), recipe.fluidInputs(), itemContents(), fluidContents())) return;
    if (!pendingOutputs.resolvable()) {
      reset();
      return;
    }
    updateDuration(recipe);
    // Persist the selected outputs, so pausing or reloading cannot reroll a centrifuge result.
    if (!canFitOutputs() || getTotalEnergyStored(getEnergyInputTiles()) < recipe.energy()) return;
    consumeEnergy(recipe.energy(), getEnergyInputTiles());
    running = true;
    progress++;
    if (progress >= duration) {
      consume(recipe.inputs(), recipe.fluidInputs(), recipe.catalysts());
      produceOutputs();
      reset();
    }
    setChanged();
  }

  private void updateDuration(ProcessingRecipe recipe) {
    int modified = modifiedDuration(recipe);
    if (modified == duration) return;
    duration = modified;
    renderChanged = true;
  }

  /** The original chemical reactor seals any ordinary armor, preserving its components. */
  private boolean processSealing() {
    ItemStackHandler armorHandler = null;
    int armorSlot = -1;
    ItemStack armor = ItemStack.EMPTY;
    ArmorSealingRecipe recipe = null;
    List<SizedIngredient> materials = List.of();
    for (var port : getItemInTiles()) {
      for (int slot = 0; slot < port.inventory.getSlots(); slot++) {
        ItemStack candidate = port.inventory.getStackInSlot(slot);
        ArmorSealingRecipe found = ArmorSealingRecipe.find(level, candidate);
        List<ItemStack> available = new ArrayList<>();
        for (ItemStack stack : itemContents()) {
          ItemStack copy = stack.copy();
          if (stack == candidate) copy.shrink(1);
          available.add(copy);
        }
        if (found != null
            && ProcessingRecipe.available(found.materials(), List.of(), available, List.of())) {
          armorHandler = port.inventory;
          armorSlot = slot;
          armor = candidate;
          materials = found.materials();
          recipe = found;
          break;
        }
      }
      if (armorHandler != null) break;
    }
    if (armorHandler == null) {
      activeSealing = null;
      sealingInput = ItemStack.EMPTY;
      if (progress > 0) {
        progress = 0;
        renderChanged = true;
        setChanged();
      }
      return false;
    }
    if (activeSealing != recipe || !ItemStack.isSameItemSameComponents(sealingInput, armor)) {
      progress = 0;
      activeSealing = recipe;
      sealingInput = armor.copyWithCount(1);
    }
    ItemStack sealed = recipe.assemble(new SingleRecipeInput(armor), level.registryAccess());
    updateDuration(
        new ProcessingRecipe(
            MachineType.CHEMICAL_REACTOR,
            recipe.ticks(),
            recipe.energy(),
            List.of(),
            List.of(),
            List.of()));
    if (!OutputSimulation.canInsertAllItems(
            getItemOutTiles().stream().map(port -> port.inventory).toList(), List.of(sealed))
        || getTotalEnergyStored(getEnergyInputTiles()) < recipe.energy()) return true;
    consumeEnergy(recipe.energy(), getEnergyInputTiles());
    running = true;
    progress++;
    if (progress >= duration) {
      armorHandler.extractItem(armorSlot, 1, false);
      consume(materials, List.of());
      for (var port : getItemOutTiles())
        for (int slot = 0; slot < port.inventory.getSlots(); slot++)
          sealed = port.inventory.insertItem(slot, sealed, false);
      progress = 0;
      renderChanged = true;
    }
    setChanged();
    return true;
  }

  int modifiedDuration(ProcessingRecipe recipe) {
    double multiplier = 1;
    PlacedMultiblock structure = structure();
    if (structure != null) {
      for (BlockPos pos : structure.positions('M'))
        if (level.getBlockState(pos).getBlock() instanceof MotorBlock motor)
          multiplier *= motor.timeMultiplier();
      for (BlockPos pos : structure.positions('K')) {
        Block actual = level.getBlockState(pos).getBlock();
        var modifier = actual.builtInRegistryHolder().getData(DataMaps.PROCESSING_TIME);
        if (modifier != null) multiplier *= modifier;
      }
    }
    return Math.max(1, (int) (recipe.ticks() * multiplier));
  }

  private List<ItemStack> itemContents() {
    List<ItemStack> items = new ArrayList<>();
    for (var port : getItemInTiles())
      for (int slot = 0; slot < port.inventory.getSlots(); slot++)
        items.add(port.inventory.getStackInSlot(slot));
    return items;
  }

  private List<FluidStack> fluidContents() {
    return getFluidInTiles().stream().map(port -> port.myTank.getFluid()).toList();
  }

  private ProcessingInput input() {
    return new ProcessingInput(machine(), itemContents(), fluidContents());
  }

  private void consume(List<SizedIngredient> items, List<SizedFluidIngredient> fluids) {
    consume(items, fluids, List.of());
  }

  private void consume(
      List<SizedIngredient> items,
      List<SizedFluidIngredient> fluids,
      List<SizedIngredient> catalysts) {
    // Match the same catalyst-first reservation used by ProcessingRecipe.available.
    List<ItemStack> available = itemContents().stream().map(ItemStack::copy).toList();
    for (var catalyst : catalysts) {
      int remaining = catalyst.count();
      for (ItemStack stack : available) {
        if (remaining == 0) break;
        if (stack.isEmpty() || !catalyst.ingredient().test(stack)) continue;
        int reserved = Math.min(remaining, stack.getCount());
        stack.shrink(reserved);
        remaining -= reserved;
      }
    }
    for (var ingredient : items) {
      int remaining = ingredient.count();
      int index = 0;
      for (var port : getItemInTiles())
        for (int slot = 0; slot < port.inventory.getSlots(); slot++) {
          ItemStack stack = available.get(index++);
          if (remaining == 0 || stack.isEmpty() || !ingredient.ingredient().test(stack)) continue;
          int consumed =
              port.inventory
                  .extractItem(slot, Math.min(remaining, stack.getCount()), false)
                  .getCount();
          stack.shrink(consumed);
          remaining -= consumed;
        }
    }
    for (var ingredient : fluids) {
      int remaining = ingredient.amount();
      for (var port : getFluidInTiles()) {
        FluidStack stack = port.myTank.getFluid();
        if (remaining == 0 || stack.isEmpty() || !ingredient.ingredient().test(stack)) continue;
        remaining -= port.myTank.drain(remaining, IFluidHandler.FluidAction.EXECUTE).getAmount();
      }
    }
  }

  private boolean canFitOutputs() {
    if (!pendingOutputs.resolvable()) return false;
    return OutputSimulation.canInsertAllItems(
            getItemOutTiles().stream().map(port -> port.inventory).toList(), pendingOutputs.items())
        && OutputSimulation.canInsertAllFluids(
            getFluidOutTiles().stream().map(port -> port.myTank).toList(), pendingOutputs.fluids());
  }

  private void produceOutputs() {
    for (FluidStack output : pendingOutputs.fluids()) {
      FluidStack remaining = output.copy();
      for (var port : getFluidOutTiles())
        remaining.shrink(port.myTank.fill(remaining, IFluidHandler.FluidAction.EXECUTE));
    }
    for (ItemStack output : pendingOutputs.items()) {
      ItemStack remaining = output.copy();
      for (var port : getItemOutTiles())
        for (int slot = 0; slot < port.inventory.getSlots(); slot++)
          remaining = port.inventory.insertItem(slot, remaining, false);
    }
  }

  private void reset() {
    recipeId = null;
    dynamicRecipe = null;
    pendingOutputs = ProcessingRecipe.Rolled.EMPTY;
    progress = 0;
    running = false;
    renderChanged = true;
    setChanged();
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    if (recipeId != null) tag.putString("processing_recipe", recipeId.toString());
    var ops = registries.createSerializationContext(NbtOps.INSTANCE);
    if (recipeId != null || dynamicRecipe != null)
      tag.put(
          "processing_outputs",
          ProcessingRecipe.Rolled.CODEC.encodeStart(ops, pendingOutputs).getOrThrow());
    if (dynamicRecipe != null)
      tag.put(
          "dynamic_processing_recipe",
          ProcessingRecipe.CODEC.encodeStart(ops, dynamicRecipe).getOrThrow());
    tag.putInt("processing_progress", progress);
    tag.putInt("processing_duration", duration);
    tag.putBoolean("processing_running", running);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    recipeId =
        tag.contains("processing_recipe")
            ? ResourceLocation.tryParse(tag.getString("processing_recipe"))
            : null;
    var ops = registries.createSerializationContext(NbtOps.INSTANCE);
    dynamicRecipe =
        tag.contains("dynamic_processing_recipe")
            ? ProcessingRecipe.CODEC
                .parse(ops, tag.get("dynamic_processing_recipe"))
                .result()
                .orElse(null)
            : null;
    progress = Math.max(0, tag.getInt("processing_progress"));
    duration = Math.max(1, tag.getInt("processing_duration"));
    running = tag.getBoolean("processing_running");
    pendingOutputs =
        tag.contains("processing_outputs")
            ? ProcessingRecipe.Rolled.CODEC
                .parse(ops, tag.get("processing_outputs"))
                .result()
                .orElse(ProcessingRecipe.Rolled.EMPTY)
            : ProcessingRecipe.Rolled.EMPTY;
    syncedInput = ItemStack.parseOptional(registries, tag.getCompound("display_input"));
    syncedOutput = ItemStack.parseOptional(registries, tag.getCompound("display_output"));
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("processing_progress", progress);
    tag.putInt("processing_duration", duration);
    tag.putBoolean("processing_running", running);
    ItemStack input = displayedInput();
    if (!input.isEmpty()) tag.put("display_input", input.save(registries));
    ItemStack output = displayedOutput();
    if (!output.isEmpty()) tag.put("display_output", output.save(registries));
    return tag;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
}
