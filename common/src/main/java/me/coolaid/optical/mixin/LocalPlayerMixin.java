package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void optical$markPlayerAsControlledCamera(CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.isActive() && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
