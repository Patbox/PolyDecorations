package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CeilingHangingSignBlock.class)
public abstract class CeilingHangingSignBlockMixin {
    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void attachToRopes(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world.getBlockState(pos.relative(Direction.UP)).is(DecorationsBlocks.ROPE)) {
            cir.setReturnValue(true);
        }
    }
}
