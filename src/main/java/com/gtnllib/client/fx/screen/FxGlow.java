package com.gtnllib.client.fx.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.Framebuffer;

import org.lwjgl.opengl.GL11;

import com.gtnllib.GTNLLib;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A soft glow around anything drawn in 3D or 2D: draw the glowing shape between {@link #begin()} and
 * {@link #end(float, float, int)}, plain white with the current matrices, and it is blurred and added over the frame.
 * The shape is drawn into a quarter-size buffer without depth, so it is not hidden by what stands in front of it.
 * Needs framebuffers and shaders; without them {@link #begin()} returns false and nothing is drawn. Client thread only;
 * call {@link #close()} when the owner is discarded.
 */
@SideOnly(Side.CLIENT)
public class FxGlow implements AutoCloseable {

    private static final int DOWNSAMPLE = 4;
    private Framebuffer mask;
    private Framebuffer blurred;
    private boolean unavailable;

    /**
     * Redirects drawing into the glow buffer, cleared to black, until {@link #end(float, float, int)}.
     *
     * @return false when the glow cannot be drawn; then draw nothing and do not call {@code end}
     */
    public boolean begin() {
        if (unavailable || !OpenGlHelper.isFramebufferEnabled() || !OpenGlHelper.shadersSupported) return false;
        Framebuffer main = Minecraft.getMinecraft()
            .getFramebuffer();
        if (main == null || main.framebufferObject < 0) return false;
        try {
            ensureSize(
                (main.framebufferWidth + DOWNSAMPLE - 1) / DOWNSAMPLE,
                (main.framebufferHeight + DOWNSAMPLE - 1) / DOWNSAMPLE);
            mask.bindFramebuffer(true);
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            return true;
        } catch (RuntimeException failure) {
            unavailable = true;
            close();
            main.bindFramebuffer(true);
            GTNLLib.LOG.error("Glow is unavailable", failure);
            return false;
        }
    }

    /**
     * Blurs what was drawn since {@link #begin()} and adds it over the main framebuffer, which is bound again.
     *
     * @param strength brightness of the added glow, 0 to 1 or more
     * @param spread   blur step in glow-buffer pixels; larger spreads the glow wider
     * @param passes   horizontal and vertical blur pass pairs, 1 to 4
     */
    public void end(float strength, float spread, int passes) {
        Framebuffer main = Minecraft.getMinecraft()
            .getFramebuffer();
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_CURRENT_BIT
                | GL11.GL_TEXTURE_BIT);
        try {
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            float stepU = spread / mask.framebufferWidth, stepV = spread / mask.framebufferHeight;
            for (int i = 0; i < Math.max(1, Math.min(4, passes)); i++) {
                if (!FxFrostedBackdrop.pass(blurred, mask.framebufferTexture, stepU, 0)) return;
                FxFrostedBackdrop.pass(mask, blurred.framebufferTexture, 0, stepV);
            }
            main.bindFramebuffer(true);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            GL11.glColor4f(strength, strength, strength, 1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mask.framebufferTexture);
            fullScreenQuad();
        } catch (RuntimeException failure) {
            unavailable = true;
            close();
            GTNLLib.LOG.error("Glow is unavailable", failure);
        } finally {
            main.bindFramebuffer(true);
            GL11.glPopAttrib();
        }
    }

    private static void fullScreenQuad() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-1, -1, 0, 0, 0);
        tessellator.addVertexWithUV(1, -1, 0, 1, 0);
        tessellator.addVertexWithUV(1, 1, 0, 1, 1);
        tessellator.addVertexWithUV(-1, 1, 0, 0, 1);
        tessellator.draw();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
    }

    private void ensureSize(int width, int height) {
        if (mask != null && mask.framebufferWidth == width && mask.framebufferHeight == height) return;
        close();
        mask = create(width, height);
        blurred = create(width, height);
    }

    private static Framebuffer create(int width, int height) {
        Framebuffer target = new Framebuffer(width, height, false);
        target.setFramebufferFilter(GL11.GL_LINEAR);
        return target;
    }

    @Override
    public void close() {
        if (mask != null) mask.deleteFramebuffer();
        if (blurred != null) blurred.deleteFramebuffer();
        mask = null;
        blurred = null;
    }
}
