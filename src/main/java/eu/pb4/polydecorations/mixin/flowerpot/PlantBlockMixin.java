package eu.pb4.polydecorations.mixin.flowerpot;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({VegetationBlock.class, CropBlock.class, PitcherCropBlock.class})
public class PlantBlockMixin {
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void canPlaceInPot(BlockState floor, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(DecorationsBlocks.LARGE_FLOWER_POT)) {
            cir.setReturnValue(true);
        }
    }
}
