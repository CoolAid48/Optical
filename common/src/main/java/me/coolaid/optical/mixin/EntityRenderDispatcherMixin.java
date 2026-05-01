package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Detached;
import me.coolaid.optical.util.FreecamCameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void optical$forceDetachedPlayerRender(Entity entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof FreecamCameraEntity) {
            cir.setReturnValue(false);
        } else if (Detached.isActive() && entity == Minecraft.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
