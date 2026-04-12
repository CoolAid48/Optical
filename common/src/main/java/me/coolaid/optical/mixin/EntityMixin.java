package me.coolaid.optical.mixin;

import me.coolaid.optical.CameraOverriddenEntity;
import me.coolaid.optical.Freelook;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements CameraOverriddenEntity {
    @Unique private float cameraPitch;
    @Unique private float cameraYaw;
    @Unique private float anchorYaw;
    @Unique private boolean hasAnchor = false;

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void onTurn(double xDelta, double yDelta, CallbackInfo ci) {
        if (Freelook.isActive() && (Object) this instanceof LocalPlayer) {
            float sensitivity = (float) OpticalConfig.FREELOOK.getSensitivityMultiplier();
            float pDelta = (float) (yDelta * 0.15F * sensitivity);
            float yDelta_ = (float) (xDelta * 0.15F * sensitivity);

            if (!hasAnchor) {
                anchorYaw = this.cameraYaw;
                hasAnchor = true;
            }

            if (OpticalConfig.FREELOOK.isInvertY()) {
                this.cameraPitch = Mth.clamp(this.cameraPitch + pDelta, -90.0f, 90.0f);
            } else {
                this.cameraPitch = Mth.clamp(this.cameraPitch - pDelta, -90.0f, 90.0f);
            }

            float limit = (float) OpticalConfig.FREELOOK.getRotationLimit();

            if (OpticalConfig.FREELOOK.isBetterStyle() || limit >= 360.0f) {
                this.cameraYaw += yDelta_;
            } else {
                this.cameraYaw = Mth.clamp(this.cameraYaw + yDelta_, anchorYaw - limit, anchorYaw + limit);
            }
            ci.cancel();
        } else {
            hasAnchor = false;
        }
    }

    @Override public float optical$getCameraPitch() { return this.cameraPitch; }
    @Override public float optical$getCameraYaw() { return this.cameraYaw; }
    @Override public void optical$setCameraPitch(float p) { this.cameraPitch = p; }
    @Override public void optical$setCameraYaw(float y) { this.cameraYaw = y; this.anchorYaw = y; this.hasAnchor = true; }
}