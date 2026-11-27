package com.gtnllib.client.fx.world;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.gtnllib.GTNLLib;
import com.gtnllib.client.shader.ShaderEffect;
import com.gtnllib.utils.Vec3d;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Reusable expanding mist/vapor wave: an irregular ground-hugging vapor carpet plus a ring of tall camera-facing smoky
 * cloud puffs that drift outward and rise, with configurable colors. All distances are in blocks.
 * <p>
 * Supports an immediate-mode fluent builder ({@link #wave}) for callers that own the timing, and fire-and-forget
 * managed spawning ({@link #spawn}) rendered automatically during {@link RenderWorldLastEvent}.
 */
@SideOnly(Side.CLIENT)
public class FxMistWave {

    public static final int DEFAULT_DARK_RGB = 0x123880;
    public static final int DEFAULT_MID_RGB = 0x3D9EF5;
    public static final int DEFAULT_CREST_RGB = 0xD6F0FF;
    public static final int DEFAULT_PUFFS = 18;
    public static final int MAX_PUFFS = 32;

    private static final int MAX_ACTIVE = 24;
    private static final ShaderEffect SHADER = new ShaderEffect(
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/world_quad.vert.glsl"),
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/mist_wave.frag.glsl"));
    private static final List<ManagedWave> ACTIVE = new ObjectArrayList<>();

    public FxMistWave() {}

    /**
     * Begins configuring an immediate-mode mist wave anchored on the ground at {@code (centerX, groundY, centerZ)}.
     *
     * @param baseRadius radius in blocks of the source the wave starts from; sets the starting puff ring and puff size
     * @param maxRadius  final carpet radius in blocks; must be greater than 0.5 or nothing is drawn
     */
    public static Wave wave(double centerX, double groundY, double centerZ, double baseRadius, double maxRadius) {
        return new Wave(centerX, groundY, centerZ, baseRadius, maxRadius);
    }

    /**
     * Same as {@link #wave(double, double, double, double, double)}; a {@code null} center gives a wave that draws
     * nothing.
     */
    public static Wave wave(Vec3d groundCenter, double baseRadius, double maxRadius) {
        return groundCenter == null ? new Wave(0, 0, 0, 0, 0)
            : new Wave(groundCenter.x(), groundCenter.y(), groundCenter.z(), baseRadius, maxRadius);
    }

    /**
     * Spawns a fire-and-forget mist wave with default puffs, strength and colors.
     *
     * @param baseRadius    starting radius in blocks
     * @param maxRadius     final radius in blocks, greater than 0.5
     * @param lifetimeTicks lifetime in ticks, positive
     */
    public static void spawn(Vec3d groundCenter, double baseRadius, double maxRadius, int lifetimeTicks) {
        spawn(
            groundCenter,
            baseRadius,
            maxRadius,
            lifetimeTicks,
            DEFAULT_PUFFS,
            1.0F,
            DEFAULT_DARK_RGB,
            DEFAULT_MID_RGB,
            DEFAULT_CREST_RGB);
    }

    /**
     * Spawns a fire-and-forget mist wave rendered automatically in {@link RenderWorldLastEvent}. At most 24 managed
     * waves exist at once; spawning beyond that drops the oldest.
     *
     * @param baseRadius    starting radius in blocks
     * @param maxRadius     final radius in blocks, greater than 0.5
     * @param lifetimeTicks lifetime in ticks, positive
     * @param puffCount     cloud puffs in the ring, clamped to [0, {@link #MAX_PUFFS}]
     * @param strength      opacity multiplier, around 1; values at or below 0.01 spawn nothing
     */
    public static void spawn(Vec3d groundCenter, double baseRadius, double maxRadius, int lifetimeTicks, int puffCount,
        float strength, int darkRgb, int midRgb, int crestRgb) {
        var world = Minecraft.getMinecraft().theWorld;
        if (world == null || groundCenter == null
            || !groundCenter.finite()
            || !(baseRadius >= 0)
            || !(maxRadius > 0.5)
            || lifetimeTicks <= 0
            || !(strength > 0.01F)) return;
        if (ACTIVE.size() >= MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(
            new ManagedWave(
                groundCenter.x(),
                groundCenter.y(),
                groundCenter.z(),
                baseRadius,
                maxRadius,
                puffCount,
                lifetimeTicks,
                world.getTotalWorldTime(),
                strength,
                darkRgb,
                midRgb,
                crestRgb));
    }

    /**
     * Clears all active managed mist waves.
     */
    public static void clear() {
        ACTIVE.clear();
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        var world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;
        double now = world.getTotalWorldTime() + event.partialTicks;
        ACTIVE.removeIf(w -> now - w.born >= w.lifetime || now < w.born);
        if (ACTIVE.isEmpty()) return;
        double camX = RenderManager.renderPosX, camY = RenderManager.renderPosY, camZ = RenderManager.renderPosZ;
        for (ManagedWave w : ACTIVE) {
            double ageTicks = now - w.born;
            float progress = (float) Math.pow(Math.max(0.0, Math.min(1.0, ageTicks / w.lifetime)), 0.72);
            drawInternal(
                w.x,
                w.y,
                w.z,
                w.baseRadius,
                w.maxRadius,
                w.puffCount,
                (float) (ageTicks / 20.0),
                progress,
                w.strength,
                camX,
                camY,
                camZ,
                w.darkRgb,
                w.midRgb,
                w.crestRgb);
        }
    }

    /**
     * Fluent configuration for one immediate-mode mist wave frame. The caller owns timing and calls {@link #draw()}
     * once per frame inside world rendering.
     */
    public static class Wave {

        private final double centerX, groundY, centerZ, baseRadius, maxRadius;
        private double cameraX = RenderManager.renderPosX, cameraY = RenderManager.renderPosY,
            cameraZ = RenderManager.renderPosZ;
        private int puffCount = DEFAULT_PUFFS;
        private float timeSec, progress, strength = 1F;
        private int darkRgb = DEFAULT_DARK_RGB, midRgb = DEFAULT_MID_RGB, crestRgb = DEFAULT_CREST_RGB;

        public Wave(double centerX, double groundY, double centerZ, double baseRadius, double maxRadius) {
            this.centerX = centerX;
            this.groundY = groundY;
            this.centerZ = centerZ;
            this.baseRadius = baseRadius;
            this.maxRadius = maxRadius;
        }

        /**
         * Sets the number of cloud puffs in the ring, clamped to [0, {@link #MAX_PUFFS}]; 0 draws only the carpet.
         */
        public Wave puffs(int puffCount) {
            this.puffCount = puffCount;
            return this;
        }

        /**
         * Sets the noise animation clock in seconds. Keep it small (an effect-local age, not the world time) so float
         * precision stays fine.
         */
        public Wave time(float timeSec) {
            this.timeSec = timeSec;
            return this;
        }

        /**
         * Sets how far the wave has expanded and faded, in [0, 1). Values outside [0, 1) draw nothing.
         */
        public Wave progress(float progress) {
            this.progress = progress;
            return this;
        }

        /**
         * Sets the opacity multiplier, default 1; values at or below 0.01 draw nothing.
         */
        public Wave strength(float strength) {
            this.strength = strength;
            return this;
        }

        public Wave colors(int darkRgb, int midRgb, int crestRgb) {
            this.darkRgb = darkRgb;
            this.midRgb = midRgb;
            this.crestRgb = crestRgb;
            return this;
        }

        /**
         * Sets the interpolated camera position the current modelview is relative to; defaults to the render manager
         * position at builder creation.
         */
        public Wave camera(double cameraX, double cameraY, double cameraZ) {
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.cameraZ = cameraZ;
            return this;
        }

        public void draw() {
            drawInternal(
                centerX,
                groundY,
                centerZ,
                baseRadius,
                maxRadius,
                puffCount,
                timeSec,
                progress,
                strength,
                cameraX,
                cameraY,
                cameraZ,
                darkRgb,
                midRgb,
                crestRgb);
        }
    }

    private static void drawInternal(double centerX, double groundY, double centerZ, double baseRadius,
        double maxRadius, int puffCount, float timeSec, float progress, float strength, double cameraX, double cameraY,
        double cameraZ, int darkRgb, int midRgb, int crestRgb) {
        if (!(strength > 0.01F) || !(progress >= 0F && progress < 1F) || !(maxRadius > 0.5)) return;
        if (!SHADER.begin()) return;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(centerX - cameraX, groundY - cameraY, centerZ - cameraZ);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            SHADER.uniform1f("time", timeSec);
            SHADER.uniform1f("progress", progress);
            SHADER.uniformRgb("darkColor", darkRgb);
            SHADER.uniformRgb("midColor", midRgb);
            SHADER.uniformRgb("crestColor", crestRgb);

            SHADER.uniform1f("mode", 0F);
            SHADER.uniform1f("strength", 0.90F * strength);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.addVertexWithUV(-maxRadius, 0.14, -maxRadius, 0, 0);
            t.addVertexWithUV(-maxRadius, 0.14, maxRadius, 0, 1);
            t.addVertexWithUV(maxRadius, 0.14, maxRadius, 1, 1);
            t.addVertexWithUV(maxRadius, 0.14, -maxRadius, 1, 0);
            t.draw();

            int puffs = Math.max(0, Math.min(MAX_PUFFS, puffCount));
            if (puffs > 0) {
                SHADER.uniform1f("mode", 1F);
                SHADER.uniform1f("strength", 0.92F * strength);
                double safeBase = Double.isFinite(baseRadius) ? Math.max(0.0, baseRadius) : 0.0;
                double baseRingR = safeBase * 0.55 + progress * Math.max(1.0, maxRadius - safeBase * 0.55);
                t.startDrawingQuads();
                for (int i = 0; i < puffs; i++) {
                    double seed = Math.sin(i * 12.9898 + 78.233) * 43758.5453;
                    double frac = seed - Math.floor(seed);
                    double ang = (i + (frac - 0.5) * 0.45) * Math.PI * 2 / puffs;
                    double pR = baseRingR * (0.82 + 0.34 * frac);
                    double puffSize = Math.max(2.2, safeBase * 0.42 + 1.4) * (0.75 + 0.65 * progress)
                        * (0.75 + 0.5 * frac);
                    double py = puffSize * (0.48 + 0.25 * progress) + frac * 1.2;
                    FxRenderHelper.localGlow(new Vec3d(Math.cos(ang) * pR, py, Math.sin(ang) * pR), puffSize);
                }
                t.draw();
            }
        } finally {
            SHADER.end();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private record ManagedWave(double x, double y, double z, double baseRadius, double maxRadius, int puffCount,
        int lifetime, long born, float strength, int darkRgb, int midRgb, int crestRgb) {}
}
