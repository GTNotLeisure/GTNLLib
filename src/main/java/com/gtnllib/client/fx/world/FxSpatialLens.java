package com.gtnllib.client.fx.world;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.gtnllib.GTNLLib;
import com.gtnllib.client.shader.ShaderEffect;
import com.gtnllib.utils.Vec3d;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Screen-space refraction lenses anchored at a world position and drawn on a camera-facing disc: a lensing hole
 * ({@link #wormhole}), a bright star flash with a needle spike and mist ({@link #pinpointFlash}) and a transparent
 * refracting ring ({@link #ripple}).
 * <p>
 * The builders draw immediately and suit callers that own the timing; call {@code draw()} from a world render pass
 * whose modelview holds only the camera rotation. {@link #spawnRipple} and {@link #spawnFlash} queue managed effects
 * that are aged and drawn automatically during {@link RenderWorldLastEvent}.
 * <p>
 * Refraction copies only the projected rectangle of the lens from the screen, and the refraction offset scales with the
 * lens's on-screen size. Lenses outside the view frustum are skipped. All radii are in blocks and all times in seconds.
 */
@SideOnly(Side.CLIENT)
public class FxSpatialLens {

    public static final int DEFAULT_VOID_RGB = 0x010206;
    public static final int DEFAULT_PRIMARY_RGB = 0x146BF5;
    public static final int DEFAULT_HIGHLIGHT_RGB = 0x75CCFF;
    public static final int DEFAULT_MIST_RGB = 0x4794E0;
    public static final int DEFAULT_RIPPLE_PRIMARY_RGB = 0x47A3F5;
    public static final int DEFAULT_RIPPLE_HIGHLIGHT_RGB = 0xD6F0FF;
    public static final double MIN_HOLE_RATIO = 0.02, MAX_HOLE_RATIO = 0.45;

    private static final int MAX_ACTIVE = 32;
    private static final double REFERENCE_SCREEN_RADIUS = 0.35, MAX_LENS_SCALE = 2.5, COPY_REACH = 2.0,
        NEAR_PLANE = 0.05;
    private static final ShaderEffect SHADER = new ShaderEffect(
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/world_quad.vert.glsl"),
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/spatial_lens.frag.glsl"));
    private static final IntBuffer VIEWPORT = BufferUtils.createIntBuffer(16);
    private static final FloatBuffer MODELVIEW = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);
    private static final List<ManagedEffect> ACTIVE = new ObjectArrayList<>();

    private static int sceneTexture, sceneWidth, sceneHeight;

    public FxSpatialLens() {}

    /**
     * Begins a lensing hole centered at a world position.
     *
     * @param holeRadius radius of the dark hole in blocks; the lens disc defaults to 2.55 times this
     */
    public static Wormhole wormhole(double x, double y, double z, double holeRadius) {
        return new Wormhole(x, y, z, holeRadius);
    }

    /**
     * Begins a lensing hole centered at a world position; a {@code null} center draws nothing.
     *
     * @param holeRadius radius of the dark hole in blocks; the lens disc defaults to 2.55 times this
     */
    public static Wormhole wormhole(Vec3d center, double holeRadius) {
        return center == null ? new Wormhole(0, 0, 0, 0) : new Wormhole(center.x(), center.y(), center.z(), holeRadius);
    }

    /**
     * Begins a star flash with a needle spike and mist. It does not refract and works without a screen copy.
     *
     * @param worldRadius radius of the flash disc in blocks
     */
    public static Flash pinpointFlash(double x, double y, double z, double worldRadius) {
        return new Flash(x, y, z, worldRadius);
    }

    /**
     * Begins a star flash with a needle spike and mist; a {@code null} center draws nothing.
     *
     * @param worldRadius radius of the flash disc in blocks
     */
    public static Flash pinpointFlash(Vec3d center, double worldRadius) {
        return center == null ? new Flash(0, 0, 0, 0) : new Flash(center.x(), center.y(), center.z(), worldRadius);
    }

    /**
     * Begins a transparent refracting ring.
     *
     * @param worldRadius  radius of the disc that holds the ring, in blocks
     * @param ringProgress ring position from the center (0) to the disc edge (1); clamped to 0.06..0.92
     */
    public static Ripple ripple(double x, double y, double z, double worldRadius, float ringProgress) {
        return new Ripple(x, y, z, worldRadius, ringProgress);
    }

    /**
     * Begins a transparent refracting ring; a {@code null} center draws nothing.
     *
     * @param worldRadius  radius of the disc that holds the ring, in blocks
     * @param ringProgress ring position from the center (0) to the disc edge (1); clamped to 0.06..0.92
     */
    public static Ripple ripple(Vec3d center, double worldRadius, float ringProgress) {
        return center == null ? new Ripple(0, 0, 0, 0, ringProgress)
            : new Ripple(center.x(), center.y(), center.z(), worldRadius, ringProgress);
    }

    /**
     * Queues an expanding refracting ring with strength 1.4 and the default ripple colors.
     *
     * @param worldRadius   final disc radius in blocks, above 0.15
     * @param lifetimeTicks lifetime in game ticks, above 0
     */
    public static void spawnRipple(Vec3d center, double worldRadius, int lifetimeTicks) {
        spawnRipple(center, worldRadius, lifetimeTicks, 1.4F, DEFAULT_RIPPLE_PRIMARY_RGB, DEFAULT_RIPPLE_HIGHLIGHT_RGB);
    }

    /**
     * Queues an expanding refracting ring drawn in {@link RenderWorldLastEvent}. The ring grows from the center to the
     * disc edge and fades out over its lifetime. At most 32 managed effects live at once; the oldest is dropped first.
     *
     * @param worldRadius   disc radius in blocks, above 0.15
     * @param lifetimeTicks lifetime in game ticks, above 0
     * @param strength      refraction and tint strength, above 0.01, about 1.5 at most for a natural look
     */
    public static void spawnRipple(Vec3d center, double worldRadius, int lifetimeTicks, float strength, int primaryRgb,
        int highlightRgb) {
        var world = Minecraft.getMinecraft().theWorld;
        if (world == null || center == null
            || !center.finite()
            || !(worldRadius > 0.15)
            || lifetimeTicks <= 0
            || !(strength > 0.01F)) return;
        if (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(
            new ManagedEffect(
                false,
                center.x(),
                center.y(),
                center.z(),
                worldRadius,
                lifetimeTicks,
                world.getTotalWorldTime(),
                strength,
                0F,
                primaryRgb,
                highlightRgb));
    }

    /**
     * Queues a star flash with strength 1.35, full spike and the default mist colors.
     *
     * @param worldRadius   disc radius in blocks, above 0.15
     * @param lifetimeTicks lifetime in game ticks, above 0
     */
    public static void spawnFlash(Vec3d center, double worldRadius, int lifetimeTicks) {
        spawnFlash(center, worldRadius, lifetimeTicks, 1.35F, 1.0F, DEFAULT_MIST_RGB, DEFAULT_HIGHLIGHT_RGB);
    }

    /**
     * Queues a star flash drawn in {@link RenderWorldLastEvent}. It widens by 30% and fades out over its lifetime. At
     * most 32 managed effects live at once; the oldest is dropped first.
     *
     * @param worldRadius   disc radius in blocks, above 0.15
     * @param lifetimeTicks lifetime in game ticks, above 0
     * @param strength      brightness, above 0.01, about 1.5 at most
     * @param spikeAmount   needle spike and mist amount, 0 or more (1 is full)
     */
    public static void spawnFlash(Vec3d center, double worldRadius, int lifetimeTicks, float strength,
        float spikeAmount, int mistRgb, int highlightRgb) {
        var world = Minecraft.getMinecraft().theWorld;
        if (world == null || center == null
            || !center.finite()
            || !(worldRadius > 0.15)
            || lifetimeTicks <= 0
            || !(strength > 0.01F)) return;
        if (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(
            new ManagedEffect(
                true,
                center.x(),
                center.y(),
                center.z(),
                worldRadius,
                lifetimeTicks,
                world.getTotalWorldTime(),
                strength,
                Float.isFinite(spikeAmount) ? Math.max(0F, spikeAmount) : 0F,
                mistRgb,
                highlightRgb));
    }

    /**
     * Removes all queued managed effects.
     */
    public static void clear() {
        ACTIVE.clear();
    }

    /**
     * Releases the cached screen-copy texture; call when unloading a world or reloading resources.
     */
    public static void release() {
        if (sceneTexture != 0) {
            GL11.glDeleteTextures(sceneTexture);
            sceneTexture = 0;
        }
        sceneWidth = 0;
        sceneHeight = 0;
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null) return;
        double now = minecraft.theWorld.getTotalWorldTime() + event.partialTicks;
        ACTIVE.removeIf(fx -> now - fx.born >= fx.lifetime || now < fx.born);
        if (ACTIVE.isEmpty()) return;
        double camX = RenderManager.renderPosX, camY = RenderManager.renderPosY, camZ = RenderManager.renderPosZ;
        for (ManagedEffect fx : ACTIVE) {
            double ageTicks = now - fx.born;
            float ageSec = (float) (ageTicks / 20.0);
            double u = Math.max(0.0, Math.min(1.0, ageTicks / fx.lifetime));
            boolean clear = FxVisibility
                .hasLineOfSight(minecraft, fx.x, fx.y, fx.z, fx.worldRadius * 0.35, camX, camY, camZ);
            if (fx.flash) {
                float fade = (float) Math.pow(1.0 - u, 2.0);
                drawBillboard(
                    fx.x,
                    fx.y,
                    fx.z,
                    fx.worldRadius * (0.85 + 0.30 * u),
                    1F,
                    ageSec,
                    fx.strength * fade,
                    0.25F,
                    0F,
                    fx.spike * fade,
                    clear,
                    camX,
                    camY,
                    camZ,
                    DEFAULT_VOID_RGB,
                    fx.primaryRgb,
                    fx.highlightRgb);
            } else {
                float ringProgress = (float) (0.06 + Math.pow(u, 0.58) * 0.88);
                float fade = (float) Math.pow(1.0 - u, 1.15);
                drawBillboard(
                    fx.x,
                    fx.y,
                    fx.z,
                    fx.worldRadius,
                    2F,
                    ageSec,
                    fx.strength * fade,
                    ringProgress,
                    0F,
                    0F,
                    clear,
                    camX,
                    camY,
                    camZ,
                    DEFAULT_VOID_RGB,
                    fx.primaryRgb,
                    fx.highlightRgb);
            }
        }
    }

    /**
     * Lensing hole: a dark disc ringed by a bright corona, bending the scene around it.
     */
    public static class Wormhole {

        private final double x, y, z, holeRadius;
        private double lensRadius;
        private double cameraX = RenderManager.renderPosX, cameraY = RenderManager.renderPosY,
            cameraZ = RenderManager.renderPosZ;
        private float timeSec, strength = 1F, openTint, openFlash;
        private int voidRgb = DEFAULT_VOID_RGB, primaryRgb = DEFAULT_PRIMARY_RGB, highlightRgb = DEFAULT_HIGHLIGHT_RGB;
        private boolean clearSight = true;

        public Wormhole(double x, double y, double z, double holeRadius) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.holeRadius = holeRadius;
            this.lensRadius = holeRadius * 2.55;
        }

        /**
         * Sets the radius of the whole lens disc in blocks; nothing is drawn at 0.15 or below. The hole-to-lens radius
         * ratio is clamped to {@link FxSpatialLens#MIN_HOLE_RATIO}..{@link FxSpatialLens#MAX_HOLE_RATIO}
         * (0.02..0.45).
         */
        public Wormhole lensRadius(double lensRadius) {
            this.lensRadius = lensRadius;
            return this;
        }

        /**
         * Sets the animation time in seconds; only its change over time matters.
         */
        public Wormhole time(float timeSec) {
            this.timeSec = timeSec;
            return this;
        }

        /**
         * Sets the refraction and corona strength: 0.01 or below draws nothing, 1 is the normal look, up to about 1.5.
         */
        public Wormhole strength(float strength) {
            this.strength = strength;
            return this;
        }

        /**
         * Sets the opening look, each 0 to 1: {@code tint} fills the hole with the primary color instead of the
         * void color, {@code openFlash} adds a bright wash over the whole disc.
         */
        public Wormhole open(float tint, float openFlash) {
            this.openTint = tint;
            this.openFlash = openFlash;
            return this;
        }

        public Wormhole colors(int voidRgb, int primaryRgb, int highlightRgb) {
            this.voidRgb = voidRgb;
            this.primaryRgb = primaryRgb;
            this.highlightRgb = highlightRgb;
            return this;
        }

        /**
         * Draws over blocks when {@code true} (see {@link FxVisibility#hasLineOfSight}), depth tested when
         * {@code false}.
         */
        public Wormhole clearSight(boolean clearSight) {
            this.clearSight = clearSight;
            return this;
        }

        /**
         * Sets the render origin the current modelview is relative to; defaults to the {@link RenderManager} render
         * position.
         */
        public Wormhole camera(double cameraX, double cameraY, double cameraZ) {
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.cameraZ = cameraZ;
            return this;
        }

        public void draw() {
            if (!(lensRadius > 0.15) || !Double.isFinite(holeRadius)) return;
            float holeRatio = (float) Math.max(MIN_HOLE_RATIO, Math.min(MAX_HOLE_RATIO, holeRadius / lensRadius));
            drawBillboard(
                x,
                y,
                z,
                lensRadius,
                0F,
                timeSec,
                strength,
                holeRatio,
                openTint,
                openFlash,
                clearSight,
                cameraX,
                cameraY,
                cameraZ,
                voidRgb,
                primaryRgb,
                highlightRgb);
        }
    }

    /**
     * Star flash with a vertical needle spike, a faint horizontal streak and mist.
     */
    public static class Flash {

        private final double x, y, z, worldRadius;
        private double cameraX = RenderManager.renderPosX, cameraY = RenderManager.renderPosY,
            cameraZ = RenderManager.renderPosZ;
        private float timeSec, strength = 1F, spikeAmount = 1F;
        private int mistRgb = DEFAULT_MIST_RGB, highlightRgb = DEFAULT_HIGHLIGHT_RGB;
        private boolean clearSight = true;

        public Flash(double x, double y, double z, double worldRadius) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.worldRadius = worldRadius;
        }

        /**
         * Sets the animation time in seconds; only its change over time matters.
         */
        public Flash time(float timeSec) {
            this.timeSec = timeSec;
            return this;
        }

        /**
         * Sets the brightness: 0.01 or below draws nothing, 1 is the normal look, up to about 1.5.
         */
        public Flash strength(float strength) {
            this.strength = strength;
            return this;
        }

        /**
         * Sets the needle spike and mist amount, 0 or more (1 is full).
         */
        public Flash spike(float spikeAmount) {
            this.spikeAmount = spikeAmount;
            return this;
        }

        public Flash colors(int mistRgb, int highlightRgb) {
            this.mistRgb = mistRgb;
            this.highlightRgb = highlightRgb;
            return this;
        }

        /**
         * Draws over blocks when {@code true} (see {@link FxVisibility#hasLineOfSight}), depth tested when
         * {@code false}.
         */
        public Flash clearSight(boolean clearSight) {
            this.clearSight = clearSight;
            return this;
        }

        /**
         * Sets the render origin the current modelview is relative to; defaults to the {@link RenderManager} render
         * position.
         */
        public Flash camera(double cameraX, double cameraY, double cameraZ) {
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.cameraZ = cameraZ;
            return this;
        }

        public void draw() {
            drawBillboard(
                x,
                y,
                z,
                worldRadius,
                1F,
                timeSec,
                strength,
                0.25F,
                0F,
                spikeAmount,
                clearSight,
                cameraX,
                cameraY,
                cameraZ,
                DEFAULT_VOID_RGB,
                mistRgb,
                highlightRgb);
        }
    }

    /**
     * Transparent turbulent ring that refracts the scene, with a faint tinted crest.
     */
    public static class Ripple {

        private final double x, y, z, worldRadius;
        private final float ringProgress;
        private double cameraX = RenderManager.renderPosX, cameraY = RenderManager.renderPosY,
            cameraZ = RenderManager.renderPosZ;
        private float timeSec, strength = 1F;
        private int primaryRgb = DEFAULT_RIPPLE_PRIMARY_RGB, highlightRgb = DEFAULT_RIPPLE_HIGHLIGHT_RGB;
        private boolean clearSight = true;

        public Ripple(double x, double y, double z, double worldRadius, float ringProgress) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.worldRadius = worldRadius;
            this.ringProgress = ringProgress;
        }

        /**
         * Sets the animation time in seconds; only its change over time matters.
         */
        public Ripple time(float timeSec) {
            this.timeSec = timeSec;
            return this;
        }

        /**
         * Sets the refraction and tint strength: 0.01 or below draws nothing, 1 is the normal look, up to about 1.5.
         */
        public Ripple strength(float strength) {
            this.strength = strength;
            return this;
        }

        public Ripple colors(int primaryRgb, int highlightRgb) {
            this.primaryRgb = primaryRgb;
            this.highlightRgb = highlightRgb;
            return this;
        }

        /**
         * Draws over blocks when {@code true} (see {@link FxVisibility#hasLineOfSight}), depth tested when
         * {@code false}.
         */
        public Ripple clearSight(boolean clearSight) {
            this.clearSight = clearSight;
            return this;
        }

        /**
         * Sets the render origin the current modelview is relative to; defaults to the {@link RenderManager} render
         * position.
         */
        public Ripple camera(double cameraX, double cameraY, double cameraZ) {
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.cameraZ = cameraZ;
            return this;
        }

        public void draw() {
            drawBillboard(
                x,
                y,
                z,
                worldRadius,
                2F,
                timeSec,
                strength,
                ringProgress,
                0F,
                0F,
                clearSight,
                cameraX,
                cameraY,
                cameraZ,
                DEFAULT_VOID_RGB,
                primaryRgb,
                highlightRgb);
        }
    }

    private static void drawBillboard(double centerX, double centerY, double centerZ, double worldRadius, float mode,
        float timeSec, float strength, float holeRatio, float fillMix, float flashAmount, boolean clearSight,
        double cameraX, double cameraY, double cameraZ, int voidRgb, int primaryRgb, int highlightRgb) {
        if (!(strength > 0.01F) || !(worldRadius > 0.15)) return;
        FxCamera.capture();
        if (!FxCamera.inView(new Vec3d(centerX, centerY, centerZ), worldRadius)) return;
        VIEWPORT.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);
        int vx = VIEWPORT.get(0), vy = VIEWPORT.get(1), vw = VIEWPORT.get(2), vh = VIEWPORT.get(3);
        if (vw <= 0 || vh <= 0) return;

        boolean needsScene = mode < 0.5F || mode >= 1.5F;
        if (needsScene && !OpenGlHelper.shadersSupported) return;

        MODELVIEW.clear();
        PROJECTION.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
        double[] eye = transform(MODELVIEW, centerX - cameraX, centerY - cameraY, centerZ - cameraZ, 1);
        double depth = -eye[2];
        if (depth <= NEAR_PLANE) return;
        double screenRadius = PROJECTION.get(5) * worldRadius / depth * 0.5;
        float lensScale = (float) Math.min(MAX_LENS_SCALE, screenRadius / REFERENCE_SCREEN_RADIUS);
        int[] region = needsScene ? copyRegion(eye, worldRadius * COPY_REACH, vw, vh) : null;
        if (needsScene && region == null) return;

        if (!SHADER.begin()) return;
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_TEXTURE_BIT);
        GL11.glPushMatrix();
        try {
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            if (needsScene) {
                captureScene(vx, vy, vw, vh, region);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                SHADER.uniform2f("copyMin", (region[0] + 0.5F) / vw, (region[1] + 0.5F) / vh);
                SHADER.uniform2f("copyMax", (region[2] + 0.5F) / vw, (region[3] + 0.5F) / vh);
            } else {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
            }
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
            if (clearSight) GL11.glDisable(GL11.GL_DEPTH_TEST);
            else GL11.glEnable(GL11.GL_DEPTH_TEST);

            SHADER.uniform2f("viewOrigin", vx, vy);
            SHADER.uniform2f("viewSize", vw, vh);
            SHADER.uniform1f("time", timeSec);
            SHADER.uniform1f("mode", mode);
            SHADER.uniform1f("strength", strength);
            SHADER.uniform1f("holeRatio", holeRatio);
            SHADER.uniform1f("fillMix", fillMix);
            SHADER.uniform1f("flashAmount", flashAmount);
            SHADER.uniform1f("lensScale", lensScale);
            SHADER.uniformRgb("voidColor", voidRgb);
            SHADER.uniformRgb("primaryColor", primaryRgb);
            SHADER.uniformRgb("highlightColor", highlightRgb);

            GL11.glTranslated(centerX - cameraX, centerY - cameraY, centerZ - cameraZ);
            Tessellator.instance.startDrawingQuads();
            FxRenderHelper.localGlow(Vec3d.ZERO, worldRadius);
            Tessellator.instance.draw();
        } finally {
            SHADER.end();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static int[] copyRegion(double[] eye, double reach, int width, int height) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int corner = 0; corner < 4; corner++) {
            double cornerX = eye[0] + ((corner & 1) == 0 ? -reach : reach);
            double cornerY = eye[1] + ((corner & 2) == 0 ? -reach : reach);
            double[] clip = transform(PROJECTION, cornerX, cornerY, eye[2], 1);
            if (clip[3] <= 1.0E-4) return new int[] { 0, 0, width - 1, height - 1 };
            double windowX = (clip[0] / clip[3] + 1) / 2 * width, windowY = (clip[1] / clip[3] + 1) / 2 * height;
            minX = Math.min(minX, windowX);
            minY = Math.min(minY, windowY);
            maxX = Math.max(maxX, windowX);
            maxY = Math.max(maxY, windowY);
        }
        int x0 = (int) Math.max(0, Math.floor(minX)), y0 = (int) Math.max(0, Math.floor(minY));
        int x1 = (int) Math.min(width - 1, Math.ceil(maxX)), y1 = (int) Math.min(height - 1, Math.ceil(maxY));
        return x0 > x1 || y0 > y1 ? null : new int[] { x0, y0, x1, y1 };
    }

    private static double[] transform(FloatBuffer matrix, double x, double y, double z, double w) {
        double[] result = new double[4];
        for (int row = 0; row < 4; row++) result[row] = matrix.get(row) * x + matrix.get(4 + row) * y
            + matrix.get(8 + row) * z
            + matrix.get(12 + row) * w;
        return result;
    }

    private static void captureScene(int vx, int vy, int vw, int vh, int[] region) {
        if (sceneTexture == 0) {
            sceneTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sceneTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            sceneWidth = 0;
            sceneHeight = 0;
        } else {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sceneTexture);
        }
        if (sceneWidth != vw || sceneHeight != vh) {
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGB8,
                vw,
                vh,
                0,
                GL11.GL_RGB,
                GL11.GL_UNSIGNED_BYTE,
                (ByteBuffer) null);
            sceneWidth = vw;
            sceneHeight = vh;
        }
        int x0 = region[0], y0 = region[1];
        GL11.glCopyTexSubImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            x0,
            y0,
            vx + x0,
            vy + y0,
            region[2] - x0 + 1,
            region[3] - y0 + 1);
    }

    private record ManagedEffect(boolean flash, double x, double y, double z, double worldRadius, int lifetime,
        long born, float strength, float spike, int primaryRgb, int highlightRgb) {}
}
