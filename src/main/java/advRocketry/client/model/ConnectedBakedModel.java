package advRocketry.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.joml.Vector3f;

public final class ConnectedBakedModel implements IDynamicBakedModel {
  private static final ModelProperty<byte[]> VARIANTS = new ModelProperty<>();
  private static final Direction[] RIGHT = new Direction[6];
  private static final Direction[] DOWN = new Direction[6];

  static {
    axes(Direction.NORTH, Direction.WEST, Direction.DOWN);
    axes(Direction.SOUTH, Direction.EAST, Direction.DOWN);
    axes(Direction.WEST, Direction.SOUTH, Direction.DOWN);
    axes(Direction.EAST, Direction.NORTH, Direction.DOWN);
    axes(Direction.UP, Direction.EAST, Direction.SOUTH);
    axes(Direction.DOWN, Direction.EAST, Direction.NORTH);
  }

  private final BakedQuad[][][] quads;
  private final TextureAtlasSprite particle;
  private final ItemTransforms transforms;
  private final boolean ambientOcclusion;
  private final boolean blockLight;
  private final RenderTypeGroup renderTypes;
  private final Set<ResourceLocation> connectWith;

  ConnectedBakedModel(
      BakedQuad[][][] quads,
      TextureAtlasSprite particle,
      ItemTransforms transforms,
      boolean ambientOcclusion,
      boolean blockLight,
      RenderTypeGroup renderTypes,
      Set<ResourceLocation> connectWith) {
    this.quads = quads;
    this.particle = particle;
    this.transforms = transforms;
    this.ambientOcclusion = ambientOcclusion;
    this.blockLight = blockLight;
    this.renderTypes = renderTypes;
    this.connectWith = connectWith;
  }

  private static void axes(Direction face, Direction right, Direction down) {
    RIGHT[face.ordinal()] = right;
    DOWN[face.ordinal()] = down;
  }

  static BakedQuad[][][] bake(TextureAtlasSprite[] sprites, ModelState state, int tint) {
    FaceBakery bakery = new FaceBakery();
    BakedQuad[][][] result = new BakedQuad[6][4][sprites.length];
    for (Direction face : Direction.values())
      for (int quadrant = 0; quadrant < 4; quadrant++) {
        float u0 = (quadrant & 1) * 8, v0 = (quadrant >> 1) * 8, u1 = u0 + 8, v1 = v0 + 8;
        Vector3f[] box = box(face, u0, v0, u1, v1);
        for (int variant = 0; variant < sprites.length; variant++)
          result[face.ordinal()][quadrant][variant] =
              bakery.bakeQuad(
                  box[0],
                  box[1],
                  new BlockElementFace(
                      face, tint, "", new BlockFaceUV(new float[] {u0, v0, u1, v1}, 0)),
                  sprites[variant],
                  face,
                  state,
                  null,
                  true);
      }
    return result;
  }

  private static Vector3f[] box(Direction face, float u0, float v0, float u1, float v1) {
    return switch (face) {
      case NORTH ->
          new Vector3f[] {new Vector3f(16 - u1, 16 - v1, 0), new Vector3f(16 - u0, 16 - v0, 16)};
      case SOUTH -> new Vector3f[] {new Vector3f(u0, 16 - v1, 0), new Vector3f(u1, 16 - v0, 16)};
      case WEST -> new Vector3f[] {new Vector3f(0, 16 - v1, u0), new Vector3f(16, 16 - v0, u1)};
      case EAST ->
          new Vector3f[] {new Vector3f(0, 16 - v1, 16 - u1), new Vector3f(16, 16 - v0, 16 - u0)};
      case UP -> new Vector3f[] {new Vector3f(u0, 0, v0), new Vector3f(u1, 16, v1)};
      case DOWN -> new Vector3f[] {new Vector3f(u0, 0, 16 - v1), new Vector3f(u1, 16, 16 - v0)};
    };
  }

  @Override
  public ModelData getModelData(
      BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
    byte[] variants = new byte[24];
    for (Direction face : Direction.values()) {
      Direction right = RIGHT[face.ordinal()], down = DOWN[face.ordinal()];
      for (int quadrant = 0; quadrant < 4; quadrant++) {
        Direction side = (quadrant & 1) == 0 ? right.getOpposite() : right;
        Direction vertical = (quadrant >> 1) == 0 ? down.getOpposite() : down;
        boolean h = connects(level, pos, state, face, pos.relative(side));
        boolean v = connects(level, pos, state, face, pos.relative(vertical));
        boolean d = connects(level, pos, state, face, pos.relative(side).relative(vertical));
        variants[face.ordinal() * 4 + quadrant] =
            (byte) (!h && !v ? 0 : h && !v ? 1 : !h ? 2 : d ? 4 : 3);
      }
    }
    return modelData.derive().with(VARIANTS, variants).build();
  }

  private boolean connects(
      BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face, BlockPos other) {
    return matches(state, level.getBlockState(other))
        && !matches(state, level.getBlockState(other.relative(face)));
  }

  private boolean matches(BlockState self, BlockState other) {
    return other.getBlock() == self.getBlock()
        || connectWith.contains(BuiltInRegistries.BLOCK.getKey(other.getBlock()));
  }

  @Override
  public List<BakedQuad> getQuads(
      BlockState state, Direction side, RandomSource random, ModelData data, RenderType type) {
    if (side == null) return List.of();
    byte[] variants = data.get(VARIANTS);
    List<BakedQuad> result = new ArrayList<>(4);
    for (int quadrant = 0; quadrant < 4; quadrant++)
      result.add(
          quads[side.ordinal()][quadrant][
              variants == null ? 0 : variants[side.ordinal() * 4 + quadrant]]);
    return result;
  }

  @Override
  public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
    return renderTypes.isEmpty()
        ? ChunkRenderTypeSet.of(RenderType.solid())
        : ChunkRenderTypeSet.of(renderTypes.block());
  }

  @Override
  public boolean useAmbientOcclusion() {
    return ambientOcclusion;
  }

  @Override
  public boolean isGui3d() {
    return true;
  }

  @Override
  public boolean usesBlockLight() {
    return blockLight;
  }

  @Override
  public boolean isCustomRenderer() {
    return false;
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return particle;
  }

  @Override
  public ItemTransforms getTransforms() {
    return transforms;
  }

  @Override
  public ItemOverrides getOverrides() {
    return ItemOverrides.EMPTY;
  }
}
