package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Zoom;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private GuiRenderState guiRenderState;

    @ModifyVariable(method = "extractRenderState", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private boolean optical$hideHudInAlternateZoom(boolean hudVisible) {
        return hudVisible && !Zoom.shouldHideHud();
    }

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;reset()V",
                    shift = At.Shift.AFTER
            )
    )
    private void optical$markHudHiddenInAlternateZoom(DeltaTracker deltaTracker, boolean hudVisible, boolean screensVisible, CallbackInfo ci) {
        if (Zoom.shouldHideHud()) {
            this.guiRenderState.isHudHidden = true;
        }
    }
}
