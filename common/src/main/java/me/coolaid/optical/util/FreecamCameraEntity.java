package me.coolaid.optical.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

import java.util.UUID;

public final class FreecamCameraEntity extends AbstractClientPlayer {
    public FreecamCameraEntity(ClientLevel level) {
        super(level, new GameProfile(UUID.randomUUID(), "optical_freecam"));
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public void setCollisionEnabled(boolean collisionEnabled) {
        this.noPhysics = !collisionEnabled;
        this.refreshDimensions();
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    @Override
    public boolean isCreative() {
        return true;
    }

    @Override
    public int getUseItemRemainingTicks() {
        if (Minecraft.getInstance().player == null) {
            return super.getUseItemRemainingTicks();
        }
        return Minecraft.getInstance().player.getUseItemRemainingTicks();
    }

    @Override
    public boolean isUsingItem() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem();
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
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean canCollideWith(net.minecraft.world.entity.Entity other) {
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
        if (Minecraft.getInstance().player == null) {
            return super.getAttackAnim(partialTicks);
        }
        return Minecraft.getInstance().player.getAttackAnim(partialTicks);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(0.6F, 1.0F);
    }
}