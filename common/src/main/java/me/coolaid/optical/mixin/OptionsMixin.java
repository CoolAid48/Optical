package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.util.GammaOverrideState;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "setCameraType(Lnet/minecraft/client/CameraType;)V", at = @At("HEAD"), cancellable = true)
    private void optical$lockConfiguredPerspectiveInFreecam(CallbackInfo ci) {
        if (Freecam.isActive() && !Freecam.isChangingCameraType()) {
            ci.cancel();
        }
    }

    @Inject(method = "processDumpedOptions", at = @At("HEAD"), require = 0)
    private void optical$suspendGammaOverrideDuringOptionDump(CallbackInfo ci) {
        GammaOverrideState.suspendGammaOverride = true;
    }

    @Inject(method = "processDumpedOptions", at = @At("RETURN"), require = 0)
    private void optical$resumeGammaOverrideAfterOptionDump(CallbackInfo ci) {
        GammaOverrideState.suspendGammaOverride = false;
    }
}