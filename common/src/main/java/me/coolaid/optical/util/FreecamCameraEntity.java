package me.coolaid.optical.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.UUID;

public final class FreecamCameraEntity extends AbstractClientPlayer {
    public FreecamCameraEntity(ClientLevel level) {
        super(level, new GameProfile(UUID.randomUUID(), "optical_freecam"));
        this.noPhysics = true;
        this.setNoGravity(true);
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
}
