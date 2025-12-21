package eu.pb4.polydecorations.block.item;

import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.util.DecorationsSoundEvents;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasketBlock extends PickableItemContainerBlock {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;


    public BasketBlock(Properties settings) {
        super(settings, DecorationsSoundEvents.BASKET_OPEN, DecorationsSoundEvents.BASKET_CLOSE);
        this.registerDefaultState(this.defaultBlockState().setValue(HANGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return waterLog(ctx, this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()).setValue(HANGING, canHangingOn(ctx.getLevel(), ctx.getClickedPos().above())));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return blockState.getValue(WATERLOGGED) ? DecorationsUtil.CAMPFIRE_WATERLOGGED_STATE : DecorationsUtil.CAMPFIRE_STATE;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_PLANKS.defaultBlockState();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        state = super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        if (direction == Direction.UP) {
            state = state.setValue(HANGING, canHangingOn(world, pos.above()));
        }
        return state;
    }

    private boolean canHangingOn(LevelReader world, BlockPos up) {
        var state = world.getBlockState(up);
        return state.is(DecorationsBlocks.ROPE) || state.getBlock() instanceof ChainBlock && state.getValue(ChainBlock.AXIS) == Direction.Axis.Y;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, initialBlockState);
    }

    public final class Model extends BlockModel {
        private final ItemDisplayElement main;

        public Model(ServerLevel world, BlockState state) {
            var direction = state.getValue(FACING).toYRot();

            this.main = ItemDisplayElementUtil.createSimple(state.getValue(OPEN) || state.getValue(FORCE_OPEN) ? modelOpen : modelClosed);
            this.main.setScale(new Vector3f(2));
            this.main.setTranslation(new Vector3f(0, state.getValue(HANGING) ? 3 / 16f - 0.005f : 0.005f, 0));
            this.main.setYaw(direction);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var direction = state.getValue(FACING).toYRot();
                this.main.setYaw(direction);
                this.main.setTranslation(new Vector3f(0, state.getValue(HANGING) ? 3 / 16f - 0.005f : 0.005f, 0));
                this.main.setItem(state.getValue(OPEN) || state.getValue(FORCE_OPEN) ?  modelOpen : modelClosed);
                this.tick();
            }
        }
    }
}
