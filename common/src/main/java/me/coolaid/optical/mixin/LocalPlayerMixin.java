package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Freecam;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.Minecraft.getInstance;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void optical$markPlayerAsControlledCamera(CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.isActive() && (Object) this == getInstance().player) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getViewXRot", at = @At("HEAD"), cancellable = true)
    private void optical$useFreecamPitch(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (Freecam.isActive()) {
            cir.setReturnValue(Freecam.getPitch());
        }
    }

    @Inject(method = "getViewYRot", at = @At("HEAD"), cancellable = true)
    private void optical$useFreecamYaw(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (Freecam.isActive()) {
            cir.setReturnValue(Freecam.getYaw());
        }
    }
}