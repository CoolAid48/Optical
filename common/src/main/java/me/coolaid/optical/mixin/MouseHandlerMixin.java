package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Zoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void optical$handleZoomScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        if (Zoom.handleScroll(yOffset)) {
            ci.cancel();
        }
    }
}