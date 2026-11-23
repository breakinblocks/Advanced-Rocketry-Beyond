package advRocketry.client.model;

import advRocketry.space.BiomeMaterials;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class RootedLoamModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<Boolean> COVERED = new ModelProperty<>();
    private static final ModelResourceLocation EXPOSED =
            new ModelResourceLocation(BiomeMaterials.id("rooted_loam"), "");
    private static final List<ModelResourceLocation> SOIL_MODELS =
            List.of(soilModel(""), soilModel("_1"), soilModel("_2"), soilModel("_3"));
    private final BakedModel soil;

    private RootedLoamModel(BakedModel exposed, BakedModel soil) {
        super(exposed);
        this.soil = soil;
    }

    private static ModelResourceLocation soilModel(String suffix) {
        return ModelResourceLocation.standalone(BiomeMaterials.id("block/rooted_loam_covered" + suffix));
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        SOIL_MODELS.forEach(event::register);
    }

    public static void wrapModel(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        var soil = new WeightedBakedModel.Builder();
        for (ModelResourceLocation model : SOIL_MODELS) soil.add(models.get(model), 1);
        BakedModel covered = soil.build();
        if (covered != null) models.computeIfPresent(EXPOSED, (key, exposed) -> new RootedLoamModel(exposed, covered));
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        // Derive this from the world so generated terrain and existing stacks use the same rule as placed blocks.
        BlockPos above = pos.above();
        BlockState cover = level.getBlockState(above);
        ModelData exposedData = super.getModelData(level, pos, state, data);
        return cover.canOcclude() && cover.isFaceSturdy(level, above, Direction.DOWN)
                ? exposedData.derive().with(COVERED, true).build()
                : exposedData;
    }

    @Override
    public List<BakedQuad> getQuads(
            BlockState state, Direction side, RandomSource random, ModelData data, RenderType renderType) {
        BakedModel model = data.has(COVERED) ? soil : originalModel;
        return model.getQuads(state, side, random, data, renderType);
    }
}
