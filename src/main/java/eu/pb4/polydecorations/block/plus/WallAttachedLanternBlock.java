package eu.pb4.polydecorations.block.plus;

import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.factorytools.api.virtualentity.BaseModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
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
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

import static eu.pb4.polydecorations.ModInit.id;

public class WallAttachedLanternBlock extends Block implements PolymerBlock, BlockWithElementHolder, QuickWaterloggable {
    public static final Map<Block, WallAttachedLanternBlock> VANILLA2WALL = new Reference2ObjectOpenHashMap<>();

    private final LanternBlock lantern;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final Property<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<Attached> ATTACHED = EnumProperty.of("attached", Attached.class);

    public WallAttachedLanternBlock(LanternBlock block) {
        super(AbstractBlock.Settings.copy(block).dropsLike(block));
        this.lantern = block;
        VANILLA2WALL.put(block, this);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
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

    public static boolean canSupport(Attached attached, WorldAccess worldAccess, Direction direction, BlockPos neighborPos, BlockState state) {
        return getSupportType(worldAccess, direction, neighborPos, state) == attached;
    }
    @Nullable
    public static Attached getSupportType(WorldAccess worldAccess, Direction direction, BlockPos neighborPos, BlockState state) {
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
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        tickWater(state, world, pos);
        if (direction != state.get(FACING)) {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
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
    public Block getPolymerBlock(BlockState state) {
        return this.lantern;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public final class Model extends BaseModel {
        public static final ItemStack MODEL = BaseItemProvider.requestModel(id("block/lantern_support"));
        public static final ItemStack MODEL_WALL = BaseItemProvider.requestModel(id("block/lantern_support_wall"));
        public static final ItemStack MODEL_FENCE = BaseItemProvider.requestModel(id("block/lantern_support_fence"));
        private final LodItemDisplayElement main;

        private Model(BlockState state) {
            this.main = LodItemDisplayElement.createSimple(model(state));
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
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockBound().getBlockState();
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
