package me.coolaid.optical.mixin;

import me.coolaid.optical.util.CameraOverriddenEntity;
import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Freelook;
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

    @Unique
    private float optical$cameraPitch;
    @Unique
    private float optical$cameraYaw;
    @Unique
    private float optical$anchorYaw;
    @Unique
    private boolean optical$hasAnchor = false;

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    public void onTurn(double xDelta, double yDelta, CallbackInfo ci) {
        if ((Object) this instanceof LocalPlayer) {
            if (Freecam.isActive()) {
                Freecam.addLookDelta(xDelta, yDelta);
                ci.cancel();
                return;
            }

            if (Freelook.isActive()) {
                float sensitivity = (float) OpticalConfig.FREELOOK.getSensitivityMultiplier();
                float pDelta = (float) (yDelta * 0.15F * sensitivity);
                float yDelta_ = (float) (xDelta * 0.15F * sensitivity);

                if (!optical$hasAnchor) {
                    optical$anchorYaw = this.optical$cameraYaw;
                    optical$hasAnchor = true;
                }

                if (OpticalConfig.FREELOOK.isInvertY()) {
                    this.optical$cameraPitch = Mth.clamp(this.optical$cameraPitch + pDelta, -90.0f, 90.0f);
                } else {
                    this.optical$cameraPitch = Mth.clamp(this.optical$cameraPitch - pDelta, -90.0f, 90.0f);
                }

                float limit = (float) OpticalConfig.FREELOOK.getRotationLimit();

                if (limit >= 360.0f) {
                    this.optical$cameraYaw += yDelta_;
                } else {
                    this.optical$cameraYaw = Mth.clamp(this.optical$cameraYaw + yDelta_, optical$anchorYaw - limit, optical$anchorYaw + limit);
                }
                ci.cancel();
                return;
            }
        }

        optical$hasAnchor = false;
    }

    @Override
    public float optical$getCameraPitch() { return this.optical$cameraPitch; }
    @Override
    public float optical$getCameraYaw() { return this.optical$cameraYaw; }
    @Override
    public void optical$setCameraPitch(float p) { this.optical$cameraPitch = p; }
    @Override
    public void optical$setCameraYaw(float y) { this.optical$cameraYaw = y; this.optical$anchorYaw = y; this.optical$hasAnchor = true; }
}