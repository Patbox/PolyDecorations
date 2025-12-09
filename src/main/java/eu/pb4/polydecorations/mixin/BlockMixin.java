package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.block.DecorationsBlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "isExceptionForConnection", at = @At("HEAD"), cancellable = true)
    private static void dontConnect(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(DecorationsBlockTags.UNCONNECTABLE)) {
            cir.setReturnValue(true);
        }
    }
}
