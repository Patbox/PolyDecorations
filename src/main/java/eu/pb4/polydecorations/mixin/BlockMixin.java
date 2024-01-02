package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.block.DecorationsBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "cannotConnect", at = @At("HEAD"), cancellable = true)
    private static void dontConnect(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.isIn(DecorationsBlockTags.UNCONNECTABLE)) {
            cir.setReturnValue(true);
        }
    }
}
