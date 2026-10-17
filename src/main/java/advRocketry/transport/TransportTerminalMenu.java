package advRocketry.transport;

import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** A terminal owns only its crafting grid; displayed stock is never an inventory. */
public final class TransportTerminalMenu extends ModMenu {
  private static final int PAGE_SIZE = 36, SCAN_BUDGET = 256, MAX_ENTRIES = 4096;
  private final BlockPos pos;
  private final TransportBlockEntity pipe;
  private final Player owner;
  private final ItemStackHandler display = new ItemStackHandler(PAGE_SIZE);
  private final TransientCraftingContainer crafting = new TransientCraftingContainer(this, 3, 3);
  private final ResultContainer result = new ResultContainer();
  private final List<ItemStack> stock = new ArrayList<>(), building = new ArrayList<>();

  private record StackKey(ItemStack stack) {
    @Override
    public int hashCode() {
      return ItemStack.hashItemAndComponents(stack);
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof StackKey key && ItemStack.isSameItemSameComponents(stack, key.stack);
    }
  }

  private final Map<StackKey, ItemStack> aggregate = new HashMap<>();
  private List<TransportNetworks.StorageAccess> scan = List.of();
  private int endpoint, slot, page;
  private long scannedTick = Long.MIN_VALUE, nextScan;
  private boolean scanning, truncated;
  private int transactionWork;
  private long transactionTick = Long.MIN_VALUE;
  private String search = "";
  private final SplitIntData data;

