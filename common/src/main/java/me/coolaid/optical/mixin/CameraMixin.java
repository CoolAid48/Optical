package me.coolaid.optical.mixin;

import me.coolaid.optical.CameraOverriddenEntity;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Freelook;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private Entity entity;
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPosition(double x, double y, double z);

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
}