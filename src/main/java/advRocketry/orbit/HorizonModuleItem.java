package advRocketry.orbit;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class HorizonModuleItem extends Item {
  public HorizonModuleItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(
        Component.translatable(
                "tooltip.adv_rocketry.horizon.bonus", OrbitalRegistry.horizonBonus(stack))
            .withStyle(ChatFormatting.LIGHT_PURPLE));
    tooltip.add(Component.translatable("tooltip.adv_rocketry.horizon.orbit"));
    tooltip.add(Component.translatable("tooltip.adv_rocketry.horizon.no_power"));
  }
}
