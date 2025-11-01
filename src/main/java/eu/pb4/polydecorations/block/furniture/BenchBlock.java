package eu.pb4.polydecorations.block.furniture;

import com.mojang.serialization.MapCodec;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.entity.SeatEntity;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;

public class BenchBlock extends Block implements FactoryBlock, QuickWaterloggable, PolymerTexturedBlock {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<Type> TYPE = EnumProperty.of("type", Type.class);
    public static final BooleanProperty HAS_REST = BooleanProperty.of("has_rest");
    private final ItemStack leftModel;
    private final ItemStack rightModel;
    private final ItemStack middleModel;
    private final Block base;
    private final ItemStack noRestModel;
    private final ItemStack leftNoRestModel;
    private final ItemStack rightNoRestModel;
    private final ItemStack middleNoRestModel;

    public BenchBlock(Settings settings, Identifier identifier, Block planks) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
        this.leftModel = ItemDisplayElementUtil.getModel(identifier.withPrefixedPath("block/").withSuffixedPath("_left"));
        this.rightModel = ItemDisplayElementUtil.getModel(identifier.withPrefixedPath("block/").withSuffixedPath("_right"));
        this.middleModel = ItemDisplayElementUtil.getModel(identifier.withPrefixedPath("block/").withSuffixedPath("_middle"));
        this.noRestModel = ItemDisplayElementUtil.getModel(identifier.withPrefixedPath("block/").withSuffixedPath("_norest"));
        this.leftNoRestModel = ItemDisplayElementUtil.getModel(identifier.withPrefixedPath("block/").withSuffixedPath("_norest_left"));
        this.rightNoRestModel = ItemDisplayElementUtil.getModel(identifier.withPrefixedPath("block/").withSuffixedPath("_norest_right"));
        this.middleNoRestModel = ItemDisplayElementUtil.getModel(identifier.withPrefixedPath("block/").withSuffixedPath("_norest_middle"));
        this.base = planks;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return blockState.get(WATERLOGGED) ? DecorationsUtil.CAMPFIRE_WATERLOGGED_STATE : DecorationsUtil.CAMPFIRE_STATE;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext player) {
        return this.base.getDefaultState();
    }

    public ItemStack getModel(BlockState state) {
        if (state.get(HAS_REST)) {
            return switch (state.get(TYPE)) {
                case BOTH -> ItemDisplayElementUtil.getModel(this.asItem());
                case LEFT -> leftModel;
                case RIGHT -> rightModel;
                case MIDDLE -> middleModel;
            };
        } else {
            return switch (state.get(TYPE)) {
                case BOTH -> noRestModel;
                case LEFT -> leftNoRestModel;
                case RIGHT -> rightNoRestModel;
                case MIDDLE -> middleNoRestModel;
            };
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, HAS_REST, WATERLOGGED);
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
        if (right.isOf(this) && right.get(FACING) == dir && right.get(HAS_REST)) {
            type = Type.RIGHT;
        }

        var left = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(dir.rotateYCounterclockwise()));
        if (left.isOf(this) && left.get(FACING) == dir && left.get(HAS_REST)) {
            type = type == Type.RIGHT ? Type.MIDDLE : Type.LEFT;
        }

        return waterLog(ctx, this.getDefaultState().with(FACING, dir).with(TYPE, type));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.getMainHandStack().isIn(ItemTags.AXES) && state.get(HAS_REST) && CommonProtection.canBreakBlock(world, pos, player.getGameProfile(), player) && player.canModifyBlocks()) {
            world.setBlockState(pos, state.with(HAS_REST, false));
            player.getMainHandStack().damage(1, player, EquipmentSlot.MAINHAND);
            world.playSound(null, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return ActionResult.SUCCESS_SERVER;
        } else if (!player.isSneaking() && SeatEntity.create(world, pos, 1 / 16f, state.get(FACING), player)) {
            return ActionResult.SUCCESS_SERVER;
        }

        return super.onUse(state, world, pos, player, hit);
    }


    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        tickWater(state, world, tickView, pos);
        var facing = state.get(FACING);
        var type = state.get(TYPE);
        var rest = state.get(HAS_REST);

        if (facing.rotateYClockwise() == direction) {
            if (neighborState.isOf(this) && neighborState.get(HAS_REST) == rest && neighborState.get(FACING) == facing) {
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
            if (neighborState.isOf(this) && neighborState.get(HAS_REST) == rest && neighborState.get(FACING) == facing) {
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

    public final class Model extends BlockModel {
        private final ItemDisplayElement main;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(getModel(state));
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            var yaw = state.get(FACING).getPositiveHorizontalDegrees();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = state.get(FACING).getPositiveHorizontalDegrees();
                this.main.setYaw(yaw);
                this.main.setItem(getModel(state));

                this.tick();
            }
        }
    }
}
