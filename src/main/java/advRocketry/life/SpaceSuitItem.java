// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.life;

import advRocketry.processing.ProcessingFluids;
import advRocketry.processing.ProcessingRegistry;
import advRocketry.util.Texts;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Original modular suit contents stored in native, synchronized item components. */
public final class SpaceSuitItem extends ArmorItem {
  public final int modules;

  public SpaceSuitItem(Type type, int modules, Properties properties) {
    super(LifeSupportRegistry.SUIT_MATERIAL, type, properties.stacksTo(1));
    this.modules = modules;
  }

  public ItemStackHandler inventory(ItemStack armor) {
    NonNullList<ItemStack> contents = NonNullList.withSize(modules, ItemStack.EMPTY);
    armor.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(contents);
    return new ItemStackHandler(contents) {
      @Override
      protected void onContentsChanged(int slot) {
        if (!armor.isEmpty())
          armor.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
      }

      @Override
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return armor.isEmpty() ? stack : super.insertItem(slot, stack, simulate);
      }

      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return armor.isEmpty() ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
      }

      @Override
      public int getSlotLimit(int slot) {
        return 1;
      }

      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        if (armor.isEmpty()) return false;
        if (getType() == Type.CHESTPLATE && slot < 2) {
          if (!(stack.getItem() instanceof PressureTankItem tank)) return false;
          var fluid = tank.handler(stack).getFluid();
          return fluid.isEmpty() || fluid.is(ProcessingFluids.oxygen());
        }
        if (stack.getItem() instanceof PressureTankItem) return getType() == Type.CHESTPLATE;
        return switch (getType()) {
          case HELMET ->
              stack.getItem() instanceof AtmosphereAnalyzerItem
                  || stack.is(ProcessingRegistry.PART_ITEMS.get("night_vision_upgrade").get())
                  || stack.is(ProcessingRegistry.PART_ITEMS.get("beacon_finder").get())
                  || stack.is(ProcessingRegistry.PART_ITEMS.get("atmosphere_upgrade").get())
                  || stack.is(ProcessingRegistry.PART_ITEMS.get("hover_upgrade").get())
                  || stack.is(ProcessingRegistry.PART_ITEMS.get("flight_speed_upgrade").get());
          case CHESTPLATE -> stack.is(ProcessingRegistry.PART_ITEMS.get("jetpack").get());
          case LEGGINGS -> stack.is(ProcessingRegistry.PART_ITEMS.get("legs_upgrade").get());
          case BOOTS -> stack.is(ProcessingRegistry.PART_ITEMS.get("padded_boots_upgrade").get());
          default -> false;
        };
      }
    };
  }

  public int oxygenCapacity(ItemStack armor) {
    var inventory = inventory(armor);
    long capacity = 0;
    for (int slot = 0; slot < inventory.getSlots(); slot++) {
      ItemStack stack = inventory.getStackInSlot(slot);
      if (!(stack.getItem() instanceof PressureTankItem tank)) continue;
      var handler = tank.handler(stack);
      if (handler.getFluid().isEmpty() || handler.getFluid().is(ProcessingFluids.oxygen()))
        capacity += handler.getTankCapacity(0);
    }
    return (int) Math.min(Integer.MAX_VALUE, capacity);
  }

  public int oxygen(ItemStack armor) {
    var inventory = inventory(armor);
    int oxygen = 0;
    for (int slot = 0; slot < inventory.getSlots(); slot++) {
      var stack = inventory.getStackInSlot(slot);
      if (stack.getItem() instanceof PressureTankItem tank) {
        var fluid = tank.handler(stack).getFluid();
        if (fluid.is(ProcessingFluids.oxygen())) oxygen += fluid.getAmount();
      }
    }
    return oxygen;
  }

  private static Iterable<ItemStack> installed(ItemStack armor) {
    return armor.getItem() instanceof SpaceSuitItem
        ? armor.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems()
        : List.of();
  }

  public static ItemStack findModule(ItemStack armor, Predicate<ItemStack> filter) {
    for (ItemStack module : installed(armor)) if (filter.test(module)) return module;
    return ItemStack.EMPTY;
  }

  public int countModule(ItemStack armor, String itemId) {
    Item item = ProcessingRegistry.PART_ITEMS.get(itemId).get();
    int count = 0;
    for (ItemStack module : installed(armor)) if (module.is(item)) count++;
    return count;
  }

  public void replaceModule(ItemStack armor, Predicate<ItemStack> filter, ItemStack replacement) {
    var modules = inventory(armor);
    for (int slot = 0; slot < modules.getSlots(); slot++)
      if (filter.test(modules.getStackInSlot(slot))) {
        modules.setStackInSlot(slot, replacement);
        return;
      }
  }

  public int transferOxygen(ItemStack armor, FluidStack oxygen, boolean fill) {
    var inventory = inventory(armor);
    int remaining = oxygen.getAmount();
    for (int slot = 0; slot < inventory.getSlots() && remaining > 0; slot++) {
      var stack = inventory.getStackInSlot(slot);
      if (!(stack.getItem() instanceof PressureTankItem tank)) continue;
      var handler = tank.handler(stack.copy());
      int moved =
          fill
              ? handler.fill(oxygen.copyWithAmount(remaining), IFluidHandler.FluidAction.EXECUTE)
              : handler
                  .drain(oxygen.copyWithAmount(remaining), IFluidHandler.FluidAction.EXECUTE)
                  .getAmount();
      if (moved > 0 && inventory.isItemValid(slot, handler.getContainer())) {
        remaining -= moved;
        inventory.setStackInSlot(slot, handler.getContainer());
      }
    }
    return oxygen.getAmount() - remaining;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!player.isShiftKeyDown()) return super.use(level, player, hand);
    ItemStack armor = player.getItemInHand(hand);
    if (player instanceof ServerPlayer serverPlayer) {
      int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
      serverPlayer.openMenu(
          new SimpleMenuProvider(
              (id, inventory, owner) -> new SuitMenu(id, inventory, slot), getName(armor)),
          buffer -> buffer.writeVarInt(slot));
    }
    return InteractionResultHolder.sidedSuccess(armor, level.isClientSide);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(
        Component.translatable("message.adv_rocketry.space_suit.sneak_and_use_to_configure_suit"));
    if (getType() == Type.CHESTPLATE)
      tooltip.add(Texts.translate("message.adv_rocketry.space_suit.oxygen_mb", oxygen(stack)));
  }
}
