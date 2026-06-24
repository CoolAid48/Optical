package me.coolaid.optical.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.coolaid.optical.logic.Detached;
import me.coolaid.optical.logic.Freecam;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void optical$hideDetachedPlayerHand(CallbackInfo ci) {
        if (Detached.isActive() || (Freecam.isActive() && !Freecam.shouldRenderPlayerHand())) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void optical$disableViewBobWhenCameraDetached(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (Detached.isActive()) {
            ci.cancel();
        }
    }

}