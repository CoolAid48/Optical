package me.coolaid.optical.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.coolaid.optical.logic.Detached;
import me.coolaid.optical.logic.Freecam;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void optical$hideDetachedPlayerHand(CameraRenderState cameraState, float partialTick, Matrix4fc pose, CallbackInfo ci) {
        if (Detached.isActive() || (Freecam.isActive() && !Freecam.shouldRenderPlayerHand())) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void optical$disableViewBobWhenCameraDetached(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (Detached.isActive()) {
            ci.cancel();
        }
    }

}
