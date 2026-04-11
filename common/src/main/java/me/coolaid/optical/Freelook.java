package me.coolaid.optical;

import me.coolaid.optical.config.Config;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class Freelook {
    private static final float TURN_FACTOR = 0.15F;

    private static boolean active;
    private static float cameraYaw;
    private static float cameraPitch;
    private static CameraType previousCameraType = CameraType.FIRST_PERSON;

    private Freelook() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void updateFromKeyState(Minecraft minecraft, boolean keyDown) {
        if (!Config.FREELOOK.isEnabled()) {
            deactivate(minecraft);
            return;
        }

        if (Config.FREELOOK.isToggleMode()) {
            return;
        }

        if (keyDown) {
            activate(minecraft);
        } else {
            deactivate(minecraft);
        }
    }

    public static boolean consumeTurn(LocalPlayer player, double yawDelta, double pitchDelta) {
        if (!active) {
            return false;
        }

        float sensitivity = (float) Config.FREELOOK.getSensitivityMultiplier();
        float yawChange = (float) yawDelta * TURN_FACTOR * sensitivity;
        float pitchChange = (float) pitchDelta * TURN_FACTOR * sensitivity;

        cameraYaw += yawChange;
        cameraPitch += Config.FREELOOK.isInvertY() ? pitchChange : -pitchChange;
        cameraPitch = Mth.clamp(cameraPitch, -90.0F, 90.0F);

        player.setYRot(player.getYRot());
        player.setXRot(player.getXRot());
        return true;
    }

    public static float getCameraYaw(float fallback) {
        return active ? cameraYaw : fallback;
    }

    public static float getCameraPitch(float fallback) {
        return active ? cameraPitch : fallback;
    }

    public static void onClientCleanup(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null){
            deactivate(minecraft);
        }
    }

    private static void activate(Minecraft minecraft) {
        if (active || minecraft.player == null) {
            return;
        }

        active = true;
        previousCameraType = minecraft.options.getCameraType();
        cameraYaw = minecraft.player.getYRot();
        cameraPitch = minecraft.player.getXRot();

        if (!previousCameraType.isMirrored()) {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }

    private static void deactivate(Minecraft minecraft) {
        if (!active) {
            return;
        }

        active = false;
        if (minecraft.options.getCameraType() != previousCameraType) {
            minecraft.options.setCameraType(previousCameraType);
        }
    }
}
