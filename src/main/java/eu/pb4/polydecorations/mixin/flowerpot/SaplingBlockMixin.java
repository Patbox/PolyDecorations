package eu.pb4.polydecorations.mixin.flowerpot;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SaplingBlock.class)
public class SaplingBlockMixin {
    @Inject(method = "isBonemealSuccess", at = @At("HEAD"), cancellable = true)
    private void canPlaceInPot(Level world, RandomSource random, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (world.getBlockState(pos.below()).is(DecorationsBlocks.LARGE_FLOWER_POT)) {
            cir.setReturnValue(false);
        }
    }
}
