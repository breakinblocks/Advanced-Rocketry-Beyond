package advRocketry.client.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Cached half-unit spheres with smooth normals and continuous latitude/longitude UVs. */
public final class HologramSphere {
  private static final float[][] MESHES = {build(16), build(32), build(64)};

  private HologramSphere() {}

  public static void render(PoseStack pose, VertexConsumer vertices, int detail, int color) {
    float[] mesh = MESHES[Math.clamp(detail, 0, 2)];
    for (int i = 0; i < mesh.length; i += 5) {
      float x = mesh[i], y = mesh[i + 1], z = mesh[i + 2];
      vertices
          .addVertex(pose.last(), x, y, z)
          .setColor(color)
          .setUv(mesh[i + 3], mesh[i + 4])
          .setOverlay(OverlayTexture.NO_OVERLAY)
          .setLight(0xf000f0)
          .setNormal(pose.last(), x * 2, y * 2, z * 2);
    }
  }

  private static float[] build(int columns) {
    int rows = columns / 2;
    float[] mesh = new float[columns * rows * 4 * 5];
    int index = 0;
    int[] dx = {0, 1, 1, 0}, dy = {0, 0, 1, 1};
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        for (int corner = 0; corner < 4; corner++) {
          float u = (column + dx[corner]) / (float) columns;
          float v = (row + dy[corner]) / (float) rows;
          double longitude = u * Math.PI * 2, latitude = v * Math.PI;
          mesh[index++] = (float) (Math.sin(latitude) * Math.cos(longitude) * .5);
          mesh[index++] = (float) (Math.cos(latitude) * .5);
          mesh[index++] = (float) (Math.sin(latitude) * Math.sin(longitude) * .5);
          mesh[index++] = u;
          mesh[index++] = v;
        }
      }
    }
    return mesh;
  }
}
