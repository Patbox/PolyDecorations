package eu.pb4.polydecorations.mixin.flowerpot;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlantBlock.class, CropBlock.class, PitcherCropBlock.class})
public class PlantBlockMixin {
    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void canPlaceInPot(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(DecorationsBlocks.LARGE_FLOWER_POT)) {
            cir.setReturnValue(true);
        }
    }
}
