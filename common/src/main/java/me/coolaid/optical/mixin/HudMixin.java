package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public class HudMixin {
    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void optical$usePlayerForHud(CallbackInfoReturnable<Player> cir) {
        if (Freecam.isActive()) {
            cir.setReturnValue(Minecraft.getInstance().player);
        }
    }

    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void optical$hideEquippedOverlayInFreecam(GuiGraphicsExtractor graphics, Identifier texture, float alpha, CallbackInfo ci) {
        if (Freecam.isActive()) {
            ci.cancel();
        }
    }
}
