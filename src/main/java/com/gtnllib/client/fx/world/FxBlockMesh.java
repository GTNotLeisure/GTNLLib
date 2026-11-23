package com.gtnllib.client.fx.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.gtnllib.GTNLLib;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * Chosen blocks of a source world, drawn at their own positions from display lists. The blocks are split into
 * 16-block sections; a section is compiled once and again only when the blocks it shows change, and compiling is
 * spread over frames by a time budget. Call {@link #release()} when done, display lists are not freed otherwise.
 */
@SideOnly(Side.CLIENT)
public class FxBlockMesh {

    public static final int MAX_BLOCKS = 250_000, PART_BLOCKS = 1024;

    public static class Section {

        /**
         * Lowest corner of the section in source-world coordinates; the section spans 16 blocks on each axis. A
         * crowded section is split into several parts with the same corner, each compiled on its own.
         */
        public final int x, y, z;
        public final IntArrayList cells = new IntArrayList();
        public int list = -1;
        public boolean dirty = true;
        public long signature;

        public Section(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public final FxBlockView view = new FxBlockView();
    private final List<Section> sections = new ArrayList<>();
    private Object key;
    private int[] xs = new int[0], ys = new int[0], zs = new int[0];

    public int x(int cell) {
        return xs[cell];
    }

    public int y(int cell) {
        return ys[cell];
    }

    public int z(int cell) {
        return zs[cell];
    }

    public int cellAt(int x, int y, int z) {
        return view.cellAt(x, y, z);
    }

    public void release() {
        for (Section section : sections) {
            if (section.list >= 0) GL11.glDeleteLists(section.list, 1);
        }
        sections.clear();
        key = null;
        view.source = null;
        view.shown = null;
        view.cells.clear();
    }

    /**
     * Uses the blocks at the given positions of the source world. Nothing is rebuilt while the key stays the same;
     * more than {@link #MAX_BLOCKS} blocks leave the mesh empty.
     *
     * @return true when the mesh was rebuilt, so per-block filters must be applied again
     */
    public boolean prepare(Object key, World source, int[] xs, int[] ys, int[] zs) {
        if (key == this.key) return false;
        release();
        this.key = key;
        if (xs.length > MAX_BLOCKS) return true;
        this.xs = xs;
        this.ys = ys;
        this.zs = zs;
        view.source = source;
        Long2ObjectOpenHashMap<Section> open = new Long2ObjectOpenHashMap<>();
        for (int cell = 0; cell < xs.length; cell++) {
            view.cells.put(FxVoxelOutline.pack(xs[cell], ys[cell], zs[cell]), cell);
            long packed = FxVoxelOutline.pack(xs[cell] >> 4, ys[cell] >> 4, zs[cell] >> 4);
            Section section = open.get(packed);
            if (section == null || section.cells.size() >= PART_BLOCKS) {
                section = new Section(xs[cell] & ~15, ys[cell] & ~15, zs[cell] & ~15);
                open.put(packed, section);
                sections.add(section);
            }
            section.cells.add(cell);
        }
        return true;
    }

    /**
     * Shows only blocks whose entry is true, or all blocks for null. Only sections whose shown blocks changed are
     * compiled again.
     */
    public void show(boolean[] shown) {
        if (shown == null && view.shown == null) return;
        view.shown = shown;
        for (Section section : sections) {
            long signature = 0;
            for (int i = 0; i < section.cells.size(); i++) {
                int cell = section.cells.getInt(i);
                if (shown == null || shown[cell]) signature = signature * 31 + cell + 1;
            }
            if (signature != section.signature) {
                section.signature = signature;
                section.dirty = true;
            }
        }
    }

    /**
     * Compiles changed sections until the budget runs out.
     *
     * @return true when every section is compiled
     */
    public boolean compile(long budgetNanos) {
        return compile(budgetNanos, section -> true);
    }

    /**
     * Compiles changed sections that are ready, in the order their first block was given, until the budget runs out.
     *
     * @return true when every section is compiled
     */
    public boolean compile(long budgetNanos, Predicate<Section> ready) {
        long deadline = System.nanoTime() + budgetNanos;
        boolean complete = true;
        for (Section section : sections) {
            if (!section.dirty) continue;
            if (System.nanoTime() > deadline || !ready.test(section)) {
                complete = false;
                continue;
            }
            compile(section);
        }
        return complete;
    }

    /**
     * Draws the compiled sections. The caller binds the block texture and sets the GL state.
     */
    public void draw() {
        for (Section section : sections) {
            if (section.list >= 0) GL11.glCallList(section.list);
        }
    }

    /**
     * Draws the compiled sections that pass the test, such as a view-frustum check of each section's box.
     */
    public void draw(Predicate<Section> visible) {
        for (Section section : sections) {
            if (section.list >= 0 && visible.test(section)) GL11.glCallList(section.list);
        }
    }

    /**
     * Sets the GL state for drawing a mesh as ordinary solid blocks: block texture, no lighting or blending, alpha
     * test, culling, depth writes and fog.
     */
    public static void solidState() {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationBlocksTexture);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glColor4f(1, 1, 1, 1);
    }

    /**
     * Adds the mesh over itself as light, right after it was drawn with {@link #solidState}; strength 0..1. The
     * lists carry their own vertex colours, so the brightness goes through the constant blend colour.
     */
    public void drawFlash(float strength, Predicate<Section> visible) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_CONSTANT_COLOR, GL11.GL_ONE);
        GL14.glBlendColor(strength, strength, strength, 1);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_FOG);
        draw(visible);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glDepthMask(true);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_BLEND);
        GL14.glBlendColor(0, 0, 0, 0);
    }

    private void compile(Section section) {
        section.dirty = false;
        if (section.list < 0) section.list = GL11.glGenLists(1);
        Minecraft mc = Minecraft.getMinecraft();
        int ambientOcclusion = mc.gameSettings.ambientOcclusion;
        mc.gameSettings.ambientOcclusion = 0;
        RenderBlocks renderer = new RenderBlocks(view);
        Tessellator tessellator = Tessellator.instance;
        EntityPlayer player = mc.thePlayer;
        World playerWorld = player == null ? null : player.worldObj;
        GL11.glNewList(section.list, GL11.GL_COMPILE);
        tessellator.setTranslation(0, 0, 0);
        tessellator.startDrawingQuads();
        try {
            // Some renderers (GT multiblock fronts) look tile entities up in the player's world at the drawn position.
            if (player != null) player.worldObj = view.source;
            for (int pass = 0; pass < 2; pass++) {
                ForgeHooksClient.setRenderPass(pass);
                for (int i = 0; i < section.cells.size(); i++) {
                    int cell = section.cells.getInt(i);
                    if (view.shown != null && !view.shown[cell]) continue;
                    Block block = view.getBlock(xs[cell], ys[cell], zs[cell]);
                    if (!block.canRenderInPass(pass)) continue;
                    try {
                        renderer.renderBlockByRenderType(block, xs[cell], ys[cell], zs[cell]);
                    } catch (RuntimeException e) {
                        GTNLLib.LOG.debug("Could not draw {} in a block mesh", block, e);
                    }
                }
            }
        } finally {
            ForgeHooksClient.setRenderPass(-1);
            tessellator.draw();
            GL11.glEndList();
            mc.gameSettings.ambientOcclusion = ambientOcclusion;
            if (player != null) player.worldObj = playerWorld;
        }
    }
}
