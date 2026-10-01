// SPDX-License-Identifier: MIT
// Copyright (c) 2026 Saymayhem LLC. See NOTICE.md for original source notices.

package advRocketry.processing.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/** OBJ face data from the original MIT models, rendered through Minecraft's quad buffers. */
public final class ObjModel {
  private record Vertex(float x, float y, float z) {}

  private record UV(float u, float v) {}

  private record Corner(Vertex position, UV uv, Vertex normal) {}

  private final Map<String, List<Corner[]>> groups = new LinkedHashMap<>();

  public ObjModel(ResourceLocation resource) {
    List<Vertex> positions = new ArrayList<>();
    List<Vertex> normals = new ArrayList<>();
    List<UV> uv = new ArrayList<>();
    String group = "default";
    groups.put(group, new ArrayList<>());
    try (var stream = Minecraft.getInstance().getResourceManager().open(resource);
        var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length == 0) continue;
        switch (tokens[0]) {
          case "v" ->
              positions.add(
                  new Vertex(
                      Float.parseFloat(tokens[1]),
                      Float.parseFloat(tokens[2]),
                      Float.parseFloat(tokens[3])));
          case "vn" ->
              normals.add(
                  new Vertex(
                      Float.parseFloat(tokens[1]),
                      Float.parseFloat(tokens[2]),
                      Float.parseFloat(tokens[3])));
          case "vt" -> uv.add(new UV(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));
          case "o", "g" -> {
            group = tokens[1];
            groups.computeIfAbsent(group, key -> new ArrayList<>());
          }
          case "f" -> {
            List<Corner> face = new ArrayList<>();
            for (int i = 1; i < tokens.length; i++) {
              String[] indices = tokens[i].split("/", -1);
              Vertex position = positions.get(index(indices[0], positions.size()));
              UV texture =
                  indices.length > 1 && !indices[1].isEmpty()
                      ? uv.get(index(indices[1], uv.size()))
                      : new UV(0, 0);
              Vertex normal =
                  indices.length > 2 && !indices[2].isEmpty()
                      ? normals.get(index(indices[2], normals.size()))
                      : null;
              face.add(new Corner(position, texture, normal));
            }
            for (int i = 1; i < face.size() - 1; i++)
              groups.get(group).add(new Corner[] {face.getFirst(), face.get(i), face.get(i + 1)});
          }
          default -> {}
        }
      }
    } catch (IOException exception) {
      throw new UncheckedIOException("Cannot load original model " + resource, exception);
    }
  }

  private static int index(String value, int size) {
    int index = Integer.parseInt(value);
    return index < 0 ? size + index : index - 1;
  }

  public void renderPart(
      String name, PoseStack pose, VertexConsumer vertices, int light, int overlay) {
    render(groups.getOrDefault(name, List.of()), pose, vertices, light, overlay, -1);
  }

  public void renderPart(
      String name, PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) {
    render(groups.getOrDefault(name, List.of()), pose, vertices, light, overlay, color);
  }

  public void renderAll(
      PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) {
    groups.values().forEach(faces -> render(faces, pose, vertices, light, overlay, color));
  }

  private static void render(
      List<Corner[]> faces,
      PoseStack pose,
      VertexConsumer vertices,
      int light,
      int overlay,
      int color) {
    for (Corner[] face : faces) {
      Vertex a = face[0].position();
      Vertex b = face[1].position();
      Vertex c = face[2].position();
      float nx = (b.y - a.y) * (c.z - a.z) - (b.z - a.z) * (c.y - a.y);
      float ny = (b.z - a.z) * (c.x - a.x) - (b.x - a.x) * (c.z - a.z);
      float nz = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
      float length = Math.max(0.000001f, (float) Math.sqrt(nx * nx + ny * ny + nz * nz));
      Vertex computedNormal = new Vertex(nx / length, ny / length, nz / length);
      // Duplicate the last corner to encode one triangle in a vanilla quad buffer.
      for (int i = 0; i < 4; i++) {
        Corner corner = face[Math.min(i, 2)];
        Vertex position = corner.position();
        Vertex normal = corner.normal() == null ? computedNormal : corner.normal();
        vertices
            .addVertex(pose.last(), position.x, position.y, position.z)
            .setColor(color)
            .setUv(corner.uv.u, 1 - corner.uv.v)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(pose.last(), normal.x, normal.y, normal.z);
      }
    }
  }
}
