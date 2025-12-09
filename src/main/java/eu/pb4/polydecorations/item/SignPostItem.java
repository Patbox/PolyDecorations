package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.DefinedPolymerItem;
import eu.pb4.polydecorations.block.extension.AttachedSignPostBlock;
import eu.pb4.polydecorations.block.extension.SignPostBlockEntity;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SignPostItem extends SimplePolymerItem {
    private String translationKey;

    public SignPostItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var upper = (context.getClickLocation().y() - (int) context.getClickLocation().y()) >= 0.5;
        var blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (AttachedSignPostBlock.MAP.containsKey(blockState.getBlock())) {
            context.getLevel().setBlockAndUpdate(context.getClickedPos(), AttachedSignPostBlock.MAP.get(blockState.getBlock()).defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, blockState.getValue(BlockStateProperties.WATERLOGGED)));
        }

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof SignPostBlockEntity be) {
            var text = be.getText(upper);
            if (text.item() == Items.AIR) {
                var rel = context.getClickLocation().subtract(context.getClickedPos().getCenter());
                var axis = context.getClickedFace().getAxis();
                if (axis == Direction.Axis.Y) {
                    axis = rel.x > rel.z ? Direction.Axis.Z : Direction.Axis.X;
                } else {
                    axis = context.getClickedFace().getClockWise().getAxis();
                }

                var flip = (rel.get(axis) * context.getClickedFace().getAxisDirection().getStep() < 0) == (axis == Direction.Axis.X);

                be.setText(upper, SignPostBlockEntity.Sign.of(context.getItemInHand().getItem(), roundAngle(180 + context.getRotation()), flip));
                        //.withFlip(context.getPlayerYaw() > 90 || context.getPlayerYaw() < -90));
                if (context.getPlayer() instanceof ServerPlayer player) {
                    be.openText(upper, player);
                }
                context.getItemInHand().shrink(1);
            }
        }
        return super.useOn(context);
    }

    public static float roundAngle(float v) {
        return Math.round(v / 15) * 15;
    }
}