  public TransportTerminalMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, buffer.readBlockPos(), null);
  }

  public TransportTerminalMenu(int id, Inventory inventory, TransportBlockEntity pipe) {
    this(id, inventory, pipe.getBlockPos(), pipe);
  }

  private TransportTerminalMenu(
      int id, Inventory inventory, BlockPos pos, TransportBlockEntity pipe) {
    super(TransportRegistry.TERMINAL.get(), id);
    this.pos = pos;
    this.pipe = pipe;
    owner = inventory.player;
    for (int i = 0; i < PAGE_SIZE; i++)
      addSlot(
          new SlotItemHandler(display, i, 8 + i % 9 * 18, 42 + i / 9 * 18) {
            @Override
            public boolean mayPickup(Player player) {
              return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
              return false;
            }
          });
    addSlot(new ResultSlot(owner, crafting, result, 0, 264, 64));
    for (int i = 0; i < 9; i++) addSlot(new Slot(crafting, i, 190 + i % 3 * 18, 44 + i / 3 * 18));
    addPlayerInventory(inventory, 69, 154);
    data =
        pipe == null
            ? SplitIntData.client(3)
            : SplitIntData.server(
                3,
                i ->
                    switch (i) {
                      case 0 -> page;
                      case 1 -> Math.max(1, (filtered().size() + PAGE_SIZE - 1) / PAGE_SIZE);
                      default -> truncated ? 2 : scanning ? 1 : 0;
                    });
    addDataSlots(data);
  }

  public int value(int i) {
    return data.value(i);
  }

  @Override
  public boolean stillValid(Player player) {
    return player.level().hasChunkAt(pos)
        && player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64
        && player.level().getBlockEntity(pos) instanceof TransportBlockEntity current
        && current.kind() == TransportRegistry.Kind.ITEM
        && (pipe == null || current == pipe);
  }

  private boolean editable(Player player) {
    return pipe != null && stillValid(player) && player.mayBuild() && !player.isSpectator();
  }

  @Override
  public void broadcastChanges() {
    if (pipe != null && stillValid(owner) && scannedTick != owner.level().getGameTime()) {
      scannedTick = owner.level().getGameTime();
      scanStock();
    }
    super.broadcastChanges();
  }

  private void scanStock() {
    if (!scanning) {
      if (scannedTick < nextScan) return;
      scan = TransportNetworks.storage(pipe, false);
      building.clear();
      aggregate.clear();
      endpoint = slot = 0;
      scanning = true;
      truncated = false;
    }
    int work = SCAN_BUDGET;
    while (endpoint < scan.size() && work-- > 0) {
      var access = scan.get(endpoint);
      if (access.pipe().isRemoved()
          || !owner.level().hasChunkAt(access.pipe().getBlockPos())
          || !owner.level().hasChunkAt(access.pipe().getBlockPos().relative(access.side()))
          || TransportNetworks.capability(access.pipe(), access.side()) != access.handler()
          || slot >= access.handler().getSlots()) {
        endpoint++;
        slot = 0;
        continue;
      }
      ItemStack stack = access.handler().extractItem(slot++, Integer.MAX_VALUE, true);
      if (stack.isEmpty() || !access.accepts(stack)) continue;
      var key = new StackKey(stack);
      ItemStack existing = aggregate.get(key);
      if (existing != null)
        existing.setCount(
            (int) Math.min(Integer.MAX_VALUE, (long) existing.getCount() + stack.getCount()));
      else if (building.size() < MAX_ENTRIES) {
        var copy = stack.copy();
        building.add(copy);
        aggregate.put(new StackKey(copy), copy);
      } else truncated = true;
    }
    if (endpoint >= scan.size()) {
      stock.clear();
      stock.addAll(building);
      stock.sort(
          Comparator.comparing(stack -> stack.getHoverName().getString().toLowerCase(Locale.ROOT)));
      scanning = false;
      nextScan = scannedTick + 20;
      showPage();
    }
  }

  private List<ItemStack> filtered() {
    String query = search.toLowerCase(Locale.ROOT);
    return stock.stream()
        .filter(
            stack ->
                query.startsWith("@")
                    ? BuiltInRegistries.ITEM
                        .getKey(stack.getItem())
                        .getNamespace()
                        .contains(query.substring(1))
                    : stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query))
        .toList();
  }

  private void showPage() {
    var entries = filtered();
    page = Math.clamp(page, 0, Math.max(0, (entries.size() - 1) / PAGE_SIZE));
    for (int i = 0; i < PAGE_SIZE; i++) {
      int index = page * PAGE_SIZE + i;
      // Counts are a snapshot, never a promise that extraction will succeed.
      display.setStackInSlot(
          i,
          index < entries.size()
              ? entries.get(index).copyWithCount(Math.min(99, entries.get(index).getCount()))
              : ItemStack.EMPTY);
    }
  }

  public void search(Player player, String text) {
    if (editable(player)) {
      search = text;
      page = 0;
      showPage();
      broadcastChanges();
    }
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (!editable(player)) return false;
    if (id == 0) page = Math.max(0, page - 1);
    else if (id == 1) page++;
    else if (id == 2) {
      if (!getCarried().isEmpty()) setCarried(deposit(getCarried()));
    } else if (id == 3) {
      // Refill from exact, already-owned ingredients; never craft from speculative stock.
      for (int i = 0; i < 9; i++) {
        var stack = crafting.getItem(i);
        if (!stack.isEmpty() && stack.getCount() < stack.getMaxStackSize()) {
          var extra = take(stack, stack.getMaxStackSize() - stack.getCount());
          stack.grow(extra.getCount());
          crafting.setItem(i, stack);
        }
      }
    } else return false;
    showPage();
    broadcastChanges();
    return true;
  }

  private boolean spendProbe() {
    long now = owner.level().getGameTime();
    if (transactionTick != now) {
      transactionTick = now;
      transactionWork = SCAN_BUDGET;
    }
    if (transactionWork <= 0) return false;
    transactionWork--;
    return true;
  }

  private ItemStack take(ItemStack wanted, int count) {
    ItemStack collected = ItemStack.EMPTY;
    if (!spendProbe()) return ItemStack.EMPTY;
    for (var access : TransportNetworks.storage(pipe, false)) {
      if (!access.accepts(wanted)) continue;
      int remainingCap = Math.min(count, access.pipe().side(access.side()).extractionLimit);
      for (int i = 0; i < access.handler().getSlots() && spendProbe() && remainingCap > 0; i++) {
        var actual = access.handler().getStackInSlot(i);
        if (!ItemStack.isSameItemSameComponents(actual, wanted)) continue;
        var extracted = access.handler().extractItem(i, Math.min(count, remainingCap), false);
        if (extracted.isEmpty()) continue;
        if (!ItemStack.isSameItemSameComponents(extracted, wanted)) {
          // A changing handler returned another resource; preserve it for the player.
          if (!owner.getInventory().add(extracted)) owner.drop(extracted, false);
          continue;
        }
        if (collected.isEmpty()) collected = extracted;
        else collected.grow(extracted.getCount());
        count -= extracted.getCount();
        remainingCap -= extracted.getCount();
        if (count <= 0) return collected;
      }
      if (transactionWork <= 0) break;
    }
    return collected;
  }

  private ItemStack deposit(ItemStack offered) {
    ItemStack remainder = offered.copy();
    if (!spendProbe()) return remainder;
    for (var access : TransportNetworks.storage(pipe, true)) {
      if (!access.accepts(remainder)) continue;
      for (int i = 0; i < access.handler().getSlots() && spendProbe(); i++) {
        remainder = access.handler().insertItem(i, remainder, false);
        if (remainder.isEmpty()) return ItemStack.EMPTY;
      }
      if (transactionWork <= 0) break;
    }
    return remainder;
  }

  @Override
  public void clicked(int index, int button, ClickType type, Player player) {
    if (index >= 0 && index < PAGE_SIZE) {
      if (editable(player) && type == ClickType.PICKUP) {
        var wanted = display.getStackInSlot(index).copy();
        if (!getCarried().isEmpty()) setCarried(deposit(getCarried()));
        else if (!wanted.isEmpty())
          setCarried(take(wanted, button == 1 ? 1 : wanted.getMaxStackSize()));
        nextScan = 0;
        broadcastChanges();
      }
      return;
    }
    if (!editable(player) && pipe != null) return;
    if (index == PAGE_SIZE) slotsChanged(crafting);
    super.clicked(index, button, type, player);
  }

  @Override
  public void slotsChanged(Container container) {
    if (owner == null || owner.level().isClientSide) return;
    var recipe =
        owner
            .level()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, crafting.asCraftInput(), owner.level());
    ItemStack output = ItemStack.EMPTY;
    if (recipe.isPresent()
        && owner instanceof ServerPlayer server
        && result.setRecipeUsed(owner.level(), server, recipe.get())) {
      output =
          recipe.get().value().assemble(crafting.asCraftInput(), owner.level().registryAccess());
      if (!output.isItemEnabled(owner.level().enabledFeatures())) output = ItemStack.EMPTY;
    }
    result.setItem(0, output);
    broadcastChanges();
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    if (!player.level().isClientSide) clearContainer(player, crafting);
  }

  @Override
  public boolean canDragTo(Slot slot) {
    return !(slot instanceof SlotItemHandler);
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    return !(slot instanceof SlotItemHandler);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (index <= PAGE_SIZE || !editable(player)) return ItemStack.EMPTY;
    return quickMove(player, index, PAGE_SIZE + 1, PAGE_SIZE + 10, PAGE_SIZE + 10, slots.size());
  }
}
