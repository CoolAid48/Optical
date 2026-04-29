package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Zoom;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (Freecam.isActive() && !Freecam.shouldRenderPlayerHand()) {
            ci.cancel();
        }
    }
}