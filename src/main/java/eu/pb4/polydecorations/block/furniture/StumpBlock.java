package eu.pb4.polydecorations.block.furniture;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.SimpleParticleBlock;
import eu.pb4.polydecorations.entity.SeatEntity;
import eu.pb4.polydecorations.mixin.PropertiesAccessor;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.awt.*;
import java.util.Map;

public class StumpBlock extends Block implements FactoryBlock, BarrierBasedWaterloggable, SimpleParticleBlock {
    public static final BooleanProperty TALL = BooleanProperty.create("tall");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final Map<Direction, VoxelShape> REGULAR_SHAPE = createShape(10);
    private static final Map<Direction, VoxelShape> TALL_SHAPE = createShape(16);
    private static final Map<Direction, VoxelShape> SUPPORT_SHAPE = createShape(1);

    private final Block base;
    private final LazyItemStack regularModel;
    private final LazyItemStack tallModel;

    public StumpBlock(Properties settings, Block log) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(TALL, false).setValue(FACING, Direction.UP));
        this.base = log;
        var modelId = ((PropertiesAccessor) settings).getId().identifier().withPrefix("block/");
        this.regularModel = ItemDisplayElementUtil.getModel(modelId);
        this.tallModel = ItemDisplayElementUtil.getModel(modelId.withSuffix("_tall"));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext player) {
        return this.base.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, TALL, FACING);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return (blockState.getValue(TALL) ? TALL_SHAPE : REGULAR_SHAPE).get(blockState.getValue(FACING));
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return TALL_SHAPE.get(blockState.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var dir = ctx.getClickedFace();
        return waterLog(ctx, this.defaultBlockState()).setValue(FACING, dir).setValue(TALL, Shapes.joinIsNotEmpty(
                ctx.getLevel().getBlockState(ctx.getClickedPos().relative(dir)).getShape(ctx.getLevel(), ctx.getClickedPos().relative(dir)),
                SUPPORT_SHAPE.get(dir), BooleanOp.AND));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && (!state.getValue(TALL) || state.getValue(FACING).getAxis() != Direction.Axis.Y)
                && SeatEntity.create(world, pos,  state.getValue(FACING).getAxis() == Direction.Axis.Y ? 3 / 16f : 5 / 16f, null, player)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }


    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        if (direction == state.getValue(FACING)) {
            state = state.setValue(TALL, Shapes.joinIsNotEmpty(neighborState.getShape(world, neighborPos), SUPPORT_SHAPE.get(direction), BooleanOp.AND));
        }
        return state;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, pos);
    }

    private static Map<Direction, VoxelShape> createShape(int i) {
        return Shapes.rotateAll(Block.column(10, 0, i), OctahedralGroup.BLOCK_ROT_X_90, new Vec3(0.5, 0.5, 0.5));
    }

    public final class Model extends BlockModel {
        private final ItemDisplayElement main;

        public Model(BlockState state, BlockPos pos) {
            this.main = ItemDisplayElementUtil.createSimple();
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            this.main.setYaw(RandomSource.create(pos.hashCode()).nextInt(4) * 90);
            this.main.setLeftRotation(new Quaternionf().rotateX(Mth.HALF_PI));
            this.updateState(state);
            this.addElement(this.main);
        }

        private void updateState(BlockState state) {
            this.main.setItem(state.getValue(TALL) ? tallModel.get() : regularModel.get());
            var dir = state.getValue(FACING);

            if (dir.getAxis() == Direction.Axis.Y) {
                this.main.setYaw(0);
                this.main.setPitch(-90 * dir.getAxisDirection().getStep());
            } else {
                this.main.setYaw(dir.toYRot());
                this.main.setPitch(0);
            }

        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            super.notifyUpdate(updateType);
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                this.updateState(this.blockState());
                this.main.tick();
            }
        }
    }
}
