package me.coolaid.optical.logic;

import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class Detached {
    private static boolean active;
    private static Vec3 position = Vec3.ZERO;
    private static float yaw;
    private static float pitch;

    private Detached() {
    }

    public static boolean isActive() {
        return active;
    }

    public static Vec3 getPosition() {
        return position;
    }

    public static float getYaw() {
        return yaw;
    }

    public static float getPitch() {
        return pitch;
    }

    public static void toggle(Minecraft minecraft) {
        if (!OpticalConfig.DETACHED_CAMERA.isEnabled()) return;
        if (active) {
            deactivate();
        } else {
            activate(minecraft);
        }
    }

    public static void onClientTick(Minecraft minecraft) {
        if (!active) return;
        if (!OpticalConfig.DETACHED_CAMERA.isEnabled() || minecraft.player == null || minecraft.level == null) {
            deactivate();
        }
    }

    public static void onDisconnect() {
        deactivate();
    }

    public static void deactivate() {
        active = false;
    }

    private static void activate(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || Freecam.isActive()) return;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        position = camera.position();
        yaw = camera.yRot();
        pitch = camera.xRot();
        active = true;
    }
}
