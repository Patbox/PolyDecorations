package eu.pb4.polydecorations.block.other;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.model.DecorationsModels;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public class RopeBlock extends Block implements FactoryBlock, PolymerTexturedBlock, CustomBreakingParticleBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
    public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
        directions.put(Direction.NORTH, BlockStateProperties.NORTH);
        directions.put(Direction.EAST, BlockStateProperties.EAST);
        directions.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        directions.put(Direction.WEST, BlockStateProperties.WEST);
        directions.put(Direction.UP, BlockStateProperties.UP);
        directions.put(Direction.DOWN, BlockStateProperties.DOWN);
    }));
    private static final BlockState STATE = PolymerBlockResourceUtils.requestEmpty(BlockModelType.VINES_BLOCK);
    private static final ParticleOptions BREAKING_PARTICLE = new ItemParticleOption(ParticleTypes.ITEM, DecorationsModels.ROPE.get(0));
    public RopeBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false).setValue(DISTANCE, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, DISTANCE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var state = this.defaultBlockState();
        var distance = getDistanceAt(ctx.getLevel(), ctx.getClickedPos());
        if (distance > 7) {
            return null;
        }

        for (var direction : Direction.values()) {
            var neighborPos = ctx.getClickedPos().relative(direction);
            var neighbor = ctx.getLevel().getBlockState(neighborPos);
            if (direction != Direction.DOWN && (canConnect(ctx.getLevel(), neighborPos, neighbor, direction.getOpposite()) || canSupport(ctx.getLevel(), neighborPos, neighbor, direction.getOpposite()))) {
                state = state.setValue(FACING_PROPERTIES.get(direction), true);
            }
        }
        return state.setValue(DISTANCE, distance);
    }

    private boolean canConnect(LevelReader world, BlockPos neighborPos, BlockState neighbor, Direction opposite) {
        if (neighbor.is(this)) {
            return true;
        } else if (opposite == Direction.UP &&
                (neighbor.getBlock() instanceof LanternBlock
                        || neighbor.is(DecorationsBlocks.WIND_CHIME)
                        || neighbor.getBlock() instanceof CeilingHangingSignBlock hangingSignBlock
                )) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return getDistanceAt(world, pos) <= 7;
    }

    public int getDistanceAt(LevelReader world, BlockPos pos) {
        int distance = 8;
        for (var direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            var neighborPos = pos.relative(direction);
            var neighbor = world.getBlockState(neighborPos);
            if (direction == Direction.UP && neighbor.is(this)) {
                distance = Math.min(neighbor.getValue(DISTANCE), distance);
            } else if (neighbor.is(this)) {
                distance = Math.min(neighbor.getValue(DISTANCE) + 1, distance);
            } else if (canSupport(world, neighborPos, neighbor, direction.getOpposite())) {
                distance = 0;
            }
        }
        return distance;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickView.scheduleTick(pos, state.getBlock(), 1);
        return state.setValue(FACING_PROPERTIES.get(direction), canConnect(world, neighborPos, neighborState, direction.getOpposite()) || canSupport(world, neighborPos, neighborState, direction.getOpposite()));
    }

    private static boolean canSupport(LevelReader world, BlockPos neighborPos, BlockState blockState, Direction opposite) {
        return !blockState.isAir() && (!Block.isExceptionForConnection(blockState) && blockState.isFaceSturdy(world, neighborPos, opposite, SupportType.CENTER));
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        var d = getDistanceAt(world, pos);
        if (d > 7) {
            world.destroyBlock(pos, true);
        } else if (d != state.getValue(DISTANCE)) {
            world.setBlockAndUpdate(pos, state.setValue(DISTANCE, d));
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return STATE;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_FENCE.defaultBlockState();
    }

    public static boolean checkModelDirection(BlockState state, Direction direction) {
        return state.getValue(FACING_PROPERTIES.get(direction));
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public ParticleOptions getBreakingParticle(BlockState blockState) {
        return BREAKING_PARTICLE;
    }

    public static class Model extends BlockModel {
        private final ItemDisplayElement main;
        private BlockState state;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple();
            this.main.setViewRange(0.8f);
            this.main.setYaw(180);
            setState(state);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                this.setState(this.blockState());
            }
        }

        protected void setState(BlockState blockState) {
            this.state = blockState;
            updateModel();
        }

        protected void updateModel() {
            this.main.setItem(DecorationsModels.ROPE.get(this.state, RopeBlock::checkModelDirection));

            if (this.main.getHolder() == this) {
                this.main.tick();
            }
        }
    }
}
