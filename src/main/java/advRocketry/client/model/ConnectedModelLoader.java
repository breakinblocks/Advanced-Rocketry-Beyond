package advRocketry.client.model;

import advRocketry.Main;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public final class ConnectedModelLoader implements IGeometryLoader<ConnectedModelLoader.Geometry> {
  public static final ResourceLocation ID =
      ResourceLocation.fromNamespaceAndPath(Main.MODID, "connected");
  static final String[] VARIANTS = {"base", "horizontal", "vertical", "corner", "center"};
  private static final ConnectedModelLoader INSTANCE = new ConnectedModelLoader();

  private ConnectedModelLoader() {}

  public static void register(ModelEvent.RegisterGeometryLoaders event) {
    event.register(ID, INSTANCE);
  }

  @Override
  public Geometry read(JsonObject json, JsonDeserializationContext context) {
    List<ResourceLocation> connectWith = new ArrayList<>();
    if (json.has("connect_with"))
      for (JsonElement element : json.getAsJsonArray("connect_with"))
        connectWith.add(ResourceLocation.parse(element.getAsString()));
    int tint = json.has("tint_index") ? json.get("tint_index").getAsInt() : -1;
    return new Geometry(Set.copyOf(connectWith), tint);
  }

  public record Geometry(Set<ResourceLocation> connectWith, int tint)
      implements IUnbakedGeometry<Geometry> {
    @Override
    public BakedModel bake(
        IGeometryBakingContext context,
        ModelBaker baker,
        Function<Material, TextureAtlasSprite> sprites,
        ModelState state,
        ItemOverrides overrides) {
      TextureAtlasSprite[] variants = new TextureAtlasSprite[VARIANTS.length];
      for (int i = 0; i < VARIANTS.length; i++)
        variants[i] = sprites.apply(context.getMaterial(VARIANTS[i]));
      TextureAtlasSprite particle =
          context.hasMaterial("particle")
              ? sprites.apply(context.getMaterial("particle"))
              : variants[0];
      ResourceLocation hint = context.getRenderTypeHint();
      RenderTypeGroup renderTypes =
          hint == null ? RenderTypeGroup.EMPTY : context.getRenderType(hint);
      return new ConnectedBakedModel(
          ConnectedBakedModel.bake(variants, state, tint),
          particle,
          context.getTransforms(),
          context.useAmbientOcclusion(),
          context.useBlockLight(),
          renderTypes,
          connectWith);
    }
  }
}
