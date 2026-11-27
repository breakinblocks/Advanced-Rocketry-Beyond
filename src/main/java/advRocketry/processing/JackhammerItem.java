// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.ModTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/** A powered excavator for rock and loose ground. */
public final class JackhammerItem extends DiggerItem {
    public JackhammerItem(Properties properties) {
        super(
                Tiers.DIAMOND,
                ModTags.JACKHAMMER_MINEABLE,
                properties.component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(ModTags.JACKHAMMER_MINEABLE) ? 50 : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability) || ability == ItemAbilities.SHOVEL_DIG;
    }
}
