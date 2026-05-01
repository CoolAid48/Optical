package me.coolaid.optical.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.coolaid.optical.logic.Detached;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Zoom;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private GameRenderState gameRenderState;

    @Inject(method = "extractOptions", at = @At("TAIL"))
    private void optical$hideHudInAlternateZoom(CallbackInfo ci) {
        if (Zoom.shouldHideHud()) {
            this.gameRenderState.optionsRenderState.hideGui = true;
        }
    }

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

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void optical$hideBlockOutlineInFreecam(CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.isActive() && Freecam.shouldPreventInteractions()) {
            cir.setReturnValue(false);
        }
    }
}
