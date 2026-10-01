package advRocketry.datagen;

import advRocketry.Main;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

abstract class ModItemModelHelpers extends ItemModelProvider {
  ModItemModelHelpers(PackOutput output, ExistingFileHelper files) {
    super(output, Main.MODID, files);
  }

  static ResourceLocation location(String path) {
    return path.contains(":")
        ? ResourceLocation.parse(path)
        : ResourceLocation.fromNamespaceAndPath(Main.MODID, path);
  }

  void model(
      String path, String parent, String renderType, boolean ambientOcclusion, String... textures) {
    ItemModelBuilder builder =
        getBuilder(path).parent(new ModelFile.UncheckedModelFile(location(parent)));
    for (int index = 0; index < textures.length; index += 2)
      builder.texture(textures[index], location(textures[index + 1]));
    if (renderType != null) builder.renderType(renderType);
    if (!ambientOcclusion) builder.ao(false);
  }

  void bucket(String path, String fluid) {
    getBuilder(path)
        .parent(new ModelFile.UncheckedModelFile(ResourceLocation.parse("neoforge:item/bucket")))
        .customLoader(DynamicFluidContainerModelBuilder::begin)
        .fluid(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)))
        .flipGas(true);
  }
}
