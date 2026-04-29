package me.coolaid.optical.logic;

import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.util.FreecamCameraEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public final class Freecam {
    public static final int DETACHED_PLAYER_VISUAL_ID = Integer.MAX_VALUE - 421;
    private static final double CREATIVE_MOMENTUM_SMOOTHING = 0.10D;
    private static final double CREATIVE_MOMENTUM_DAMPING = 0.94D;

    private static boolean active = false;

    private static CameraType lastPerspective;
    private static Vec3 velocity = Vec3.ZERO;
    private static FreecamCameraEntity cameraEntity;
    private static FreecamCameraEntity detachedPlayerVisual;

    private static final Vec3[] TRIPOD_POSITIONS = new Vec3[3];
    private static final float[] TRIPOD_YAWS = new float[3];
    private static final float[] TRIPOD_PITCHES = new float[3];

    private Freecam() {
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean shouldRenderPlayerName() {
        return OpticalConfig.FREECAM.isShowDetachedPlayerName();
    }

    public static boolean shouldRenderPlayerHand() {
        return OpticalConfig.FREECAM.isShowDetachedPlayerHand();
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

        float pitchDelta = (float) (yDelta * 0.15F);
        float yawDelta = (float) (xDelta * 0.15F);

        float pitch = cameraEntity.getXRot();
        if (OpticalConfig.FREECAM.isInvertY()) {
            pitch = Mth.clamp(pitch - pitchDelta, -90.0f, 90.0f);
        } else {
            pitch = Mth.clamp(pitch + pitchDelta, -90.0f, 90.0f);
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
        OpticalConfig.FreecamConfig.FlightMode mode = OpticalConfig.FREECAM.getFlightMode();
        OpticalConfig.FREECAM.setFlightMode(mode == OpticalConfig.FreecamConfig.FlightMode.DEFAULT
                ? OpticalConfig.FreecamConfig.FlightMode.CREATIVE
                : OpticalConfig.FreecamConfig.FlightMode.DEFAULT);
    }

    public static void onClientTick(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled() || minecraft.player == null || minecraft.level == null) {
            deactivate(minecraft);
            return;
        }

        ensureDetachedPlayerVisual(minecraft);
        syncDetachedPlayerVisual(minecraft);

        if (!active || cameraEntity == null) {
            return;
        }

        cameraEntity.setCollisionEnabled(OpticalConfig.FREECAM.isCollisionEnabled());

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

        if (OpticalConfig.FREECAM.getFlightMode() == OpticalConfig.FreecamConfig.FlightMode.CREATIVE) {
            velocity = new Vec3(
                    Mth.lerp(CREATIVE_MOMENTUM_SMOOTHING, velocity.x, targetVelocity.x),
                    Mth.lerp(CREATIVE_MOMENTUM_SMOOTHING, velocity.y, targetVelocity.y),
                    Mth.lerp(CREATIVE_MOMENTUM_SMOOTHING, velocity.z, targetVelocity.z)
            );

            if (targetVelocity.lengthSqr() < 1.0E-6) {
                velocity = velocity.scale(CREATIVE_MOMENTUM_DAMPING);
            }
        } else {
            velocity = targetVelocity;
        }

        cameraEntity.setDeltaMovement(velocity);
        if (OpticalConfig.FREECAM.isCollisionEnabled()) {
            cameraEntity.move(MoverType.SELF, velocity);
        } else {
            cameraEntity.setPos(cameraEntity.getX() + velocity.x, cameraEntity.getY() + velocity.y, cameraEntity.getZ() + velocity.z);
        }
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
            detachedPlayerVisual = null;
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
        Vec3 detachPos = minecraft.gameRenderer.getMainCamera().position();
        cameraEntity.setPos(detachPos.x, detachPos.y, detachPos.z);
        cameraEntity.setYRot(minecraft.player.getYRot());
        cameraEntity.setXRot(minecraft.player.getXRot());
        cameraEntity.yHeadRot = cameraEntity.getYRot();
        cameraEntity.yBodyRot = cameraEntity.getYRot();
        cameraEntity.setCollisionEnabled(OpticalConfig.FREECAM.isCollisionEnabled());
        level.addEntity(cameraEntity);

        ensureDetachedPlayerVisual(minecraft);
        syncDetachedPlayerVisual(minecraft);
        minecraft.setCameraEntity(cameraEntity);

        velocity = Vec3.ZERO;
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

    private static void syncDetachedPlayerVisual(Minecraft minecraft) {
        if (minecraft.player == null || detachedPlayerVisual == null) {
            return;
        }

        detachedPlayerVisual.setPos(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ());
        detachedPlayerVisual.setYRot(minecraft.player.getYRot());
        detachedPlayerVisual.setXRot(minecraft.player.getXRot());
        detachedPlayerVisual.yHeadRot = minecraft.player.yHeadRot;
        detachedPlayerVisual.yBodyRot = minecraft.player.yBodyRot;
    }

    private static void ensureDetachedPlayerVisual(Minecraft minecraft) {
        if (minecraft.player == null || !(minecraft.level instanceof ClientLevel level)) {
            return;
        }

        if (detachedPlayerVisual != null && detachedPlayerVisual.level() == level && !detachedPlayerVisual.isRemoved()) {
            return;
        }

        detachedPlayerVisual = FreecamCameraEntity.detachedVisual(level, minecraft.player.getGameProfile());
        detachedPlayerVisual.setId(DETACHED_PLAYER_VISUAL_ID);
        level.addEntity(detachedPlayerVisual);
    }

}