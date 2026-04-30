package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Gamma;
import me.coolaid.optical.util.GammaOverrideState;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public class OptionInstanceMixin {
    @Shadow @Final Component caption;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void optical$overrideGamma(CallbackInfoReturnable<Object> cir) {
        if (!GammaOverrideState.suspendGammaOverride
                && OpticalConfig.BRIGHTNESS.isEnabled()
                && optical$isGammaOption()) {
            cir.setReturnValue(Gamma.getCurrentGamma());
        }
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private void optical$ignoreVanillaGammaSet(Object value, CallbackInfo ci) {
        if (OpticalConfig.BRIGHTNESS.isEnabled() && optical$isGammaOption()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean optical$isGammaOption() {
        if (caption.getContents() instanceof TranslatableContents translatableContents) {
            return "options.gamma".equals(translatableContents.getKey());
        }
        return false;
    }
}