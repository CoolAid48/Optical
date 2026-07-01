package me.coolaid.optical.mixin;

import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.logic.Freecam;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getFrictionInfluencedSpeed(F)F", at = @At("HEAD"), cancellable = true, require = 0)
    private void optical$useConfiguredCreativeFreecamSpeed(float friction, CallbackInfoReturnable<Float> cir) {
        if (Freecam.isActive()
                && Freecam.getCameraEntity() == (Object) this
                && OpticalConfig.FREECAM.getFlightMode() == OpticalConfig.FreecamConfig.FlightMode.CREATIVE) {
            double speed = OpticalConfig.FREECAM.getHorizontalSpeed() / 10.0D;
            cir.setReturnValue((float) speed * (Freecam.getCameraEntity().isSprinting() ? 2.0F : 1.0F));
        }
    }
}
