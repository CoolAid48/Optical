package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Detached;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class CameraTypeMixin {
    @Inject(method = "cycle", at = @At("HEAD"))
    private void optical$disableDetachedCameraOnPerspectiveCycle(CallbackInfoReturnable<CameraType> cir) {
        Detached.deactivate();
    }
}
