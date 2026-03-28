package eu.pb4.polydecorations.block.extension;

import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.DecorationsBlockTags;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
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

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

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
        var colShape = state.is(DecorationsBlockTags.USE_BASE_SHAPE_OVER_SUPPORT_SHAPE)
                ? state.getShape(worldAccess, neighborPos) : state.getBlockSupportShape(worldAccess, neighborPos);

        var optPoint = colShape.move(-0.5, -0.5, -0.5).closestPointTo(direction.getUnitVec3());
        if (optPoint.isEmpty() ) {
            return null;
        }
        var point = optPoint.get();

        if (Math.abs(point.x) + Math.abs(point.y) + Math.abs(point.z) != Math.abs(point.get(direction.getAxis()))) {
            return null;
        }

        var val = (int) (8 - (direction.getAxisDirection().getStep() * point.get(direction.getAxis())) * 16);
        return val >= 0 && val < Attached.values().length ? Attached.values()[val] : null;
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
        public static final LazyItemStack MODEL[] = Arrays.stream(Attached.values())
                .map(x -> ItemDisplayElementUtil.getModel(id("block/lantern_support/" + x.ordinal())))
                .toArray(LazyItemStack[]::new);
        private final ItemDisplayElement main;

        private Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(model(state));
            this.main.setYaw(state.getValue(FACING).getOpposite().toYRot());
            this.main.setViewRange(4);
            this.addElement(main);
        }

        private ItemStack model(BlockState state) {
            return MODEL[state.getValue(ATTACHED).ordinal()].get();
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
        PIXEL_1,
        PIXEL_2,
        WALL,
        PIXEL_4,
        PIXEL_5,
        FENCE;

        @Override
        public String getSerializedName() {
            return this.toString().toLowerCase(Locale.ROOT);
        }
    }
}
