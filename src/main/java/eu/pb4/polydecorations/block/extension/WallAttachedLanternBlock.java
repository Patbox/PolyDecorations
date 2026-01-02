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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;
import java.util.Map;

import static eu.pb4.polydecorations.ModInit.id;

public class WallAttachedLanternBlock extends Block implements PolymerBlock, BlockWithElementHolder, QuickWaterloggable {
    public static final Map<Block, WallAttachedLanternBlock> VANILLA2WALL = new Reference2ObjectOpenHashMap<>();

    private final LanternBlock lantern;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Attached> ATTACHED = EnumProperty.create("attached", Attached.class);

    public WallAttachedLanternBlock(BlockBehaviour.Properties settings, LanternBlock block) {
        super(settings.noOcclusion().overrideLootTable(block.getLootTable()));
        this.lantern = block;
        VANILLA2WALL.put(block, this);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return this.lantern.defaultBlockState().getCloneItemStack(world, pos, includeData);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.lantern.defaultBlockState().setValue(LanternBlock.WATERLOGGED, state.getValue(WATERLOGGED));
    }

    @Override
    public MutableComponent getName() {
        return this.lantern.getName();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, ATTACHED);
    }

    public static boolean canSupport(Attached attached, LevelReader worldAccess, Direction direction, BlockPos neighborPos, BlockState state) {
        return getSupportType(worldAccess, direction, neighborPos, state) == attached;
    }
    @Nullable
    public static Attached getSupportType(LevelReader worldAccess, Direction direction, BlockPos neighborPos, BlockState state) {
        var colShape = state.getCollisionShape(worldAccess, neighborPos);
        if (!Shapes.joinIsNotEmpty(Shapes.block(), colShape.getFaceShape(direction), BooleanOp.NOT_SAME)) {
            return Attached.BLOCK;
        }
        if (state.is(BlockTags.WALLS)) {
            return Attached.WALL;
        }
        if (state.is(BlockTags.FENCES)) {
            return Attached.FENCE;
        }

        return null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        if (direction != state.getValue(FACING)) {
            return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }

        return canSupport(state.getValue(ATTACHED), world, direction.getOpposite(), neighborPos, neighborState) ? state : getFluidState(state).createLegacyBlock();
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public boolean canPathfindThrough(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel {
        public static final ItemStack MODEL = ItemDisplayElementUtil.getSolidModel(id("block/lantern_support"));
        public static final ItemStack MODEL_WALL = ItemDisplayElementUtil.getSolidModel(id("block/lantern_support_wall"));
        public static final ItemStack MODEL_FENCE = ItemDisplayElementUtil.getSolidModel(id("block/lantern_support_fence"));
        private final ItemDisplayElement main;

        private Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(model(state));
            this.main.setYaw(state.getValue(FACING).getOpposite().toYRot());
            this.addElement(main);
        }

        private ItemStack model(BlockState state) {
            return switch (state.getValue(ATTACHED)) {
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
                this.main.setYaw(state.getValue(FACING).getOpposite().toYRot());
                this.tick();
            }
        }
    }

    public enum Attached implements StringRepresentable {
        BLOCK,
        FENCE,
        WALL;

        @Override
        public String getSerializedName() {
            return this.toString().toLowerCase(Locale.ROOT);
        }
    }
}
