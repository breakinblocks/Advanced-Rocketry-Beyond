package advRocketry.space.client;

import advRocketry.AdvancedRocketryConfig;
import advRocketry.Main;
import advRocketry.space.Planet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Layered sky geometry using standard textured/color passes understood by shader packs. */
public final class PlanetAtmosphere {
    private static final ResourceLocation SPARSE = texture("cloud_sparse");
    private static final ResourceLocation BROAD = texture("cloud_broad");
    private static final ResourceLocation WISPS = texture("cloud_wisps");
    private static final Grid BACKGROUND = Grid.background(96, 32);
    private static final Grid CLOUDS_LOW = Grid.clouds(48, 32);
    private static final Grid CLOUDS_HIGH = Grid.clouds(96, 32);
    private static final Vector3f POINT = new Vector3f();
    private static float[] positions = new float[0];
    private static float[] colors = new float[0];
    private static float[] uvs = new float[0];

    private PlanetAtmosphere() {}

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/sky/" + name + ".png");
    }

    private record Grid(int segments, int bands, float[] x, float[] y, float[] z, float[] a, float[] b) {
        int points() {
            return (segments + 1) * (bands + 1);
        }

        static Grid background(int segments, int bands) {
            Grid grid = allocate(segments, bands);
            for (int row = 0; row <= bands; row++)
                for (int column = 0; column <= segments; column++)
                    grid.set(
                            row * (segments + 1) + column,
                            Math.PI * (row / (double) bands - .5),
                            column * Math.PI * 2 / segments);
            for (int index = 0; index < grid.points(); index++) {
                float sy = grid.y[index];
                grid.a[index] = (float) Math.pow(1 - Math.abs(sy), 4);
                grid.b[index] = .68f + .32f * Math.max(0, sy);
            }
            return grid;
        }

        static Grid clouds(int segments, int bands) {
            Grid grid = allocate(segments, bands);
            for (int row = 0; row <= bands; row++)
                for (int column = 0; column <= segments; column++)
                    grid.set(row * (segments + 1) + column, row * Math.PI / 64, column * Math.PI * 2 / segments);
            for (int index = 0; index < grid.points(); index++) {
                float sy = grid.y[index];
                grid.a[index] = PlanetSkyState.smooth(0, .23f, sy);
                grid.b[index] = .8f / (1 + sy);
            }
            return grid;
        }

        private static Grid allocate(int segments, int bands) {
            int points = (segments + 1) * (bands + 1);
            return new Grid(
                    segments,
                    bands,
                    new float[points],
                    new float[points],
                    new float[points],
                    new float[points],
                    new float[points]);
        }

        private void set(int index, double elevation, double azimuth) {
            y[index] = (float) Math.sin(elevation);
            x[index] = (float) (Math.cos(elevation) * Math.cos(azimuth));
            z[index] = (float) (Math.cos(elevation) * Math.sin(azimuth));
        }
    }

    private static void prepare(Matrix4f view, Grid grid) {
        int points = grid.points();
        if (positions.length < points * 3) {
            positions = new float[points * 3];
            colors = new float[points * 4];
            uvs = new float[points * 2];
        }
        for (int index = 0; index < points; index++) {
            view.transformPosition(grid.x[index] * 100, grid.y[index] * 100, grid.z[index] * 100, POINT);
            positions[index * 3] = POINT.x;
            positions[index * 3 + 1] = POINT.y;
            positions[index * 3 + 2] = POINT.z;
        }
    }

    private static void emit(BufferBuilder buffer, Grid grid, boolean textured) {
        int width = grid.segments + 1;
        for (int row = 0; row < grid.bands; row++)
            for (int column = 0; column < grid.segments; column++) {
                int corner = row * width + column;
                vertex(buffer, corner, textured);
                vertex(buffer, corner + 1, textured);
                vertex(buffer, corner + width + 1, textured);
                vertex(buffer, corner + width, textured);
            }
    }

    private static void vertex(BufferBuilder buffer, int index, boolean textured) {
        buffer.addVertex(positions[index * 3], positions[index * 3 + 1], positions[index * 3 + 2]);
        if (textured) buffer.setUv(uvs[index * 2], uvs[index * 2 + 1]);
        buffer.setColor(colors[index * 4], colors[index * 4 + 1], colors[index * 4 + 2], colors[index * 4 + 3]);
    }

    public static void background(Matrix4f view, Planet planet, PlanetSkyState state, Vec3 base, float angle) {
        Grid grid = BACKGROUND;
        prepare(view, grid);
        Vec3 fog = Vec3.fromRGB24(planet.fogColor);
        double light = .025 + .55 * state.daylight();
        fog = fog.scale(light * Math.min(1, state.density()));
        double haze = Math.min(1, state.density() * planet.sky.haze());
        double sun = -Math.sin(angle * Math.PI * 2);
        double twilight = state.twilight() * planet.sky.sunset();
        double dim = 1 - state.weather() * .35;
        double sunsetRed = .7 + ((planet.skyColor >> 16) & 255) / 255d * .3;
        double sunsetGreen = .224 + ((planet.skyColor >> 8) & 255) / 255d * .3;
        double sunsetBlue = .07 + (planet.skyColor & 255) / 255d * .3;
        for (int index = 0; index < grid.points(); index++) {
            double horizon = grid.a[index] * haze;
            double mix = grid.y[index] < 0 ? .8 : horizon * .65;
            double weight = grid.b[index] * (1 - mix);
            double facing = Math.max(0, sun * grid.x[index]);
            double sunset = Math.min(.85, twilight * horizon * (.12 + .65 * facing * facing * facing));
            double keep = 1 - sunset;
            colors[index * 4] = (float) (((base.x * weight + fog.x * mix) * keep + sunsetRed * sunset) * dim);
            colors[index * 4 + 1] = (float) (((base.y * weight + fog.y * mix) * keep + sunsetGreen * sunset) * dim);
            colors[index * 4 + 2] = (float) (((base.z * weight + fog.z * mix) * keep + sunsetBlue * sunset) * dim);
            colors[index * 4 + 3] = 1;
        }
        RenderSystem.setShader(SkyLayerShader::color);
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        emit(buffer, grid, false);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static void clouds(Matrix4f view, Planet planet, PlanetSkyState state, double ticks, Vec3 camera) {
        if (!AdvancedRocketryConfig.advancedVfx()
                || state.coverage() <= .001
                || AdvancedRocketryConfig.effectDetail() == 0) return;
        double motion = AdvancedRocketryConfig.reducedMotion()
                ? 0
                : ticks * planet.sky.clouds().speed() / 24000d;
        double direction = Math.toRadians(planet.sky.clouds().direction());
        double seed = Math.floorMod(planet.id * 31L + planet.sky.seed(), 997) / 997d;
        double u = motion * Math.cos(direction) + seed + camera.x / 4096;
        double v = motion * Math.sin(direction) + seed + camera.z / 4096;
        float broad = PlanetSkyState.smooth(.35f, .85f, state.coverage());
        Vec3 color = state.cloudColor().scale((.14 + state.daylight() * .86) * (1 - state.weather() * .4));
        color = color.lerp(new Vec3(1, .48, .25), Math.min(.55, state.twilight() * planet.sky.sunset() * .5));
        float opacity = Math.min(1, state.coverage() * 1.7f);
        Grid grid = AdvancedRocketryConfig.effectDetail() > 1 ? CLOUDS_HIGH : CLOUDS_LOW;
        prepare(view, grid);
        layer(grid, SPARSE, color, opacity * (1 - broad), u, v, 1);
        layer(grid, BROAD, color, opacity * broad, u, v, 1);
        if (AdvancedRocketryConfig.effectDetail() > 1)
            layer(grid, WISPS, color, opacity * .4f, u * .63 + .3, v * .7, 1.25);
    }

    private static void layer(
            Grid grid, ResourceLocation texture, Vec3 color, float alpha, double u, double v, double scale) {
        if (alpha < .001) return;
        float offsetU = (float) (u % 1), offsetV = (float) (v % 1);
        float red = (float) color.x, green = (float) color.y, blue = (float) color.z;
        for (int index = 0; index < grid.points(); index++) {
            float projection = (float) (scale * grid.b[index]);
            uvs[index * 2] = grid.x[index] * projection + offsetU;
            uvs[index * 2 + 1] = grid.z[index] * projection + offsetV;
            colors[index * 4] = red;
            colors[index * 4 + 1] = green;
            colors[index * 4 + 2] = blue;
            colors[index * 4 + 3] = alpha * grid.a[index];
        }
        RenderSystem.setShader(SkyLayerShader::texture);
        RenderSystem.setShaderTexture(0, texture);
        var skyTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
        skyTexture.setBlurMipmap(true, false);
        try {
            var buffer =
                    Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            emit(buffer, grid, true);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } finally {
            skyTexture.restoreLastBlurMipmap();
        }
    }

    public static void night(Matrix4f view, Planet planet, PlanetSkyState state, double ticks) {
        if (!AdvancedRocketryConfig.advancedVfx() || AdvancedRocketryConfig.effectDetail() == 0) return;
        float visible = Math.max(state.night(), 1 - Math.min(1, state.density())) * (1 - state.weather());
        if (visible <= .001) return;
        float opacity = visible * Math.min(1, state.density()) * planet.sky.aurora();
        if (opacity <= .001) return;
        double time = AdvancedRocketryConfig.reducedMotion() ? 0 : ticks / 160d;
        RenderSystem.setShader(SkyLayerShader::color);
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 96; i++) {
            for (int band = 0; band < 8; band++) {
                float low = band / 8f, high = (band + 1) / 8f;
                auroraVertex(buffer, view, planet.sky.seed(), time, i, low, opacity);
                auroraVertex(buffer, view, planet.sky.seed(), time, i + 1, low, opacity);
                auroraVertex(buffer, view, planet.sky.seed(), time, i + 1, high, opacity);
                auroraVertex(buffer, view, planet.sky.seed(), time, i, high, opacity);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void auroraVertex(
            BufferBuilder buffer, Matrix4f view, int seed, double time, int i, float vertical, float opacity) {
        double a = i / 96d * Math.PI * 2;
        double wave = Math.sin(a * 5 + time + seed) * .08 + Math.sin(a * 11 - time * .7) * .025;
        double elevation = .45 + wave + vertical * (.22 + .06 * Math.sin(a * 3 + time));
        float r = .16f + vertical * .32f, g = .85f - vertical * .6f;
        float fade = opacity
                * (float) Math.sin(vertical * Math.PI)
                * (.2f + .3f * (float) Math.pow(Math.sin(a * 3 + time * .2), 2));
        buffer.addVertex(
                        view,
                        (float) (Math.cos(a) * Math.cos(elevation) * 100),
                        (float) (Math.sin(elevation) * 100),
                        (float) (Math.sin(a) * Math.cos(elevation) * 100))
                .setColor(r, g, .65f, fade);
    }
}
