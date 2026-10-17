package advRocketry.processing;

import advRocketry.util.ModMenu;
import advRocketry.util.SplitIntData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class FuelGeneratorMenu extends ModMenu {
  private final BlockPos pos;
  private final FuelGeneratorBlockEntity generator;
  private final SplitIntData data;

  public FuelGeneratorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(id, inventory, buffer.readBlockPos(), null);
  }

  public FuelGeneratorMenu(int id, Inventory inventory, FuelGeneratorBlockEntity generator) {
    this(id, inventory, generator.getBlockPos(), generator);
  }

  private FuelGeneratorMenu(
      int id, Inventory inventory, BlockPos pos, FuelGeneratorBlockEntity generator) {
    super(MachinePorts.FUEL_GENERATOR_MENU.get(), id);
    this.pos = pos;
    this.generator = generator;
    addPlayerInventory(inventory, 8, 112);
    data =
        generator == null
            ? SplitIntData.client(6)
            : SplitIntData.server(
                6,
                index ->
                    switch (index) {
                      case 0 -> generator.energyStored();
                      case 1 -> generator.burnTime();
                      case 2 -> generator.fePerTick();
                      case 3 -> generator.tank.getFluidAmount();
                      case 4 -> BuiltInRegistries.FLUID.getId(generator.tank.getFluid().getFluid());
                      case 5 -> generator.burnDuration();
                      default -> 0;
                    });
    addDataSlots(data);
  }

  public int value(int index) {
    return data.value(index);
  }

  @Override
  public boolean stillValid(Player player) {
    return player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64
        && player.level().getBlockEntity(pos) instanceof FuelGeneratorBlockEntity current
        && (generator == null || generator == current && !generator.isRemoved());
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }
}
