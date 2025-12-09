package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.block.extension.WallAttachedLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin {
    @Shadow @Final public static BooleanProperty HANGING;

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void swapByDefault(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (!ctx.isSecondaryUseActive() || ctx.getClickedFace().getAxis() == Direction.Axis.Y) {
            return;
        }
        var pos = ctx.getClickedPos().relative(ctx.getClickedFace(), -1);
        var attachment = WallAttachedLanternBlock.getSupportType(ctx.getLevel(), ctx.getClickedFace(), pos, ctx.getLevel().getBlockState(pos));


        if (attachment == null) {
            return;
        }
        var block = WallAttachedLanternBlock.VANILLA2WALL.get(this);
        if (block == null) {
            return;
        }
        cir.setReturnValue(block.waterLog(ctx, block.defaultBlockState()).setValue(WallAttachedLanternBlock.ATTACHED, attachment)
                .setValue(WallAttachedLanternBlock.FACING, ctx.getClickedFace().getOpposite()));

    }
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void swapNullForWallAttached(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() != null || ctx.getClickedFace().getAxis() == Direction.Axis.Y) {
            return;
        }
        var pos = ctx.getClickedPos().relative(ctx.getClickedFace(), -1);
        var attachment = WallAttachedLanternBlock.getSupportType(ctx.getLevel(), ctx.getClickedFace(), pos, ctx.getLevel().getBlockState(pos));


        if (attachment == null) {
            return;
        }
        var block = WallAttachedLanternBlock.VANILLA2WALL.get(this);
        if (block == null) {
            return;
        }
        cir.setReturnValue(block.waterLog(ctx, block.defaultBlockState()).setValue(WallAttachedLanternBlock.ATTACHED, attachment)
                .setValue(WallAttachedLanternBlock.FACING, ctx.getClickedFace().getOpposite()));

    }

    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void attachToRopes(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getValue(HANGING) && world.getBlockState(pos.relative(Direction.UP)).is(DecorationsBlocks.ROPE)) {
            cir.setReturnValue(true);
        }
    }
}
