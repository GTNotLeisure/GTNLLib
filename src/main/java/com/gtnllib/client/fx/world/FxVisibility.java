package com.gtnllib.client.fx.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import com.gtnllib.utils.Vec3d;

public class FxVisibility {

    public static boolean allowed(boolean self, double distance, boolean showOthers, double maxDistance) {
        return self || showOthers && distance <= maxDistance;
    }

    public static boolean isViewer(Entity entity) {
        return entity != null && entity == Minecraft.getMinecraft().thePlayer;
    }

    public static boolean isViewer(int entityId) {
        Entity player = Minecraft.getMinecraft().thePlayer;
        return player != null && player.getEntityId() == entityId;
    }

    public static double distanceToViewer(Entity entity) {
        Entity viewer = Minecraft.getMinecraft().renderViewEntity;
        return viewer == null || viewer == entity ? 0 : viewer.getDistanceToEntity(entity);
    }

    public static double distanceToViewer(Vec3d point) {
        Entity viewer = Minecraft.getMinecraft().renderViewEntity;
        if (viewer == null || point == null) return 0;
        return Math.sqrt(viewer.getDistanceSq(point.x(), point.y(), point.z()));
    }

    /**
     * Tests whether no block lies between the {@link RenderManager} render position and the near surface of a sphere.
     * Returns {@code true} when there is no world or the center is {@code null} or not finite.
     *
     * @param center          sphere center in world coordinates (blocks)
     * @param clearanceRadius sphere radius in blocks; blocks inside the sphere are ignored
     */
    public static boolean hasLineOfSight(Vec3d center, double clearanceRadius) {
        if (center == null || !center.finite()) return true;
        return hasLineOfSight(
            Minecraft.getMinecraft(),
            center.x(),
            center.y(),
            center.z(),
            clearanceRadius,
            RenderManager.renderPosX,
            RenderManager.renderPosY,
            RenderManager.renderPosZ);
    }

    /**
     * Tests whether no block lies between a camera position and the near surface of a sphere, ignoring the last half
     * block before the sphere. Returns {@code true} when there is no world or the camera is within 1.2 blocks of the
     * sphere surface.
     *
     * @param centerX         sphere center in world coordinates (blocks)
     * @param clearanceRadius sphere radius in blocks; blocks inside the sphere are ignored; negative or not finite
     *                        counts as 0
     * @param cameraX         camera position in world coordinates (blocks)
     */
    public static boolean hasLineOfSight(Minecraft minecraft, double centerX, double centerY, double centerZ,
        double clearanceRadius, double cameraX, double cameraY, double cameraZ) {
        if (minecraft == null || minecraft.theWorld == null) return true;
        double clearance = Double.isFinite(clearanceRadius) ? Math.max(0.0, clearanceRadius) : 0.0;
        double dx = centerX - cameraX, dy = centerY - cameraY, dz = centerZ - cameraZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist <= clearance + 1.2) return true;
        double frac = Math.max(0.05, (dist - clearance - 0.5) / dist);
        Vec3 from = Vec3.createVectorHelper(cameraX, cameraY, cameraZ);
        Vec3 to = Vec3.createVectorHelper(cameraX + dx * frac, cameraY + dy * frac, cameraZ + dz * frac);
        return minecraft.theWorld.func_147447_a(from, to, false, true, false) == null;
    }
}
