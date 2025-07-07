package eu.pb4.polydecorations.block.other;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.model.DecorationsModels;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public class RopeBlock extends Block implements FactoryBlock, PolymerTexturedBlock, CustomBreakingParticleBlock {
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final IntProperty DISTANCE = Properties.DISTANCE_0_7;
    public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
        directions.put(Direction.NORTH, Properties.NORTH);
        directions.put(Direction.EAST, Properties.EAST);
        directions.put(Direction.SOUTH, Properties.SOUTH);
        directions.put(Direction.WEST, Properties.WEST);
        directions.put(Direction.UP, Properties.UP);
        directions.put(Direction.DOWN, Properties.DOWN);
    }));
    private static final BlockState STATE = PolymerBlockResourceUtils.requestEmpty(BlockModelType.VINES_BLOCK);
    private static final ParticleEffect BREAKING_PARTICLE = new ItemStackParticleEffect(ParticleTypes.ITEM, DecorationsModels.ROPE.get(0));
    public RopeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(NORTH, false).with(SOUTH, false)
                .with(EAST, false).with(WEST, false).with(UP, false).with(DOWN, false).with(DISTANCE, 0)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, DISTANCE);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var state = this.getDefaultState();
        var distance = getDistanceAt(ctx.getWorld(), ctx.getBlockPos());
        if (distance > 7) {
            return null;
        }

        for (var direction : Direction.values()) {
            var neighborPos = ctx.getBlockPos().offset(direction);
            var neighbor = ctx.getWorld().getBlockState(neighborPos);
            if (direction != Direction.DOWN && (canConnect(ctx.getWorld(), neighborPos, neighbor, direction.getOpposite()) || canSupport(ctx.getWorld(), neighborPos, neighbor, direction.getOpposite()))) {
                state = state.with(FACING_PROPERTIES.get(direction), true);
            }
        }
        return state.with(DISTANCE, distance);
    }

    private boolean canConnect(WorldView world, BlockPos neighborPos, BlockState neighbor, Direction opposite) {
        if (neighbor.isOf(this)) {
            return true;
        } else if (opposite == Direction.UP && neighbor.getBlock() instanceof LanternBlock) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return getDistanceAt(world, pos) <= 7;
    }

    public int getDistanceAt(WorldView world, BlockPos pos) {
        int distance = 8;
        for (var direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            var neighborPos = pos.offset(direction);
            var neighbor = world.getBlockState(neighborPos);
            if (direction == Direction.UP && neighbor.isOf(this)) {
                distance = Math.min(neighbor.get(DISTANCE), distance);
            } else if (neighbor.isOf(this)) {
                distance = Math.min(neighbor.get(DISTANCE) + 1, distance);
            } else if (canSupport(world, neighborPos, neighbor, direction.getOpposite())) {
                distance = 0;
            }
        }
        return distance;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        tickView.scheduleBlockTick(pos, state.getBlock(), 1);
        return state.with(FACING_PROPERTIES.get(direction), canConnect(world, neighborPos, neighborState, direction.getOpposite()) || canSupport(world, neighborPos, neighborState, direction.getOpposite()));
    }

    private static boolean canSupport(WorldView world, BlockPos neighborPos, BlockState blockState, Direction opposite) {
        return !blockState.isAir() && (!Block.cannotConnect(blockState) && blockState.isSideSolid(world, neighborPos, opposite, SideShapeType.CENTER));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        var d = getDistanceAt(world, pos);
        if (d > 7) {
            world.breakBlock(pos, true);
        } else if (d != state.get(DISTANCE)) {
            world.setBlockState(pos, state.with(DISTANCE, d));
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return STATE;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_FENCE.getDefaultState();
    }

    public static boolean checkModelDirection(BlockState state, Direction direction) {
        return state.get(FACING_PROPERTIES.get(direction));
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public ParticleEffect getBreakingParticle(BlockState blockState) {
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
