package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Brightness;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public class OptionInstanceMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void optical$overrideGamma(CallbackInfoReturnable<Object> cir) {
        if (OpticalConfig.BRIGHTNESS.isEnabled()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null && (Object) this == mc.options.gamma()) {
                cir.setReturnValue(Brightness.getCurrentGamma());
            }
        }
    }
}