package me.coolaid.optical.logic;

import me.coolaid.optical.util.FreecamCameraEntity;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Freecam {
    private static boolean active = false;
    private static boolean momentumMode = false;

    private static CameraType lastPerspective;
    private static Vec3 velocity = Vec3.ZERO;
    private static FreecamCameraEntity cameraEntity;

    private static final Vec3[] TRIPOD_POSITIONS = new Vec3[3];
    private static final float[] TRIPOD_YAWS = new float[3];
    private static final float[] TRIPOD_PITCHES = new float[3];

    private Freecam() {
    }

    public static boolean isActive() {
        return active;
    }

    public static Vec3 getPosition() {
        return cameraEntity != null ? cameraEntity.position() : Vec3.ZERO;
    }

    public static float getYaw() {
        return cameraEntity != null ? cameraEntity.getYRot() : 0.0F;
    }

    public static float getPitch() {
        return cameraEntity != null ? cameraEntity.getXRot() : 0.0F;
    }

    public static void addLookDelta(double xDelta, double yDelta) {
        if (!active || cameraEntity == null) {
            return;
        }

        float sensitivity = (float) OpticalConfig.FREECAM.getSensitivityMultiplier();
        float pitchDelta = (float) (yDelta * 0.15F * sensitivity);
        float yawDelta = (float) (xDelta * 0.15F * sensitivity);

        float pitch = cameraEntity.getXRot();
        if (OpticalConfig.FREECAM.isInvertY()) {
            pitch = Mth.clamp(pitch + pitchDelta, -90.0f, 90.0f);
        } else {
            pitch = Mth.clamp(pitch - pitchDelta, -90.0f, 90.0f);
        }

        cameraEntity.setXRot(pitch);
        cameraEntity.setYRot(cameraEntity.getYRot() + yawDelta);
        cameraEntity.yHeadRot = cameraEntity.getYRot();
        cameraEntity.yBodyRot = cameraEntity.getYRot();
    }

    public static void toggle(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled()) {
            return;
        }
        if (active) {
            deactivate(minecraft);
        } else {
            activate(minecraft);
        }
    }

    public static void toggleMomentumMode() {
        momentumMode = !momentumMode;
    }

    public static void onClientTick(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled() || minecraft.player == null || minecraft.level == null) {
            deactivate(minecraft);
            return;
        }

        if (!active || cameraEntity == null) {
            return;
        }

        minecraft.options.keyAttack.setDown(false);
        minecraft.options.keyUse.setDown(false);

        double horizontalSpeed = OpticalConfig.FREECAM.getHorizontalSpeed();
        double verticalSpeed = OpticalConfig.FREECAM.getVerticalSpeed();

        double forward = (minecraft.options.keyUp.isDown() ? 1.0 : 0.0) - (minecraft.options.keyDown.isDown() ? 1.0 : 0.0);
        double strafe = (minecraft.options.keyLeft.isDown() ? 1.0 : 0.0) - (minecraft.options.keyRight.isDown() ? 1.0 : 0.0);
        double vertical = (minecraft.options.keyJump.isDown() ? 1.0 : 0.0) - (minecraft.options.keyShift.isDown() ? 1.0 : 0.0);

        Vec3 inputDirection = new Vec3(strafe, 0.0, forward);
        if (inputDirection.lengthSqr() > 1.0E-6) {
            inputDirection = inputDirection.normalize();
        }

        float yawRad = cameraEntity.getYRot() * ((float) Math.PI / 180F);
        Vec3 forwardVec = new Vec3(-Mth.sin(yawRad), 0.0, Mth.cos(yawRad));
        Vec3 rightVec = new Vec3(forwardVec.z, 0.0, -forwardVec.x);

        Vec3 targetVelocity = forwardVec.scale(inputDirection.z * horizontalSpeed)
                .add(rightVec.scale(inputDirection.x * horizontalSpeed))
                .add(0.0, vertical * verticalSpeed, 0.0);

        if (momentumMode) {
            double smoothing = OpticalConfig.FREECAM.getMomentumSmoothing();
            velocity = new Vec3(
                    Mth.lerp(smoothing, velocity.x, targetVelocity.x),
                    Mth.lerp(smoothing, velocity.y, targetVelocity.y),
                    Mth.lerp(smoothing, velocity.z, targetVelocity.z)
            );

            if (targetVelocity.lengthSqr() < 1.0E-6) {
                velocity = velocity.scale(OpticalConfig.FREECAM.getMomentumDamping());
            }
        } else {
            velocity = targetVelocity;
        }

        cameraEntity.setDeltaMovement(velocity);
        cameraEntity.setPos(cameraEntity.getX() + velocity.x, cameraEntity.getY() + velocity.y, cameraEntity.getZ() + velocity.z);
    }

    public static void handleTripod(int index) {
        if (!active || cameraEntity == null || index < 0 || index >= TRIPOD_POSITIONS.length) {
            return;
        }

        if (Minecraft.getInstance().options.keyShift.isDown()) {
            TRIPOD_POSITIONS[index] = cameraEntity.position();
            TRIPOD_YAWS[index] = cameraEntity.getYRot();
            TRIPOD_PITCHES[index] = cameraEntity.getXRot();
            return;
        }

        Vec3 tripodPos = TRIPOD_POSITIONS[index];
        if (tripodPos != null) {
            cameraEntity.setPos(tripodPos.x, tripodPos.y, tripodPos.z);
            cameraEntity.setYRot(TRIPOD_YAWS[index]);
            cameraEntity.setXRot(TRIPOD_PITCHES[index]);
            cameraEntity.yHeadRot = cameraEntity.getYRot();
            cameraEntity.yBodyRot = cameraEntity.getYRot();
            velocity = Vec3.ZERO;
        }
    }

    public static void onClientCleanup(Minecraft minecraft) {
        if (active && (minecraft.player == null || minecraft.level == null)) {
            active = false;
            velocity = Vec3.ZERO;
            cameraEntity = null;
        }
    }

    private static void activate(Minecraft minecraft) {
        if (active || minecraft.player == null || !(minecraft.level instanceof ClientLevel level)) {
            return;
        }

        Freelook.forceDeactivate(minecraft);

        active = true;
        lastPerspective = minecraft.options.getCameraType();
        if (lastPerspective != CameraType.FIRST_PERSON) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }

        cameraEntity = new FreecamCameraEntity(level);
        cameraEntity.setId(-420);
        cameraEntity.setPos(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ());
        cameraEntity.setYRot(minecraft.player.getYRot());
        cameraEntity.setXRot(minecraft.player.getXRot());
        cameraEntity.yHeadRot = cameraEntity.getYRot();
        cameraEntity.yBodyRot = cameraEntity.getYRot();
        level.addEntity(cameraEntity);
        minecraft.setCameraEntity(cameraEntity);

        velocity = Vec3.ZERO;
        momentumMode = OpticalConfig.FREECAM.isMomentumByDefault();
    }

    private static void deactivate(Minecraft minecraft) {
        if (!active) {
            return;
        }

        active = false;
        velocity = Vec3.ZERO;

        if (minecraft.player != null) {
            minecraft.setCameraEntity(minecraft.player);
        }

        if (cameraEntity != null) {
            cameraEntity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            cameraEntity = null;
        }

        if (lastPerspective != null && minecraft.options.getCameraType() != lastPerspective) {
            minecraft.options.setCameraType(lastPerspective);
        }
    }
}