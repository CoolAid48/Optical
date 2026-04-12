package me.coolaid.optical.mixin;

import me.coolaid.optical.CameraOverriddenEntity;
import me.coolaid.optical.Freelook;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Unique private float optical$cachedYaw;
    @Unique private float optical$cachedBodyYaw;
    @Unique private float optical$cachedHeadYaw;
    @Unique private boolean optical$overrodeYaw;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void optical$applyCameraRelativeMovement(CallbackInfo ci) {
        if (!Freelook.isActive() || !OpticalConfig.FREELOOK.isBetterStyle()) return;
        if (Freelook.getFreeLookPerspective() == CameraType.FIRST_PERSON) return;

        LocalPlayer player = (LocalPlayer) (Object) this;
        CameraOverriddenEntity cameraEntity = (CameraOverriddenEntity) player;

        optical$cachedYaw = player.getYRot();
        optical$cachedBodyYaw = player.yBodyRot;
        optical$cachedHeadYaw = player.getYHeadRot();

        float cameraYaw = cameraEntity.optical$getCameraYaw();

        player.setYRot(cameraYaw);
        player.yBodyRot = cameraYaw;
        player.setYHeadRot(cameraYaw);
        optical$overrodeYaw = true;
    }

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void optical$restoreYawAfterMovement(CallbackInfo ci) {
        if (!optical$overrodeYaw) return;

        LocalPlayer player = (LocalPlayer) (Object) this;

        if (player.xxa != 0 || player.zza != 0) {
            float moveYaw = player.getYRot();
            player.setYRot(moveYaw);
            player.yBodyRot = moveYaw;
            player.setYHeadRot(moveYaw);

            optical$cachedYaw = moveYaw;
            optical$cachedBodyYaw = moveYaw;
            optical$cachedHeadYaw = moveYaw;
        }

        player.setYRot(optical$cachedYaw);
        player.yBodyRot = optical$cachedBodyYaw;
        player.setYHeadRot(optical$cachedHeadYaw);

        optical$overrodeYaw = false;
    }
}