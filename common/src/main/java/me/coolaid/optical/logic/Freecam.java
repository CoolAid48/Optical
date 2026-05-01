package me.coolaid.optical.logic;

import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.util.ActionBarMessages;
import me.coolaid.optical.util.FreecamCameraEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public final class Freecam {
    private static boolean active = false;
    private static boolean switchingCameraEntity = false;

    private static CameraType lastPerspective;
    private static boolean lastSmartCull = true;
    private static FreecamCameraEntity cameraEntity;

    private Freecam() {
    }

    public static boolean isActive() { return active; }
    public static boolean shouldRenderPlayerName() { return OpticalConfig.FREECAM.isShowDetachedPlayerName(); }
    public static boolean shouldRenderPlayerHand() { return OpticalConfig.FREECAM.isShowDetachedPlayerHand(); }
    public static Vec3 getPosition(float partialTick) {
        if (cameraEntity == null) return Vec3.ZERO;
        return new Vec3(
                Mth.lerp(partialTick, cameraEntity.xo, cameraEntity.getX()),
                Mth.lerp(partialTick, cameraEntity.yo, cameraEntity.getY()),
                Mth.lerp(partialTick, cameraEntity.zo, cameraEntity.getZ())
        );
    }
    public static float getYaw(float partialTick) {
        if (cameraEntity == null) return 0.0F;
        return cameraEntity.yRotO + partialTick * Mth.wrapDegrees(cameraEntity.getYRot() - cameraEntity.yRotO);
    }
    public static float getPitch(float partialTick) {
        return cameraEntity != null ? Mth.lerp(partialTick, cameraEntity.xRotO, cameraEntity.getXRot()) : 0.0F;
    }
    public static FreecamCameraEntity getCameraEntity() { return cameraEntity; }
    public static boolean isSwitchingCameraEntity() { return switchingCameraEntity; }

    public static void addLookDelta(double xDelta, double yDelta) {
        if (!active || cameraEntity == null) return;
        float pitchDelta = (float) (yDelta * 0.15F);
        float yawDelta = (float) (xDelta * 0.15F);
        float pitch = cameraEntity.getXRot();

        pitch = OpticalConfig.FREECAM.isInvertY()
                ? Mth.clamp(pitch - pitchDelta, -90.0f, 90.0f)
                : Mth.clamp(pitch + pitchDelta, -90.0f, 90.0f);

        cameraEntity.setXRot(pitch);
        cameraEntity.setYRot(cameraEntity.getYRot() + yawDelta);
        cameraEntity.xRotO = cameraEntity.getXRot();
        cameraEntity.yRotO = cameraEntity.getYRot();
        cameraEntity.yHeadRot = cameraEntity.getYRot();
        cameraEntity.yHeadRotO = cameraEntity.getYRot();
        cameraEntity.yBodyRot = cameraEntity.getYRot();
        cameraEntity.yBodyRotO = cameraEntity.getYRot();
        cameraEntity.xBob = cameraEntity.xBobO = cameraEntity.getXRot();
        cameraEntity.yBob = cameraEntity.yBobO = cameraEntity.getYRot();
    }

    public static void toggle(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled()) return;
        if (active) deactivate(minecraft); else activate(minecraft);
    }

    public static void onClientTick(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled() || minecraft.player == null || minecraft.level == null) {
            deactivate(minecraft);
            return;
        }

        if (!active || cameraEntity == null) return;
        if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) minecraft.options.setCameraType(CameraType.FIRST_PERSON);

        cameraEntity.setCollisionEnabled(OpticalConfig.FREECAM.isCollisionEnabled());
    }

    public static void onPreClientTick(Minecraft minecraft) {
        if (!active || minecraft.player == null || cameraEntity == null) return;
        if (!(minecraft.player.input instanceof KeyboardInput)) return;

        ClientInput input = new ClientInput();
        Input keyPresses = minecraft.player.input.keyPresses;
        input.keyPresses = new Input(false, false, false, false, false, keyPresses.shift(), false);
        minecraft.player.input = input;
    }

    public static void onClientCleanup(Minecraft minecraft) {
        if (active && (minecraft.player == null || minecraft.level == null)) {
            active = false;
            cameraEntity = null;
        }
    }

    public static void onDisconnect(Minecraft minecraft) {
        deactivate(minecraft);
    }

    private static void activate(Minecraft minecraft) {
        if (active || minecraft.player == null || !(minecraft.level instanceof ClientLevel level)) return;
        Freelook.forceDeactivate(minecraft);
        Detached.deactivate();
        lastSmartCull = minecraft.smartCull;
        minecraft.smartCull = false;
        lastPerspective = minecraft.options.getCameraType();
        if (lastPerspective != CameraType.FIRST_PERSON) minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        active = true;

        cameraEntity = new FreecamCameraEntity(level);
        cameraEntity.setId(-420);
        Vec3 detachPos = minecraft.player.getEyePosition()
                .add(Vec3.directionFromRotation(minecraft.player.getXRot(), minecraft.player.getYRot()).scale(-4.0D));

        cameraEntity.setPos(detachPos.x, detachPos.y, detachPos.z);
        cameraEntity.setYRot(minecraft.player.getYRot());
        cameraEntity.setXRot(minecraft.player.getXRot());
        snapCameraHistory(cameraEntity);
        cameraEntity.setCollisionEnabled(OpticalConfig.FREECAM.isCollisionEnabled());

        level.addEntity(cameraEntity);
        setMinecraftCameraEntity(minecraft, cameraEntity);
        ActionBarMessages.showFreecam(true);
    }

    private static void deactivate(Minecraft minecraft) {
        if (!active) return;
        active = false;
        minecraft.smartCull = lastSmartCull;
        if (minecraft.player != null) setMinecraftCameraEntity(minecraft, minecraft.player);
        if (cameraEntity != null) { cameraEntity.remove(Entity.RemovalReason.DISCARDED); cameraEntity = null; }
        if (minecraft.player != null) minecraft.player.input = new KeyboardInput(minecraft.options);
        if (lastPerspective != null && minecraft.options.getCameraType() != lastPerspective) minecraft.options.setCameraType(lastPerspective);
        ActionBarMessages.showFreecam(false);
    }

    private static void setMinecraftCameraEntity(Minecraft minecraft, Entity entity) {
        switchingCameraEntity = true;
        try {
            minecraft.setCameraEntity(entity);
        } finally {
            switchingCameraEntity = false;
        }
    }

    private static void snapCameraHistory(FreecamCameraEntity camera) {
        camera.xo = camera.getX();
        camera.yo = camera.getY();
        camera.zo = camera.getZ();
        camera.xRotO = camera.getXRot();
        camera.yRotO = camera.getYRot();
        camera.yHeadRot = camera.getYRot();
        camera.yHeadRotO = camera.getYRot();
        camera.yBodyRot = camera.getYRot();
        camera.yBodyRotO = camera.getYRot();
        camera.xBob = camera.xBobO = camera.getXRot();
        camera.yBob = camera.yBobO = camera.getYRot();
    }
}
