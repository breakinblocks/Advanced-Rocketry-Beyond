package advRocketry.compat.jei;

import advRocketry.transport.TransportGhostEdit;
import advRocketry.transport.TransportRegistry;
import advRocketry.transport.client.TransportScreen;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

final class TransportGhostHandler implements IGhostIngredientHandler<TransportScreen> {
  @Override
  public <I> List<Target<I>> getTargetsTyped(
      TransportScreen screen, ITypedIngredient<I> ingredient, boolean start) {
    var menu = screen.getMenu();
    Object value = ingredient.getIngredient();
    if (!(value instanceof ItemStack) && !(value instanceof FluidStack)) return List.of();
    if (menu.kind == TransportRegistry.Kind.ENERGY) return List.of();
    var result = new ArrayList<Target<I>>();
    for (int i = 0; i < 9; i++) {
      int index = i;
      var slot = menu.slots.get(i + (menu.kind == TransportRegistry.Kind.ITEM ? 1 : 0));
      result.add(
          new Target<>() {
            @Override
            public Rect2i getArea() {
              return new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
            }

            @Override
            public void accept(I value) {
              PacketDistributor.sendToServer(
                  new TransportGhostEdit(
                      menu.containerId,
                      menu.value(13),
                      index,
                      value instanceof ItemStack item ? item.copyWithCount(1) : ItemStack.EMPTY,
                      value instanceof FluidStack fluid
                          ? fluid.copyWithAmount(1000)
                          : FluidStack.EMPTY));
            }
          });
    }
    return result;
  }

  @Override
  public void onComplete() {}
}
