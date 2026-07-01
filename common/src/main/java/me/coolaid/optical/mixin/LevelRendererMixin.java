package me.coolaid.optical.mixin;

import me.coolaid.optical.logic.Detached;
import me.coolaid.optical.logic.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    protected abstract EntityRenderState extractEntity(Entity entity, float partialTick);

    @Inject(method = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At("RETURN"), require = 0)
    private void optical$extractDetachedPlayer(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState renderState, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if ((!Freecam.isActive() && !Detached.isActive())
                || minecraft.level == null
                || minecraft.player == null
                || minecraft.player.isRemoved()
                || (Freecam.isActive() && Freecam.getCameraEntity() == null)) {
            return;
        }

        TickRateManager tickRateManager = minecraft.level.tickRateManager();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(minecraft.player));
        renderState.entityRenderStates.add(this.extractEntity(minecraft.player, partialTick));
    }
}