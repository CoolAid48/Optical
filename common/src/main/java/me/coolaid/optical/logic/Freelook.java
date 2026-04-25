package me.coolaid.optical.logic;

import me.coolaid.optical.CameraOverriddenEntity;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public final class Freelook {
    private static boolean active = false;
    private static CameraType lastPerspective;
    private static CameraType freeLookPerspective;

    private Freelook() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void updateFromKeyState(Minecraft minecraft, boolean keyDown) {
        if (!OpticalConfig.FREELOOK.isEnabled()) {
            deactivate(minecraft);
            return;
        }
        if (OpticalConfig.FREELOOK.isToggleMode()) {
            return;
        }
        if (keyDown) {
            activate(minecraft, CameraType.THIRD_PERSON_BACK);
        } else {
            deactivate(minecraft);
        }
    }

    public static void toggle(Minecraft minecraft) {
        if (!OpticalConfig.FREELOOK.isEnabled()) {
            return;
        }
        if (active) {
            deactivate(minecraft);
        } else {
            activate(minecraft, CameraType.THIRD_PERSON_BACK);
        }
    }

    public static void onClientCleanup(Minecraft minecraft) {
        if (active && (minecraft.player == null || minecraft.level == null)) {
            active = false;
            freeLookPerspective = null;
        }
    }

    private static void activate(Minecraft minecraft, CameraType requestedPerspective) {
        if (active || minecraft.player == null) {
            return;
        }
        active = true;
        lastPerspective = minecraft.options.getCameraType();
        freeLookPerspective = requestedPerspective;
        if (minecraft.player instanceof CameraOverriddenEntity cameraEntity) {
            cameraEntity.optical$setCameraYaw(minecraft.player.getYRot());
            cameraEntity.optical$setCameraPitch(minecraft.player.getXRot());
        }
        if (lastPerspective == CameraType.FIRST_PERSON) {
            minecraft.options.setCameraType(requestedPerspective);
        }
    }

    public static void forceDeactivate(Minecraft minecraft) {
        deactivate(minecraft);
    }

    private static void deactivate(Minecraft minecraft) {
        if (!active) {
            return;
        }
        active = false;
        freeLookPerspective = null;
        if (minecraft.options.getCameraType() != lastPerspective) {
            minecraft.options.setCameraType(lastPerspective);
        }
    }

    public static CameraType getFreeLookPerspective() {
        return freeLookPerspective;
    }
}