package me.coolaid.optical.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.util.FreecamCameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Unique
    private float optical$tickDelta;

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"))
    private float optical$redirectHandPitch(LocalPlayer player, float partialTick) {
        FreecamCameraEntity camera = Freecam.getCameraEntity();
        return Freecam.isActive() && camera != null ? camera.getViewXRot(partialTick) : player.getViewXRot(partialTick);
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"))
    private float optical$redirectHandYaw(LocalPlayer player, float partialTick) {
        FreecamCameraEntity camera = Freecam.getCameraEntity();
        return Freecam.isActive() && camera != null ? camera.getViewYRot(partialTick) : player.getViewYRot(partialTick);
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;xBob:F", opcode = Opcodes.GETFIELD))
    private float optical$redirectXBob(LocalPlayer player) {
        FreecamCameraEntity camera = Freecam.getCameraEntity();
        return Freecam.isActive() && camera != null ? camera.xBob : player.xBob;
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;xBobO:F", opcode = Opcodes.GETFIELD))
    private float optical$redirectXBobO(LocalPlayer player) {
        FreecamCameraEntity camera = Freecam.getCameraEntity();
        return Freecam.isActive() && camera != null ? camera.xBobO : player.xBobO;
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;yBob:F", opcode = Opcodes.GETFIELD))
    private float optical$redirectYBob(LocalPlayer player) {
        FreecamCameraEntity camera = Freecam.getCameraEntity();
        return Freecam.isActive() && camera != null ? camera.yBob : player.yBob;
    }

    @Redirect(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;yBobO:F", opcode = Opcodes.GETFIELD))
    private float optical$redirectYBobO(LocalPlayer player) {
        FreecamCameraEntity camera = Freecam.getCameraEntity();
        return Freecam.isActive() && camera != null ? camera.yBobO : player.yBobO;
    }

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void optical$storeTickDelta(float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int packedLight, CallbackInfo ci) {
        this.optical$tickDelta = partialTick;
    }

    @ModifyVariable(method = "renderHandsWithItems", at = @At("HEAD"), argsOnly = true)
    private int optical$lightHandsFromFreecam(int lightCoords) {
        FreecamCameraEntity camera = Freecam.getCameraEntity();
        if (Freecam.isActive() && camera != null) {
            return Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera, this.optical$tickDelta);
        }
        return lightCoords;
    }
}
