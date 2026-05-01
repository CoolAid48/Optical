package me.coolaid.optical.mixin;

import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.util.FreecamCameraEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {
    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void optical$ignoreCollisionForFreecam(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (Freecam.isActive()
                && !OpticalConfig.FREECAM.isCollisionEnabled()
                && context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof FreecamCameraEntity) {
            cir.setReturnValue(Shapes.empty());
        }
    }
}
