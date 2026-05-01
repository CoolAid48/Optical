package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Detached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void optical$preventAttackInFreecam(CallbackInfoReturnable<Boolean> cir) {
        if (optical$disableInteract()) {
            cir.cancel();
        }
    }

    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    private void optical$preventPickInFreecam(CallbackInfo ci) {
        if (optical$disableInteract()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "pick(F)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity optical$pickFromPlayerWhenFreecamBlocksInteraction(Entity entity) {
        if (Freecam.isActive()) {
            return Minecraft.getInstance().player;
        }
        return entity;
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void optical$preventBlockBreakingInFreecam(CallbackInfo ci) {
        if (optical$disableInteract()) {
            ci.cancel();
        }
    }

    @Inject(method = "disconnect*", at = @At("HEAD"))
    private void optical$disableFreecamOnDisconnect(CallbackInfo ci) {
        Freecam.onDisconnect((Minecraft) (Object) this);
        Detached.onDisconnect();
    }

    @Redirect(
            method = "setCameraEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V"),
            require = 0
    )
    private void optical$suppressFreecamAllChanged(LevelRenderer levelRenderer) {
        if (!Freecam.isSwitchingCameraEntity()) {
            levelRenderer.allChanged();
        }
    }

    @Redirect(
            method = "setCameraEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;needsUpdate()V"),
            require = 0
    )
    private void optical$suppressFreecamNeedsUpdate(LevelRenderer levelRenderer) {
        if (!Freecam.isSwitchingCameraEntity()) {
            levelRenderer.needsUpdate();
        }
    }

    @Unique
    private static boolean optical$disableInteract() {
        return Freecam.isActive() && Freecam.shouldPreventInteractions();
    }
}
