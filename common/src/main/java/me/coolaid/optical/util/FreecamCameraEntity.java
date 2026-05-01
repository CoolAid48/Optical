package me.coolaid.optical.util;

import com.mojang.authlib.GameProfile;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class FreecamCameraEntity extends AbstractClientPlayer {
    private static final double DIAGONAL_MULTIPLIER = Mth.sin((float) Math.toRadians(45));

    public ClientInput input;
    public float yBob;
    public float xBob;
    public float yBobO;
    public float xBobO;

    public FreecamCameraEntity(ClientLevel level) {
        super(level, new GameProfile(UUID.randomUUID(), "optical_freecam"));
        this.setPose(Pose.SWIMMING);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.getAbilities().flying = true;
        this.input = new KeyboardInput(Minecraft.getInstance().options);
    }

    public void setCollisionEnabled(boolean collisionEnabled) {
        this.noPhysics = !collisionEnabled;
        this.refreshDimensions();
    }

    @Override
    public void tick() {
        this.input.tick();
        this.doFreecamMotion();
        super.tick();
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        this.xBob = this.getXRot();
        this.yBob = this.getYRot();
        this.xBobO = this.xBob;
        this.yBobO = this.yBob;
    }

    @Override
    public int getUseItemRemainingTicks() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return super.getUseItemRemainingTicks();
        }
        return player.getUseItemRemainingTicks();
    }

    @Override
    public boolean isUsingItem() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.isUsingItem();
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public MobEffectInstance getEffect(Holder<MobEffect> effect) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return super.getEffect(effect);
        }
        return player.getEffect(effect);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return OpticalConfig.FREECAM.isCollisionEnabled() ? PushReaction.NORMAL : PushReaction.IGNORE;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public void setPose(Pose pose) {
        super.setPose(Pose.SWIMMING);
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    protected boolean updateIsUnderwater() {
        this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
        return this.wasUnderwater;
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    public float getAttackAnim(float partialTicks) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return super.getAttackAnim(partialTicks);
        }
        return player.getAttackAnim(partialTicks);
    }

    @Override
    public float getViewXRot(float partialTick) {
        return this.getXRot();
    }

    @Override
    public float getViewYRot(float partialTick) {
        return this.getYRot();
    }

    @Override
    public boolean isEffectiveAi() {
        return true;
    }

    @Override
    public boolean canSimulateMovement() {
        return true;
    }

    @Override
    protected void applyInput() {
        Vec2 moveVector = this.input.getMoveVector();
        if (moveVector.lengthSquared() != 0.0F) {
            moveVector = moveVector.scale(0.98F);
        }

        this.xxa = moveVector.x;
        this.zza = moveVector.y;
        this.jumping = this.input.keyPresses.jump();
        this.setSprinting((Minecraft.getInstance().options.keySprint.isDown() && this.input.keyPresses.forward())
                || (this.input.keyPresses.forward() && this.isSprinting()));

        this.yBobO = this.yBob;
        this.xBobO = this.xBob;
        this.xBob += (this.getXRot() - this.xBob) * 0.5F;
        this.yBob += (this.getYRot() - this.yBob) * 0.5F;
    }

    private void doFreecamMotion() {
        if (OpticalConfig.FREECAM.getFlightMode() == OpticalConfig.FreecamConfig.FlightMode.CREATIVE) {
            this.getAbilities().setFlyingSpeed((float) OpticalConfig.FREECAM.getVerticalSpeed() / 10.0F);
            if (this.input.keyPresses.shift() ^ this.input.keyPresses.jump()) {
                int direction = this.input.keyPresses.jump() ? 1 : -1;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, direction * this.getAbilities().getFlyingSpeed() * 3.0F, 0.0D));
            }
        } else {
            this.getAbilities().setFlyingSpeed(0.0F);
            this.doDefaultMotion();
        }

        this.getAbilities().flying = true;
        this.setOnGround(false);
    }

    private void doDefaultMotion() {
        float yaw = this.getYRot();
        double hSpeed = OpticalConfig.FREECAM.getHorizontalSpeed() * (this.isSprinting() ? 1.5D : 1.0D);
        double vSpeed = OpticalConfig.FREECAM.getVerticalSpeed();
        double velocityX = 0.0D;
        double velocityY = 0.0D;
        double velocityZ = 0.0D;

        Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 side = Vec3.directionFromRotation(0.0F, yaw + 90.0F);

        boolean straight = false;
        if (this.input.keyPresses.forward()) {
            velocityX += forward.x * hSpeed;
            velocityZ += forward.z * hSpeed;
            straight = true;
        }

        if (this.input.keyPresses.backward()) {
            velocityX -= forward.x * hSpeed;
            velocityZ -= forward.z * hSpeed;
            straight = true;
        }

        boolean strafing = false;
        if (this.input.keyPresses.right()) {
            velocityX += side.x * hSpeed;
            velocityZ += side.z * hSpeed;
            strafing = true;
        }

        if (this.input.keyPresses.left()) {
            velocityX -= side.x * hSpeed;
            velocityZ -= side.z * hSpeed;
            strafing = true;
        }

        if (straight && strafing) {
            velocityX *= DIAGONAL_MULTIPLIER;
            velocityZ *= DIAGONAL_MULTIPLIER;
        }

        if (this.input.keyPresses.jump()) {
            velocityY += vSpeed;
        }

        if (this.input.keyPresses.shift()) {
            velocityY -= vSpeed;
        }

        this.setDeltaMovement(velocityX, velocityY, velocityZ);
    }
}
