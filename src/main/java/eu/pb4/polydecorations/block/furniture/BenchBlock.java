package eu.pb4.polydecorations.block.furniture;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.factorytools.api.virtualentity.BaseModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polydecorations.entity.SeatEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Locale;

public class BenchBlock extends Block implements FactoryBlock, BarrierBasedWaterloggable {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final EnumProperty<Type> TYPE = EnumProperty.of("type", Type.class);
    private final ItemStack leftModel;
    private final ItemStack rightModel;
    private final ItemStack middleModel;

    public BenchBlock(Identifier identifier, Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
        this.leftModel = BaseItemProvider.requestModel(identifier.withPrefixedPath("block/").withSuffixedPath("_left"));
        this.rightModel = BaseItemProvider.requestModel(identifier.withPrefixedPath("block/").withSuffixedPath("_right"));
        this.middleModel = BaseItemProvider.requestModel(identifier.withPrefixedPath("block/").withSuffixedPath("_middle"));
    }

    public ItemStack getModel(BlockState state) {
        return switch (state.get(TYPE)) {
            case BOTH -> LodItemDisplayElement.getModel(this.asItem());
            case LEFT -> leftModel;
            case RIGHT -> rightModel;
            case MIDDLE -> middleModel;
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, WATERLOGGED);
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var type = Type.BOTH;

        var dir = ctx.getHorizontalPlayerFacing().getOpposite();
        var right = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(dir.rotateYClockwise()));
        if (right.isOf(this) && right.get(FACING) == dir) {
            type = Type.RIGHT;
        }

        var left = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(dir.rotateYCounterclockwise()));
        if (left.isOf(this) && left.get(FACING) == dir) {
            type = type == Type.RIGHT ? Type.MIDDLE : Type.LEFT;
        }

        return waterLog(ctx, this.getDefaultState().with(FACING, dir).with(TYPE, type));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && !player.isSneaking() && SeatEntity.create(world, pos, 1 / 16f, state.get(FACING), player)) {
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }


    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        tickWater(state, world, pos);
        var facing = state.get(FACING);
        var type = state.get(TYPE);

        if (facing.rotateYClockwise() == direction) {
            if (neighborState.isOf(this) && state.get(FACING) == facing) {
                state = state.with(TYPE, switch (type) {
                    case MIDDLE, LEFT -> Type.MIDDLE;
                    case RIGHT, BOTH -> Type.RIGHT;
                });
            } else {
                state = state.with(TYPE, switch (type) {
                    case MIDDLE, LEFT -> Type.LEFT;
                    case RIGHT, BOTH -> Type.BOTH;
                });
            }
        } else if (facing.rotateYCounterclockwise() == direction) {
            if (neighborState.isOf(this) && state.get(FACING) == facing) {
                state = state.with(TYPE, switch (type) {
                    case MIDDLE, RIGHT -> Type.MIDDLE;
                    case LEFT, BOTH -> Type.LEFT;
                });
            } else {
                state = state.with(TYPE, switch (type) {
                    case MIDDLE, RIGHT -> Type.RIGHT;
                    case LEFT, BOTH -> Type.BOTH;
                });
            }
        }
        return state;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public enum Type implements StringIdentifiable {
        BOTH,
        LEFT,
        RIGHT,
        MIDDLE;

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public final class Model extends BaseModel {
        private final LodItemDisplayElement main;

        public Model(BlockState state) {
            this.main = LodItemDisplayElement.createSimple(getModel(state));
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            var yaw = state.get(FACING).asRotation();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockBound().getBlockState();
                var yaw = state.get(FACING).asRotation();
                this.main.setYaw(yaw);
                this.main.setItem(getModel(state));

                this.tick();
            }
        }
    }
}
