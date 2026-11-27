package com.gtnllib.client.fx.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.gtnllib.GTNLLib;
import com.gtnllib.client.fx.screen.FxGlow;
import com.gtnllib.client.shader.ShaderEffect;
import com.gtnllib.utils.ColorMath;
import com.gtnllib.utils.Vec3d;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Reusable shader passes for crystallizing/materializing 3D models (block meshes, OBJ models, or custom geometry):
 * flowing wireframe outlines, procedural 3D-noise energy surface weaving, and whole-structure optical bloom submission
 * for {@link FxGlow}.
 * <p>
 * The builders only hold parameters; the caller owns timing and calls a draw method once per frame. Positions and
 * sizes are in blocks, in the mesh's local coordinates unless stated otherwise.
 */
@SideOnly(Side.CLIENT)
public class FxCrystalMesh {

    public static final int DEFAULT_SHADOW_RGB = 0x0F2952;
    public static final int DEFAULT_PRIMARY_RGB = 0x5294D6;
    public static final int DEFAULT_HIGHLIGHT_RGB = 0xD6F0FF;
    public static final int DEFAULT_WIRE_PRIMARY_RGB = 0x38ADFF;

    private static final ShaderEffect SURFACE = new ShaderEffect(
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/crystal_surface.vert.glsl"),
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/crystal_surface.frag.glsl"));
    private static final ShaderEffect WIRE = new ShaderEffect(
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/crystal_wire.vert.glsl"),
        new ResourceLocation(GTNLLib.MODID, "shaders/fx/crystal_wire.frag.glsl"));

    private FxCrystalMesh() {}

    /**
     * Begins configuring a procedural carved-crystal materialization surface pass around {@code drawMesh}.
     */
    public static Surface surface(Runnable drawMesh) {
        return new Surface(drawMesh);
    }

    /**
     * Begins configuring a flowing wireframe or whole-mesh glow pass around {@code drawGeometry}.
     */
    public static Wire wire(Runnable drawGeometry) {
        return new Wire(drawGeometry);
    }

    /**
     * Fluent configuration for {@link #drawSurface(Surface)}. Defaults to the Minecraft block texture atlas with full
     * {@link #sheen(float) sheen}; call {@link #texture(ResourceLocation) texture(null)} to keep the caller's bound
     * texture, and {@link #sheen(float) sheen(0)} for a plain noise weave.
     */
    public static class Surface {

        private final Runnable drawMesh;
        private double translateX, translateY, translateZ;
        private float localCenterX, localCenterY, localCenterZ, sizeX = 1F, sizeY = 1F, sizeZ = 1F;
        private float timeSec, fill = 1F, whiteout, sheen = 1F, reveal = 1F;
        private int shadowRgb = DEFAULT_SHADOW_RGB, primaryRgb = DEFAULT_PRIMARY_RGB,
            highlightRgb = DEFAULT_HIGHLIGHT_RGB;
        private ResourceLocation texture = TextureMap.locationBlocksTexture;

        public Surface(Runnable drawMesh) {
            this.drawMesh = drawMesh;
        }

        /**
         * Sets the translation in blocks applied before drawing the mesh, relative to the current modelview.
         */
        public Surface at(double x, double y, double z) {
            this.translateX = x;
            this.translateY = y;
            this.translateZ = z;
            return this;
        }

        public Surface at(Vec3d offset) {
            return offset == null ? this : at(offset.x(), offset.y(), offset.z());
        }

        /**
         * Sets the mesh's center and size in local blocks; {@link #reveal(float) reveal} grows from this center and
         * the whiteout surges strongest at its height.
         */
        public Surface bounds(float localCenterX, float localCenterY, float localCenterZ, float sizeX, float sizeY,
            float sizeZ) {
            this.localCenterX = localCenterX;
            this.localCenterY = localCenterY;
            this.localCenterZ = localCenterZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            return this;
        }

        /**
         * Limits the crystal to an ellipsoid growing from the {@link #bounds bounds} center, in [0, 1] (default 1, no
         * limit). Its front is drawn as a bright band that fades out near 1; at 1 it has passed every corner. Without
         * shader support the whole surface is drawn regardless.
         */
        public Surface reveal(float reveal) {
            this.reveal = reveal;
            return this;
        }

        /**
         * Sets the noise animation clock in seconds. Keep it small (an effect-local age) so float precision stays fine.
         */
        public Surface time(float timeSec) {
            this.timeSec = timeSec;
            return this;
        }

        /**
         * Sets how much of the surface the noise weave covers, in [0, 1] (default 1). At or below 0.01 nothing is
         * drawn; from 0.65 up the surface also writes depth.
         */
        public Surface fill(float fill) {
            this.fill = fill;
            return this;
        }

        /**
         * Blends the whole surface toward pure white and raises its opacity, in [0, 1] (default 0).
         */
        public Surface whiteout(float whiteout) {
            this.whiteout = whiteout;
            return this;
        }

        public Surface colors(int shadowRgb, int primaryRgb, int highlightRgb) {
            this.shadowRgb = shadowRgb;
            this.primaryRgb = primaryRgb;
            this.highlightRgb = highlightRgb;
            return this;
        }

        /**
         * Sets the texture to bind before drawing, or {@code null} to preserve the caller's currently bound texture.
         */
        public Surface texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        /**
         * Sets the sheen in [0, 1] (default 1): on faces lying on integer local coordinates, which covers every face of
         * full-cube blocks, it raises the opacity floor by up to 0.35 (scaled in with {@link #fill(float) fill}) and
         * blends
         * the surface up to 45% toward white, so the woven surface reads as a solid pale shell. On other faces it only
         * appears within 0.05 blocks of an integer coordinate. Use 0 for a plain noise weave.
         */
        public Surface sheen(float sheen) {
            this.sheen = sheen;
            return this;
        }

        public void draw() {
            drawSurface(this);
        }
    }

    /**
     * Fluent configuration for {@link #drawWireframe(Wire)} and {@link #drawMeshGlow(Wire)}.
     */
    public static class Wire {

        private final Runnable drawGeometry;
        private double translateX, translateY, translateZ;
        private float localCenterX, localCenterY, localCenterZ;
        private float sizeX = 1F, sizeY = 1F, sizeZ = 1F;
        private float timeSec, strength = 1F, whiteCore = 0.5F, lineWidth = 2.0F;
        private int primaryRgb = DEFAULT_WIRE_PRIMARY_RGB, highlightRgb = DEFAULT_HIGHLIGHT_RGB;

        public Wire(Runnable drawGeometry) {
            this.drawGeometry = drawGeometry;
        }

        /**
         * Sets the translation in blocks applied before drawing the geometry, relative to the current modelview.
         */
        public Wire at(double x, double y, double z) {
            this.translateX = x;
            this.translateY = y;
            this.translateZ = z;
            return this;
        }

        public Wire at(Vec3d offset) {
            return offset == null ? this : at(offset.x(), offset.y(), offset.z());
        }

        /**
         * Sets the geometry's center and size in local blocks; {@link #whiteCore(float) whiteCore} peaks at the center.
         */
        public Wire bounds(float localCenterX, float localCenterY, float localCenterZ, float sizeX, float sizeY,
            float sizeZ) {
            this.localCenterX = localCenterX;
            this.localCenterY = localCenterY;
            this.localCenterZ = localCenterZ;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            return this;
        }

        /**
         * Sets the energy stream clock in seconds. Keep it small (an effect-local age) so float precision stays fine.
         */
        public Wire time(float timeSec) {
            this.timeSec = timeSec;
            return this;
        }

        /**
         * Sets the opacity multiplier, around [0, 1] (default 1); at or below 0.01 nothing is drawn.
         */
        public Wire strength(float strength) {
            this.strength = strength;
            return this;
        }

        /**
         * Sets how strongly the lines whiten toward the bounds center, usually [0, 2] (default 0.5); the white blend
         * itself is capped at 98%.
         */
        public Wire whiteCore(float whiteCore) {
            this.whiteCore = whiteCore;
            return this;
        }

        /**
         * Sets the GL line width in pixels for {@link #drawWireframe()}, at least 0.5 (default 2).
         */
        public Wire lineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Wire colors(int primaryRgb, int highlightRgb) {
            this.primaryRgb = primaryRgb;
            this.highlightRgb = highlightRgb;
            return this;
        }

        public void drawWireframe() {
            FxCrystalMesh.drawWireframe(this);
        }

        public void drawGlow() {
            FxCrystalMesh.drawMeshGlow(this);
        }
    }

    /**
     * Draws a model callback with procedural carved-crystal noise weaving that can be driven to a white glow with
     * {@link Surface#whiteout(float)}.
     */
    public static void drawSurface(Surface spec) {
        if (spec == null || spec.drawMesh == null || !(spec.fill > 0.01F)) return;
        float sheen = Float.isFinite(spec.sheen) ? Math.max(0F, Math.min(1F, spec.sheen)) : 1F;
        float reveal = Float.isFinite(spec.reveal) ? Math.max(0F, Math.min(1F, spec.reveal)) : 1F;
        float peak = Float.isFinite(spec.whiteout) ? Math.max(0F, Math.min(1F, spec.whiteout)) : 0F;
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_CURRENT_BIT
                | GL11.GL_TEXTURE_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(spec.translateX, spec.translateY, spec.translateZ);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            if (spec.texture != null) {
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(spec.texture);
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(1F, 1F, 1F, 1F);

            if (SURFACE.begin()) {
                try {
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glDepthMask(spec.fill >= 0.65F);
                    SURFACE.uniform1f("time", spec.timeSec);
                    SURFACE.uniform1f("fill", spec.fill);
                    SURFACE.uniform1f("whiteout", peak);
                    SURFACE.uniform1f("sheen", sheen);
                    SURFACE.uniform1f("centerY", spec.localCenterY);
                    SURFACE.uniform1f("sizeY", spec.sizeY);
                    SURFACE.uniform1f("reveal", reveal);
                    SURFACE.uniform3f("revealCenter", spec.localCenterX, spec.localCenterY, spec.localCenterZ);
                    SURFACE.uniform3f("revealHalf", spec.sizeX * 0.5F, spec.sizeY * 0.5F, spec.sizeZ * 0.5F);
                    SURFACE.uniformRgb("shadowColor", spec.shadowRgb);
                    SURFACE.uniformRgb("primaryColor", spec.primaryRgb);
                    SURFACE.uniformRgb("highlightColor", spec.highlightRgb);
                    spec.drawMesh.run();
                } finally {
                    SURFACE.end();
                }
            } else {
                float a = Math.min(1F, 0.35F + spec.fill * 0.65F);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
                try {
                    GL11.glColorMask(false, false, false, false);
                    GL11.glDepthMask(true);
                    spec.drawMesh.run();
                    GL11.glColorMask(true, true, true, true);
                    GL11.glDepthMask(false);
                    GL11.glDepthFunc(GL11.GL_EQUAL);
                    GL11.glBlendFunc(GL11.GL_CONSTANT_COLOR, GL11.GL_ONE);
                    GL14.glBlendColor(
                        (ColorMath.red(spec.primaryRgb) + (1F - ColorMath.red(spec.primaryRgb)) * peak) * a,
                        (ColorMath.green(spec.primaryRgb) + (1F - ColorMath.green(spec.primaryRgb)) * peak) * a,
                        (ColorMath.blue(spec.primaryRgb) + (1F - ColorMath.blue(spec.primaryRgb)) * peak) * a,
                        1F);
                    spec.drawMesh.run();
                } finally {
                    GL14.glBlendColor(0F, 0F, 0F, 0F);
                }
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    /**
     * Draws a wireframe callback (such as compiled {@link FxVoxelOutline#mergedLines}) with streaming vertical energy.
     */
    public static void drawWireframe(Wire spec) {
        if (spec == null || spec.drawGeometry == null || !(spec.strength > 0.01F)) return;
        float width = Float.isFinite(spec.lineWidth) ? Math.max(0.5F, spec.lineWidth) : 1.0F;
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_LINE_BIT
                | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(spec.translateX, spec.translateY, spec.translateZ);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(width);

            if (WIRE.begin()) {
                try {
                    uploadWireUniforms(spec);
                    spec.drawGeometry.run();
                } finally {
                    WIRE.end();
                }
            } else {
                GL11.glColor4f(
                    ColorMath.red(spec.highlightRgb),
                    ColorMath.green(spec.highlightRgb),
                    ColorMath.blue(spec.highlightRgb),
                    spec.strength);
                spec.drawGeometry.run();
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    /**
     * Emits a whole 3D mesh into an active {@link FxGlow} pass so the entire structure silhouette radiates smooth
     * optical bloom regardless of underlying vertex colors.
     */
    public static void drawMeshGlow(Wire spec) {
        if (spec == null || spec.drawGeometry == null || !(spec.strength > 0.01F)) return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(spec.translateX, spec.translateY, spec.translateZ);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (WIRE.begin()) {
                try {
                    uploadWireUniforms(spec);
                    spec.drawGeometry.run();
                } finally {
                    WIRE.end();
                }
            } else {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_FOG);
                GL11.glColor4f(
                    ColorMath.red(spec.highlightRgb) * spec.strength,
                    ColorMath.green(spec.highlightRgb) * spec.strength,
                    ColorMath.blue(spec.highlightRgb) * spec.strength,
                    1F);
                spec.drawGeometry.run();
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void uploadWireUniforms(Wire spec) {
        WIRE.uniform1f("time", spec.timeSec);
        WIRE.uniform1f("strength", spec.strength);
        WIRE.uniform1f("whiteCore", spec.whiteCore);
        WIRE.uniform2f("centerXZ", spec.localCenterX, spec.localCenterZ);
        WIRE.uniform1f("centerY", spec.localCenterY);
        WIRE.uniform2f("sizeXZ", spec.sizeX, spec.sizeZ);
        WIRE.uniform1f("sizeY", spec.sizeY);
        WIRE.uniformRgb("primaryColor", spec.primaryRgb);
        WIRE.uniformRgb("highlightColor", spec.highlightRgb);
    }
}
