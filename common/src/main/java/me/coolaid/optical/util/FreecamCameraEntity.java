package me.coolaid.optical.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

import java.util.UUID;

public final class FreecamCameraEntity extends AbstractClientPlayer {
    public FreecamCameraEntity(ClientLevel level) {
        this(level, new GameProfile(UUID.randomUUID(), "optical_freecam"), true);
    }

    public FreecamCameraEntity(ClientLevel level, GameProfile profile, boolean cameraMode) {
        super(level, profile);
        this.setNoGravity(true);
        this.noPhysics = cameraMode;
    }

    public static FreecamCameraEntity detachedVisual(ClientLevel level, GameProfile profile) {
        return new FreecamCameraEntity(level, profile, false);
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