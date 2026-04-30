package me.coolaid.optical.mixin;

import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.util.CameraOverriddenEntity;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Freelook;
import me.coolaid.optical.logic.Zoom;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Entity entity;
    @Shadow private float eyeHeightOld;
    @Shadow private float eyeHeight;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setEntity", at = @At("HEAD"))
    private void optical$syncEyeHeightImmediately(Entity newEntity, CallbackInfo ci) {
        if (newEntity instanceof me.coolaid.optical.util.FreecamCameraEntity || this.entity instanceof me.coolaid.optical.util.FreecamCameraEntity) {
            this.eyeHeightOld = this.eyeHeight = newEntity.getEyeHeight();
        }
    }

    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1, shift = At.Shift.AFTER))
    public void optical$applyFreelookRotation(float f, CallbackInfo ci) {
        if (Freelook.isActive() && this.entity instanceof CameraOverriddenEntity ce) {
            this.setRotation(ce.optical$getCameraYaw(), ce.optical$getCameraPitch());
        }

        if (Freecam.isActive()) {
            this.setRotation(Freecam.getYaw(), Freecam.getPitch());
            this.setPosition(Freecam.getPosition().x, Freecam.getPosition().y, Freecam.getPosition().z);
        }
    }

    @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
    private void optical$hideSubmersionFogInFreecam(CallbackInfoReturnable<FogType> cir) {
        if (Freecam.isActive() && !OpticalConfig.FREECAM.isCollisionEnabled()) {
            cir.setReturnValue(FogType.NONE);
        }
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void optical$applyZoomToWorldFov(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float) Zoom.applyZoomFov(cir.getReturnValueF()));
    }

    @Inject(method = "calculateHudFov", at = @At("RETURN"), cancellable = true)
    private void optical$applyZoomToHudFov(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float) Zoom.applyZoomFov(cir.getReturnValueF()));
    }
}