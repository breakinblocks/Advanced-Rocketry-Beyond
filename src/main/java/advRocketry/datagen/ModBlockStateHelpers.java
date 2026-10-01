package advRocketry.datagen;

import advRocketry.Main;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

abstract class ModBlockStateHelpers extends BlockStateProvider {
  ModBlockStateHelpers(PackOutput output, ExistingFileHelper files) {
    super(output, Main.MODID, files);
  }

  static ResourceLocation location(String path) {
    return path.contains(":")
        ? ResourceLocation.parse(path)
        : ResourceLocation.fromNamespaceAndPath(Main.MODID, path);
  }

  static ModelFile unchecked(String path) {
    return new ModelFile.UncheckedModelFile(location(path));
  }

  void model(
      String path, String parent, String renderType, boolean ambientOcclusion, String... textures) {
    BlockModelBuilder builder = models().getBuilder(path).parent(unchecked(parent));
    for (int index = 0; index < textures.length; index += 2)
      builder.texture(textures[index], location(textures[index + 1]));
    if (renderType != null) builder.renderType(renderType);
    if (!ambientOcclusion) builder.ao(false);
  }

  void obj(String path, String model, String particle) {
    models()
        .getBuilder(path)
        .parent(unchecked("minecraft:block/block"))
        .texture("particle", location(particle))
        .customLoader(ObjModelBuilder::begin)
        .modelLocation(location(model))
        .flipV(true)
        .automaticCulling(false);
  }

  void connected(String path, String folder, int tint, String... connectWith) {
    BlockModelBuilder builder =
        models()
            .getBuilder(path)
            .parent(unchecked("minecraft:block/block"))
            .texture("particle", location(folder + "/base"));
    for (String variant : new String[] {"base", "horizontal", "vertical", "corner", "center"})
      builder.texture(variant, location(folder + "/" + variant));
    ConnectedModelBuilder<BlockModelBuilder> loader =
        builder.customLoader(ConnectedModelBuilder::begin).tint(tint);
    for (String block : connectWith) loader.connectWith(block);
  }

  void states(String name, Map<String, int[]> rotations, String... models) {
    Block block = BuiltInRegistries.BLOCK.get(location(name));
    VariantBlockStateBuilder builder = getVariantBuilder(block);
    for (int index = 0; index < models.length; index += 2)
      for (var rotation : rotations.entrySet()) {
        VariantBlockStateBuilder.PartialBlockstate state = builder.partialState();
        state = apply(block, state, models[index]);
        state = apply(block, state, rotation.getKey());
        builder.setModels(
            state,
            ConfiguredModel.builder()
                .modelFile(unchecked(models[index + 1]))
                .rotationX(rotation.getValue()[0])
                .rotationY(rotation.getValue()[1])
                .build());
      }
  }

  private static VariantBlockStateBuilder.PartialBlockstate apply(
      Block block, VariantBlockStateBuilder.PartialBlockstate state, String properties) {
    if (properties.isEmpty()) return state;
    for (String pair : properties.split(",")) {
      String[] parts = pair.split("=");
      state = with(state, block.getStateDefinition().getProperty(parts[0]), parts[1]);
    }
    return state;
  }

  private static <T extends Comparable<T>> VariantBlockStateBuilder.PartialBlockstate with(
      VariantBlockStateBuilder.PartialBlockstate state, Property<T> property, String value) {
    return state.with(property, property.getValue(value).orElseThrow());
  }
}
