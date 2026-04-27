package me.coolaid.optical.mixin;

import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.logic.Brightness;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
    @Redirect(
            method = "extract",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0)
    )
    private float optical$allowNegativeGamma(float first, float second) {
        if (!OpticalConfig.BRIGHTNESS.isEnabled()) {
            return Math.max(first, second);
        }

        if (Brightness.getCurrentGamma() < 0.0D) {
            if (first == 0.0F) {
                return second;
            }
            if (second == 0.0F) {
                return first;
            }
        }

        return Math.max(first, second);
    }
}