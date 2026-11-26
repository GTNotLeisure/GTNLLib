package com.gtnllib.client.fx.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.gtnllib.GTNLLib;
import com.gtnllib.client.fx.screen.FxGlow;
import com.gtnllib.client.shader.ShaderEffect;
import com.gtnllib.utils.ColorMath;
import com.gtnllib.utils.Vec3d;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Volumetric fractal branching lightning ribbons rendered with a procedural plasma shader. Positions and widths are in
 * world blocks; ages and lifetimes are in ticks.
 * <p>
 * Can be instantiated as a per-owner pool ({@code new FxFractalLightning(maxBolts)}) for tight draw-order / bloom
 * synchronization, or fired directly into the world via {@link #spawnWorld} for fire-and-forget effects rendered
 * automatically in {@link RenderWorldLastEvent}.
 * <p>
 * A pool runs on its owner's clock: every {@code age} passed to {@link #spawn}, {@link #prune}, {@link #draw} and
 * {@link #drawGlowCores} is the owner's current age in ticks (fractional ticks allowed), counted from a recent start
 * such as the effect's own begin, not the world time. The owner must call {@link #prune(double)} each tick or frame so
 * expired bolts free their slots; a full pool drops its oldest bolts to make room for new ones.
 */
@SideOnly(Side.CLIENT)
public class FxFractalLightning {

    public static final int DEFAULT_PRIMARY_RGB = 0x0F66FF;
    public static final int DEFAULT_SECONDARY_RGB = 0x57C7FF;
    public static final int DEFAULT_CORE_RGB = 0xFFFFFF;
    public static final int DEFAULT_GLOW_RGB = 0x52BDFF;

    private static final int MAX_WORLD_BOLTS = 96;
    private static final ShaderEffect SHADER = new ShaderEffect(
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/world_quad.vert.glsl"),
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/fractal_lightning.frag.glsl"));
    private static final Random RANDOM = new Random();
    private static final List<Bolt> WORLD_BOLTS = new ArrayList<>();

    private final List<Bolt> bolts = new ArrayList<>();
    private final int maxBolts;

    /**
     * Creates a pool holding at most {@code maxBolts} ribbons (at least 1); each trunk and each branch takes one slot.
     */
    public FxFractalLightning(int maxBolts) {
        this.maxBolts = Math.max(1, maxBolts);
    }

    /**
     * Spawns a fire-and-forget fractal lightning bolt between two world points with default colors.
     *
     * @param lifeTicks lifetime in ticks, positive
     */
    public static void spawnWorld(Vec3d from, Vec3d to, int lifeTicks) {
        spawnWorld(from, to, lifeTicks, DEFAULT_PRIMARY_RGB, DEFAULT_SECONDARY_RGB, DEFAULT_CORE_RGB);
    }

    /**
     * Spawns a fire-and-forget fractal lightning bolt between two world points with custom RGB colors.
     *
     * @param lifeTicks lifetime in ticks, positive
     */
    public static void spawnWorld(Vec3d from, Vec3d to, int lifeTicks, int primaryRgb, int secondaryRgb, int coreRgb) {
        if (from == null || to == null) return;
        double minY = Math.min(from.y(), to.y()) - 2.0;
        spawnWorld(from, to, 4, 0.42, 0.28F, lifeTicks, 1, minY, primaryRgb, secondaryRgb, coreRgb);
    }

    /**
     * Spawns a fire-and-forget fractal lightning bolt with full geometric and palette control. At most 96 managed
     * ribbons exist at once; spawning beyond that drops the oldest.
     *
     * @param depth        midpoint subdivision depth, clamped to [1, 5] ({@code 2^depth} segments)
     * @param roughness    sideways jitter as a fraction of each segment's length, around 0.3 to 0.6
     * @param rootWidth    minimum trunk half-width in blocks, positive; ribbons widen with camera distance
     * @param lifeTicks    lifetime in ticks, positive; branches live 85% as long
     * @param branchLevels recursive branch levels, 0 for a single trunk
     * @param minFloorY    lowest world Y a branch end may reach
     */
    public static void spawnWorld(Vec3d from, Vec3d to, int depth, double roughness, float rootWidth, int lifeTicks,
        int branchLevels, double minFloorY, int primaryRgb, int secondaryRgb, int coreRgb) {
        var world = Minecraft.getMinecraft().theWorld;
        if (world == null || from == null
            || to == null
            || !from.finite()
            || !to.finite()
            || lifeTicks <= 0
            || !(rootWidth > 0)) return;
        addBolt(
            WORLD_BOLTS,
            MAX_WORLD_BOLTS,
            from.x(),
            from.y(),
            from.z(),
            to.x(),
            to.y(),
            to.z(),
            depth,
            roughness,
            rootWidth,
            world.getTotalWorldTime(),
            lifeTicks,
            branchLevels,
            minFloorY,
            primaryRgb,
            secondaryRgb,
            coreRgb);
    }

    /**
     * Clears all fire-and-forget world lightning bolts.
     */
    public static void clearWorld() {
        WORLD_BOLTS.clear();
    }

    public boolean isEmpty() {
        return bolts.isEmpty();
    }

    public void clear() {
        bolts.clear();
    }

    /**
     * Removes bolts whose lifetime has passed, or that were born after {@code age}; call it every tick or frame.
     *
     * @param age the owner's current age in ticks
     */
    public void prune(double age) {
        pruneList(bolts, age);
    }

    /**
     * Same as {@link #spawn(double, double, double, double, double, double, int, double, float, double, double, int,
     * double)}; ignores {@code null} or non-finite points.
     */
    public void spawn(Vec3d from, Vec3d to, int depth, double roughness, float rootWidth, double age, double lifeTicks,
        int branchLevels, double minFloorY) {
        if (from == null || to == null || !from.finite() || !to.finite()) return;
        spawn(
            from.x(),
            from.y(),
            from.z(),
            to.x(),
            to.y(),
            to.z(),
            depth,
            roughness,
            rootWidth,
            age,
            lifeTicks,
            branchLevels,
            minFloorY);
    }

    /**
     * Spawns a fractal lightning trunk and optional recursive sub-branches between two world points. When the pool is
     * full the oldest bolts are dropped.
     *
     * @param depth        midpoint subdivision depth, clamped to [1, 5] ({@code 2^depth} segments)
     * @param roughness    sideways jitter as a fraction of each segment's length, around 0.3 to 0.6
     * @param rootWidth    minimum trunk half-width in blocks, positive; ribbons widen with camera distance
     * @param age          the owner's current age in ticks; the bolt is born now
     * @param lifeTicks    lifetime in ticks, positive; branches live 85% as long
     * @param branchLevels recursive branch levels, 0 for a single trunk
     * @param minFloorY    lowest world Y a branch end may reach
     */
    public void spawn(double x0, double y0, double z0, double x1, double y1, double z1, int depth, double roughness,
        float rootWidth, double age, double lifeTicks, int branchLevels, double minFloorY) {
        addBolt(
            bolts,
            maxBolts,
            x0,
            y0,
            z0,
            x1,
            y1,
            z1,
            depth,
            roughness,
            rootWidth,
            age,
            lifeTicks,
            branchLevels,
            minFloorY,
            DEFAULT_PRIMARY_RGB,
            DEFAULT_SECONDARY_RGB,
            DEFAULT_CORE_RGB);
    }

    public void draw(double age, float strength) {
        draw(
            age,
            RenderManager.renderPosX,
            RenderManager.renderPosY,
            RenderManager.renderPosZ,
            strength,
            DEFAULT_PRIMARY_RGB,
            DEFAULT_SECONDARY_RGB,
            DEFAULT_CORE_RGB);
    }

    public void draw(double age, float strength, int primaryRgb, int secondaryRgb, int coreRgb) {
        draw(
            age,
            RenderManager.renderPosX,
            RenderManager.renderPosY,
            RenderManager.renderPosZ,
            strength,
            primaryRgb,
            secondaryRgb,
            coreRgb);
    }

    public void draw(double age, double cameraX, double cameraY, double cameraZ, float strength) {
        draw(age, cameraX, cameraY, cameraZ, strength, DEFAULT_PRIMARY_RGB, DEFAULT_SECONDARY_RGB, DEFAULT_CORE_RGB);
    }

    /**
     * Draws all active bolts in world space as mitered camera-facing tapered quad ribbons with custom RGB palette. The
     * overloads without a camera use the render manager position; the ones without colors use the defaults.
     *
     * @param age      the owner's current age in ticks, on the same clock as {@link #spawn}; also drives the shader
     *                 animation, so it must stay small
     * @param strength opacity multiplier, around 1; at or below 0.01 nothing is drawn
     */
    public void draw(double age, double cameraX, double cameraY, double cameraZ, float strength, int primaryRgb,
        int secondaryRgb, int coreRgb) {
        drawList(bolts, age, cameraX, cameraY, cameraZ, strength, false, primaryRgb, secondaryRgb, coreRgb);
    }

    /**
     * Emits slim untextured core ribbons into an active {@link FxGlow} pass. The caller must already have translated
     * by {@code -cameraX, -cameraY, -cameraZ}.
     *
     * @param age        the owner's current age in ticks
     * @param widthScale ribbon width relative to {@link #draw}, such as 0.36
     */
    public void drawGlowCores(double age, double cameraX, double cameraY, double cameraZ, float widthScale,
        int glowRgb) {
        if (bolts.isEmpty()) return;
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(ColorMath.red(glowRgb), ColorMath.green(glowRgb), ColorMath.blue(glowRgb), 1F);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            for (int b = 0; b < bolts.size(); b++)
                emitBoltQuads(t, bolts.get(b), b, age, cameraX, cameraY, cameraZ, widthScale);
            t.draw();
        } finally {
            GL11.glPopAttrib();
        }
    }

    public static class WorldRenderer {

        public WorldRenderer() {}

        @SubscribeEvent
        public void render(RenderWorldLastEvent event) {
            var world = Minecraft.getMinecraft().theWorld;
            if (world == null) return;
            double now = world.getTotalWorldTime() + event.partialTicks;
            pruneList(WORLD_BOLTS, now);
            if (WORLD_BOLTS.isEmpty()) return;
            drawList(
                WORLD_BOLTS,
                now,
                RenderManager.renderPosX,
                RenderManager.renderPosY,
                RenderManager.renderPosZ,
                1.0F,
                true,
                DEFAULT_PRIMARY_RGB,
                DEFAULT_SECONDARY_RGB,
                DEFAULT_CORE_RGB);
        }
    }

    private static void pruneList(List<Bolt> list, double age) {
        Iterator<Bolt> it = list.iterator();
        while (it.hasNext()) {
            Bolt bolt = it.next();
            double elapsed = age - bolt.bornAge;
            if (elapsed >= bolt.lifeTicks || elapsed < 0) it.remove();
        }
    }

    private static void addBolt(List<Bolt> list, int limit, double x0, double y0, double z0, double x1, double y1,
        double z1, int depth, double roughness, float rootWidth, double age, double lifeTicks, int branchLevels,
        double minFloorY, int primaryRgb, int secondaryRgb, int coreRgb) {
        if (!(lifeTicks > 0) || !(rootWidth > 0) || !Double.isFinite(age)) return;
        List<Bolt> spawned = new ArrayList<>();
        growBolt(
            spawned,
            limit,
            x0,
            y0,
            z0,
            x1,
            y1,
            z1,
            depth,
            roughness,
            rootWidth,
            age,
            lifeTicks,
            branchLevels,
            minFloorY,
            primaryRgb,
            secondaryRgb,
            coreRgb);
        int overflow = Math.min(list.size(), list.size() + spawned.size() - limit);
        if (overflow > 0) list.subList(0, overflow)
            .clear();
        list.addAll(spawned);
    }

    private static void growBolt(List<Bolt> list, int limit, double x0, double y0, double z0, double x1, double y1,
        double z1, int depth, double roughness, float rootWidth, double age, double lifeTicks, int branchLevels,
        double minFloorY, int primaryRgb, int secondaryRgb, int coreRgb) {
        if (list.size() >= limit) return;
        int clampedDepth = Math.max(1, Math.min(5, depth));
        int segs = 1 << clampedDepth;
        double[] pts = new double[(segs + 1) * 3];
        pts[0] = x0;
        pts[1] = y0;
        pts[2] = z0;
        pts[segs * 3] = x1;
        pts[segs * 3 + 1] = y1;
        pts[segs * 3 + 2] = z1;
        subdivide(pts, 0, segs, roughness);
        list.add(new Bolt(pts, rootWidth, age, lifeTicks, primaryRgb, secondaryRgb, coreRgb));

        if (branchLevels <= 0 || list.size() >= limit) return;
        int branches = branchLevels >= 2 ? 2 + RANDOM.nextInt(2) : 1 + RANDOM.nextInt(2);
        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        double totalLen = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (totalLen < 0.4) return;
        double ux = dx / totalLen, uy = dy / totalLen, uz = dz / totalLen;
        int margin = Math.min(2, segs / 2);
        for (int b = 0; b < branches; b++) {
            if (list.size() >= limit) break;
            int idx = Math.max(margin, Math.min(segs - margin, (int) ((0.22 + RANDOM.nextDouble() * 0.56) * segs)));
            double bx0 = pts[idx * 3], by0 = pts[idx * 3 + 1], bz0 = pts[idx * 3 + 2];
            double bLen = totalLen * (0.28 + RANDOM.nextDouble() * 0.30);
            double bx1 = bx0 + (ux * 0.65 + (RANDOM.nextDouble() - 0.5) * 1.15) * bLen;
            double by1 = Math.max(minFloorY, by0 + (uy * 0.65 + (RANDOM.nextDouble() - 0.5) * 1.05) * bLen);
            double bz1 = bz0 + (uz * 0.65 + (RANDOM.nextDouble() - 0.5) * 1.15) * bLen;
            growBolt(
                list,
                limit,
                bx0,
                by0,
                bz0,
                bx1,
                by1,
                bz1,
                Math.max(2, clampedDepth - 1),
                roughness * 1.12,
                rootWidth * 0.54F,
                age,
                lifeTicks * 0.85,
                branchLevels - 1,
                minFloorY,
                primaryRgb,
                secondaryRgb,
                coreRgb);
        }
    }

    private static void drawList(List<Bolt> list, double age, double cameraX, double cameraY, double cameraZ,
        float strength, boolean managed, int primaryRgb, int secondaryRgb, int coreRgb) {
        if (list.isEmpty() || !(strength > 0.01F)) return;
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(-cameraX, -cameraY, -cameraZ);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            if (SHADER.begin()) {
                try {
                    SHADER.uniform1f("strength", strength);
                    if (!managed) {
                        SHADER.uniform1f("time", (float) (age / 20.0));
                        SHADER.uniformRgb("primaryColor", primaryRgb);
                        SHADER.uniformRgb("secondaryColor", secondaryRgb);
                        SHADER.uniformRgb("coreColor", coreRgb);
                        Tessellator t = Tessellator.instance;
                        t.startDrawingQuads();
                        for (int b = 0; b < list.size(); b++)
                            emitBoltQuads(t, list.get(b), b, age, cameraX, cameraY, cameraZ, 1.0F);
                        t.draw();
                    } else {
                        drawGroupedByPalette(list, age, cameraX, cameraY, cameraZ, true);
                    }
                } finally {
                    SHADER.end();
                }
            } else if (!managed) {
                GL11.glColor4f(
                    ColorMath.red(secondaryRgb),
                    ColorMath.green(secondaryRgb),
                    ColorMath.blue(secondaryRgb),
                    0.9F);
                Tessellator t = Tessellator.instance;
                t.startDrawingQuads();
                for (int b = 0; b < list.size(); b++)
                    emitBoltQuads(t, list.get(b), b, age, cameraX, cameraY, cameraZ, 0.45F);
                t.draw();
            } else {
                drawGroupedByPalette(list, age, cameraX, cameraY, cameraZ, false);
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void drawGroupedByPalette(List<Bolt> list, double age, double cameraX, double cameraY,
        double cameraZ, boolean useShader) {
        Tessellator t = Tessellator.instance;
        int curPrimary = 0, curSecondary = 0, curCore = 0;
        double curBorn = 0;
        boolean started = false;
        for (int b = 0; b < list.size(); b++) {
            Bolt bolt = list.get(b);
            if (!started || bolt.primaryRgb != curPrimary
                || bolt.secondaryRgb != curSecondary
                || bolt.coreRgb != curCore
                || (useShader && bolt.bornAge != curBorn)) {
                if (started) t.draw();
                curPrimary = bolt.primaryRgb;
                curSecondary = bolt.secondaryRgb;
                curCore = bolt.coreRgb;
                curBorn = bolt.bornAge;
                if (useShader) {
                    SHADER.uniform1f("time", (float) ((age - curBorn) / 20.0));
                    SHADER.uniformRgb("primaryColor", curPrimary);
                    SHADER.uniformRgb("secondaryColor", curSecondary);
                    SHADER.uniformRgb("coreColor", curCore);
                } else {
                    GL11.glColor4f(
                        ColorMath.red(curSecondary),
                        ColorMath.green(curSecondary),
                        ColorMath.blue(curSecondary),
                        0.9F);
                }
                t.startDrawingQuads();
                started = true;
            }
            emitBoltQuads(t, bolt, b, age, cameraX, cameraY, cameraZ, useShader ? 1.0F : 0.45F);
        }
        if (started) t.draw();
    }

    private static void emitBoltQuads(Tessellator t, Bolt bolt, int index, double age, double cameraX, double cameraY,
        double cameraZ, float widthScale) {
        double lifeU = (age - bolt.bornAge) / bolt.lifeTicks;
        if (lifeU < 0 || lifeU >= 1) return;
        double flicker = 0.72 + 0.28 * Math.sin(age * 4.8 + index * 2.17);
        double lifeMul = Math.pow(1.0 - lifeU, 1.15) * flicker;
        double[] pts = bolt.pts;
        int count = pts.length / 3;
        if (count < 2) return;

        double prevLx = 0, prevLy = 0, prevLz = 0, prevRx = 0, prevRy = 0, prevRz = 0, prevU = 0;
        for (int i = 0; i < count; i++) {
            double px = pts[i * 3], py = pts[i * 3 + 1], pz = pts[i * 3 + 2];
            int iPrev = Math.max(0, i - 1), iNext = Math.min(count - 1, i + 1);
            double tx = pts[iNext * 3] - pts[iPrev * 3];
            double ty = pts[iNext * 3 + 1] - pts[iPrev * 3 + 1];
            double tz = pts[iNext * 3 + 2] - pts[iPrev * 3 + 2];
            double vx = cameraX - px, vy = cameraY - py, vz = cameraZ - pz;

            double sx = ty * vz - tz * vy;
            double sy = tz * vx - tx * vz;
            double sz = tx * vy - ty * vx;
            double sLen = Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (sLen < 1.0E-5) {
                sx = 0;
                sy = 1;
                sz = 0;
            } else {
                sx /= sLen;
                sy /= sLen;
                sz /= sLen;
            }

            double u = (double) i / (count - 1);
            double camDist = Math.sqrt(vx * vx + vy * vy + vz * vz);
            double adaptiveW = Math.max(bolt.rootWidth, camDist * 0.015);
            double halfW = adaptiveW * widthScale * (1.0 - 0.68 * u) * (0.48 + 0.62 * lifeMul);
            double lx = px - sx * halfW, ly = py - sy * halfW, lz = pz - sz * halfW;
            double rx = px + sx * halfW, ry = py + sy * halfW, rz = pz + sz * halfW;
            if (i > 0) {
                t.addVertexWithUV(prevLx, prevLy, prevLz, prevU, 0);
                t.addVertexWithUV(prevRx, prevRy, prevRz, prevU, 1);
                t.addVertexWithUV(rx, ry, rz, u, 1);
                t.addVertexWithUV(lx, ly, lz, u, 0);
            }
            prevLx = lx;
            prevLy = ly;
            prevLz = lz;
            prevRx = rx;
            prevRy = ry;
            prevRz = rz;
            prevU = u;
        }
    }

    private static void subdivide(double[] pts, int lo, int hi, double roughness) {
        if (hi - lo <= 1) return;
        int mid = (lo + hi) >>> 1;
        double ax = pts[lo * 3], ay = pts[lo * 3 + 1], az = pts[lo * 3 + 2];
        double bx = pts[hi * 3], by = pts[hi * 3 + 1], bz = pts[hi * 3 + 2];
        double dx = bx - ax, dy = by - ay, dz = bz - az;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double mx = (ax + bx) * 0.5, my = (ay + by) * 0.5, mz = (az + bz) * 0.5;
        if (len > 1.0E-4) {
            double ux = dx / len, uy = dy / len, uz = dz / len;
            double rx, ry, rz;
            if (Math.abs(uy) < 0.9) {
                rx = -uz;
                ry = 0;
                rz = ux;
            } else {
                rx = 0;
                ry = uz;
                rz = -uy;
            }
            double rl = Math.sqrt(rx * rx + ry * ry + rz * rz);
            rx /= rl;
            ry /= rl;
            rz /= rl;
            double vx = uy * rz - uz * ry;
            double vy = uz * rx - ux * rz;
            double vz = ux * ry - uy * rx;
            double s1 = (RANDOM.nextDouble() - 0.5) * len * roughness;
            double s2 = (RANDOM.nextDouble() - 0.5) * len * roughness;
            mx += rx * s1 + vx * s2;
            my += ry * s1 + vy * s2;
            mz += rz * s1 + vz * s2;
        }
        pts[mid * 3] = mx;
        pts[mid * 3 + 1] = my;
        pts[mid * 3 + 2] = mz;
        subdivide(pts, lo, mid, roughness * 0.92);
        subdivide(pts, mid, hi, roughness * 0.92);
    }

    private static class Bolt {

        final double[] pts;
        final float rootWidth;
        final double bornAge, lifeTicks;
        final int primaryRgb, secondaryRgb, coreRgb;

        Bolt(double[] pts, float rootWidth, double bornAge, double lifeTicks, int primaryRgb, int secondaryRgb,
            int coreRgb) {
            this.pts = pts;
            this.rootWidth = rootWidth;
            this.bornAge = bornAge;
            this.lifeTicks = lifeTicks;
            this.primaryRgb = primaryRgb;
            this.secondaryRgb = secondaryRgb;
            this.coreRgb = coreRgb;
        }
    }
}
