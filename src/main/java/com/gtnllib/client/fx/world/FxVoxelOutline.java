package com.gtnllib.client.fx.world;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Line outlines around sets of whole blocks, drawn into the current GL coordinate space. Block positions are packed
 * with {@link #pack(int, int, int)}; each axis keeps 21 signed bits.
 */
@SideOnly(Side.CLIENT)
public class FxVoxelOutline {

    private static final double LIFT = 0.003;

    private FxVoxelOutline() {}

    public static long pack(int x, int y, int z) {
        return ((long) (x & 0x1FFFFF) << 42) | ((long) (y & 0x1FFFFF) << 21) | (z & 0x1FFFFF);
    }

    public static int unpackX(long packed) {
        return (int) (packed << 1 >> 43);
    }

    public static int unpackY(long packed) {
        return (int) (packed << 22 >> 43);
    }

    public static int unpackZ(long packed) {
        return (int) (packed << 43 >> 43);
    }

    /**
     * One slightly enlarged box per block, like vanilla's block highlight.
     */
    public static void boxes(Tessellator tessellator, LongSet blocks, int rgb, int alpha) {
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorRGBA_I(rgb, alpha);
        LongIterator iterator = blocks.iterator();
        while (iterator.hasNext()) {
            long packed = iterator.nextLong();
            double[] low = { unpackX(packed) - LIFT, unpackY(packed) - LIFT, unpackZ(packed) - LIFT };
            double[] high = { low[0] + 1 + 2 * LIFT, low[1] + 1 + 2 * LIFT, low[2] + 1 + 2 * LIFT };
            for (int axis = 0; axis < 3; axis++) {
                int first = (axis + 1) % 3, second = (axis + 2) % 3;
                for (int i = 0; i < 4; i++) {
                    double[] start = new double[3];
                    start[axis] = low[axis];
                    start[first] = (i & 1) == 0 ? low[first] : high[first];
                    start[second] = (i >> 1) == 0 ? low[second] : high[second];
                    double[] end = start.clone();
                    end[axis] = high[axis];
                    tessellator.addVertex(start[0], start[1], start[2]);
                    tessellator.addVertex(end[0], end[1], end[2]);
                }
            }
        }
        tessellator.draw();
    }

    /**
     * The outline of the whole set as one shape: an edge is drawn only where the surface bends, so edges between two
     * touching blocks disappear.
     */
    public static void merged(Tessellator tessellator, LongSet blocks, int rgb, int alpha) {
        LongSet[] seen = { new LongOpenHashSet(), new LongOpenHashSet(), new LongOpenHashSet() };
        tessellator.startDrawing(GL11.GL_LINES);
        tessellator.setColorRGBA_I(rgb, alpha);
        int[] point = new int[3], corner = new int[3];
        LongIterator iterator = blocks.iterator();
        while (iterator.hasNext()) {
            long packed = iterator.nextLong();
            point[0] = unpackX(packed);
            point[1] = unpackY(packed);
            point[2] = unpackZ(packed);
            for (int axis = 0; axis < 3; axis++) {
                int first = (axis + 1) % 3, second = (axis + 2) % 3;
                for (int i = 0; i < 4; i++) {
                    corner[axis] = point[axis];
                    corner[first] = point[first] + (i & 1);
                    corner[second] = point[second] + (i >> 1);
                    if (seen[axis].add(pack(corner[0], corner[1], corner[2])))
                        edge(tessellator, blocks, corner, axis, first, second);
                }
            }
        }
        tessellator.draw();
    }

    private static void edge(Tessellator tessellator, LongSet blocks, int[] corner, int axis, int first, int second) {
        boolean[] filled = new boolean[4];
        int count = 0;
        double shiftFirst = 0, shiftSecond = 0;
        for (int i = 0; i < 4; i++) {
            int[] cell = corner.clone();
            cell[first] -= 1 - (i & 1);
            cell[second] -= 1 - (i >> 1);
            filled[i] = blocks.contains(pack(cell[0], cell[1], cell[2]));
            if (filled[i]) {
                count++;
            } else {
                shiftFirst += (i & 1) == 0 ? -1 : 1;
                shiftSecond += (i >> 1) == 0 ? -1 : 1;
            }
        }
        boolean diagonal = count == 2 && filled[0] == filled[3];
        if (count != 1 && count != 3 && !diagonal) return;
        double[] start = { corner[0], corner[1], corner[2] };
        start[first] += Math.signum(shiftFirst) * LIFT;
        start[second] += Math.signum(shiftSecond) * LIFT;
        double[] end = start.clone();
        end[axis] += 1;
        tessellator.addVertex(start[0], start[1], start[2]);
        tessellator.addVertex(end[0], end[1], end[2]);
    }
}
