// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ProcessingRecipeSerializer implements RecipeSerializer<ProcessingRecipe> {
  private static final StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipe> STREAM_CODEC =
      ByteBufCodecs.fromCodecWithRegistries(ProcessingRecipe.CODEC);

  @Override
  public MapCodec<ProcessingRecipe> codec() {
    return ProcessingRecipe.MAP_CODEC;
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipe> streamCodec() {
    return STREAM_CODEC;
  }
}
