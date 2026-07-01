package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Detached;
import me.coolaid.optical.logic.Freecam;
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
    @Inject(method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void optical$suppressProxyAndQueuedPlayerRender(Entity entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        Entity player = Minecraft.getInstance().player;
        if (entity instanceof FreecamCameraEntity) {
            cir.setReturnValue(false);
        } else if ((Detached.isActive() || Freecam.isActive()) && entity == player) {
            cir.setReturnValue(false);
        }
    }
}