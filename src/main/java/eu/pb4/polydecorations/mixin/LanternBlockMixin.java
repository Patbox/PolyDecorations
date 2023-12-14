package eu.pb4.polydecorations.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polydecorations.block.plus.WallAttachedLanternBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LanternBlock.class)
public class LanternBlockMixin {

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void swapByDefault(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (!ctx.shouldCancelInteraction() || ctx.getSide().getAxis() == Direction.Axis.Y) {
            return;
        }
        var pos = ctx.getBlockPos().offset(ctx.getSide(), -1);
        var attachment = WallAttachedLanternBlock.getSupportType(ctx.getWorld(), ctx.getSide(), pos, ctx.getWorld().getBlockState(pos));


        if (attachment == null) {
            return;
        }
        var block = WallAttachedLanternBlock.VANILLA2WALL.get(this);
        if (block == null) {
            return;
        }
        cir.setReturnValue(block.waterLog(ctx, block.getDefaultState()).with(WallAttachedLanternBlock.ATTACHED, attachment)
                .with(WallAttachedLanternBlock.FACING, ctx.getSide().getOpposite()));

    }
    @Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    private void swapNullForWallAttached(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() != null || ctx.getSide().getAxis() == Direction.Axis.Y) {
            return;
        }
        var pos = ctx.getBlockPos().offset(ctx.getSide(), -1);
        var attachment = WallAttachedLanternBlock.getSupportType(ctx.getWorld(), ctx.getSide(), pos, ctx.getWorld().getBlockState(pos));


        if (attachment == null) {
            return;
        }
        var block = WallAttachedLanternBlock.VANILLA2WALL.get(this);
        if (block == null) {
            return;
        }
        cir.setReturnValue(block.waterLog(ctx, block.getDefaultState()).with(WallAttachedLanternBlock.ATTACHED, attachment)
                .with(WallAttachedLanternBlock.FACING, ctx.getSide().getOpposite()));

    }
}
