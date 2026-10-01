// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Original stormland electric mushroom: occasional visual lightning during rain. */
public final class ElectricMushroomBlock extends BushBlock {
  public static final MapCodec<ElectricMushroomBlock> CODEC =
      simpleCodec(ElectricMushroomBlock::new);
  private static final ResourceKey<Biome> STORMLAND =
      ResourceKey.create(
          Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Main.MODID, "stormland"));

  public ElectricMushroomBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends BushBlock> codec() {
    return CODEC;
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (!level.getBiome(pos).is(STORMLAND) || random.nextInt(100) != 0) return;
    for (int index = 0; index < 7; index++)
      level.addParticle(
          ParticleTypes.ELECTRIC_SPARK,
          pos.getX() + .5 + (random.nextDouble() - .5) * .6,
          pos.getY() + .5 + (random.nextDouble() - .5) * .6,
          pos.getZ() + .5 + (random.nextDouble() - .5) * .6,
          (random.nextDouble() - .5) * .1,
          random.nextDouble() * .15,
          (random.nextDouble() - .5) * .1);
    level.playLocalSound(
        pos.getX() + .5,
        pos.getY() + .5,
        pos.getZ() + .5,
        ProcessingRegistry.ELECTRIC_SHOCK_SOUND.get(),
        SoundSource.BLOCKS,
        .7f,
        .975f + random.nextFloat() * .05f,
        false);
  }

  @Override
  public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    if (level instanceof ServerLevel server) {
      server.sendParticles(
          ParticleTypes.ELECTRIC_SPARK,
          pos.getX() + .5,
          pos.getY() + .5,
          pos.getZ() + .5,
          7,
          .3,
          .3,
          .3,
          .05);
      server.playSound(
          null,
          pos,
          ProcessingRegistry.ELECTRIC_SHOCK_SOUND.get(),
          SoundSource.BLOCKS,
          .7f,
          .975f + level.random.nextFloat() * .05f);
    }
    return super.playerWillDestroy(level, pos, state, player);
  }

  @Override
  protected void randomTick(
      BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (!AdvancedRocketryConfig.electricPlantsSpawnLightning()
        || !level.isRainingAt(pos.above())
        || !level.getBiome(pos).is(STORMLAND)) return;
    BlockPos target =
        level.getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING,
            pos.offset(random.nextInt(24) - 12, 0, random.nextInt(24) - 12));
    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
    if (bolt != null) {
      bolt.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
      bolt.setVisualOnly(true);
      level.addFreshEntity(bolt);
    }
  }
}
