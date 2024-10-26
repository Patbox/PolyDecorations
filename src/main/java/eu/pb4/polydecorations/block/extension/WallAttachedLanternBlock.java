package eu.pb4.polydecorations.block.extension;

import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;
import java.util.Map;

import static eu.pb4.polydecorations.ModInit.id;

public class WallAttachedLanternBlock extends Block implements PolymerBlock, BlockWithElementHolder, QuickWaterloggable {
    public static final Map<Block, WallAttachedLanternBlock> VANILLA2WALL = new Reference2ObjectOpenHashMap<>();

    private final LanternBlock lantern;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final Property<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<Attached> ATTACHED = EnumProperty.of("attached", Attached.class);

    public WallAttachedLanternBlock(AbstractBlock.Settings settings, LanternBlock block) {
        super(settings.nonOpaque().lootTable(block.getLootTableKey()));
        this.lantern = block;
        VANILLA2WALL.put(block, this);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return this.lantern.getPickStack(world, pos, this.lantern.getDefaultState());
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.lantern.getDefaultState().with(LanternBlock.WATERLOGGED, state.get(WATERLOGGED));
    }

    @Override
    public MutableText getName() {
        return this.lantern.getName();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, ATTACHED);
    }

    public static boolean canSupport(Attached attached, WorldView worldAccess, Direction direction, BlockPos neighborPos, BlockState state) {
        return getSupportType(worldAccess, direction, neighborPos, state) == attached;
    }
    @Nullable
    public static Attached getSupportType(WorldView worldAccess, Direction direction, BlockPos neighborPos, BlockState state) {
        var colShape = state.getCollisionShape(worldAccess, neighborPos);
        if (!VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), colShape.getFace(direction), BooleanBiFunction.NOT_SAME)) {
            return Attached.BLOCK;
        }
        if (state.isIn(BlockTags.WALLS)) {
            return Attached.WALL;
        }
        if (state.isIn(BlockTags.FENCES)) {
            return Attached.FENCE;
        }

        return null;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        tickWater(state, world, tickView, pos);
        if (direction != state.get(FACING)) {
            return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }

        return canSupport(state.get(ATTACHED), world, direction.getOpposite(), neighborPos, neighborState) ? state : getFluidState(state).getBlockState();
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel {
        public static final ItemStack MODEL = ItemDisplayElementUtil.getModel(id("block/lantern_support"));
        public static final ItemStack MODEL_WALL = ItemDisplayElementUtil.getModel(id("block/lantern_support_wall"));
        public static final ItemStack MODEL_FENCE = ItemDisplayElementUtil.getModel(id("block/lantern_support_fence"));
        private final ItemDisplayElement main;

        private Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(model(state));
            this.main.setYaw(state.get(FACING).getOpposite().asRotation());
            this.addElement(main);
        }

        private ItemStack model(BlockState state) {
            return switch (state.get(ATTACHED)) {
                case BLOCK -> MODEL;
                case WALL -> MODEL_WALL;
                case FENCE -> MODEL_FENCE;
            };
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                this.main.setItem(model(state));
                this.main.setYaw(state.get(FACING).getOpposite().asRotation());
                this.tick();
            }
        }
    }

    public enum Attached implements StringIdentifiable {
        BLOCK,
        FENCE,
        WALL;

        @Override
        public String asString() {
            return this.toString().toLowerCase(Locale.ROOT);
        }
    }
}
