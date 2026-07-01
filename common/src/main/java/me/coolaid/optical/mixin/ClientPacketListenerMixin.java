package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", at = @At("HEAD"))
    private void optical$disableFreecamBeforeRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        Freecam.deactivate(Minecraft.getInstance());
    }
}
