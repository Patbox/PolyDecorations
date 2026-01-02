package eu.pb4.polydecorations.block.furniture;

import com.mojang.serialization.MapCodec;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.SimpleParticleBlock;
import eu.pb4.polydecorations.entity.SeatEntity;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;

public class BenchBlock extends Block implements FactoryBlock, QuickWaterloggable, PolymerTexturedBlock, SimpleParticleBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);
    public static final BooleanProperty HAS_REST = BooleanProperty.create("has_rest");
    private final ItemStack leftModel;
    private final ItemStack rightModel;
    private final ItemStack middleModel;
    private final Block base;
    private final ItemStack noRestModel;
    private final ItemStack leftNoRestModel;
    private final ItemStack rightNoRestModel;
    private final ItemStack middleNoRestModel;

    public BenchBlock(Properties settings, Identifier identifier, Block planks) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
        this.leftModel = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_left"));
        this.rightModel = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_right"));
        this.middleModel = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_middle"));
        this.noRestModel = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_norest"));
        this.leftNoRestModel = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_norest_left"));
        this.rightNoRestModel = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_norest_right"));
        this.middleNoRestModel = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_norest_middle"));
        this.base = planks;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return blockState.getValue(WATERLOGGED) ? DecorationsUtil.CAMPFIRE_WATERLOGGED_STATE : DecorationsUtil.CAMPFIRE_STATE;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext player) {
        return this.base.defaultBlockState();
    }

    public ItemStack getModel(BlockState state) {
        if (state.getValue(HAS_REST)) {
            return switch (state.getValue(TYPE)) {
                case BOTH -> ItemDisplayElementUtil.getSolidModel(this.asItem());
                case LEFT -> leftModel;
                case RIGHT -> rightModel;
                case MIDDLE -> middleModel;
            };
        } else {
            return switch (state.getValue(TYPE)) {
                case BOTH -> noRestModel;
                case LEFT -> leftNoRestModel;
                case RIGHT -> rightNoRestModel;
                case MIDDLE -> middleNoRestModel;
            };
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, HAS_REST, WATERLOGGED);
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var type = Type.BOTH;

        var dir = ctx.getHorizontalDirection().getOpposite();
        var right = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(dir.getClockWise()));
        if (right.is(this) && right.getValue(FACING) == dir && right.getValue(HAS_REST)) {
            type = Type.RIGHT;
        }

        var left = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(dir.getCounterClockWise()));
        if (left.is(this) && left.getValue(FACING) == dir && left.getValue(HAS_REST)) {
            type = type == Type.RIGHT ? Type.MIDDLE : Type.LEFT;
        }

        return waterLog(ctx, this.defaultBlockState().setValue(FACING, dir).setValue(TYPE, type));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.getMainHandItem().is(ItemTags.AXES) && state.getValue(HAS_REST) && CommonProtection.canBreakBlock(world, pos, player.getGameProfile(), player) && player.mayBuild()) {
            world.setBlockAndUpdate(pos, state.setValue(HAS_REST, false));
            player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            world.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS_SERVER;
        } else if (!player.isShiftKeyDown() && SeatEntity.create(world, pos, 1 / 16f, state.getValue(FACING), player)) {
            return InteractionResult.SUCCESS_SERVER;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }


    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        var facing = state.getValue(FACING);
        var type = state.getValue(TYPE);
        var rest = state.getValue(HAS_REST);

        if (facing.getClockWise() == direction) {
            if (neighborState.is(this) && neighborState.getValue(HAS_REST) == rest && neighborState.getValue(FACING) == facing) {
                state = state.setValue(TYPE, switch (type) {
                    case MIDDLE, LEFT -> Type.MIDDLE;
                    case RIGHT, BOTH -> Type.RIGHT;
                });
            } else {
                state = state.setValue(TYPE, switch (type) {
                    case MIDDLE, LEFT -> Type.LEFT;
                    case RIGHT, BOTH -> Type.BOTH;
                });
            }
        } else if (facing.getCounterClockWise() == direction) {
            if (neighborState.is(this) && neighborState.getValue(HAS_REST) == rest && neighborState.getValue(FACING) == facing) {
                state = state.setValue(TYPE, switch (type) {
                    case MIDDLE, RIGHT -> Type.MIDDLE;
                    case LEFT, BOTH -> Type.LEFT;
                });
            } else {
                state = state.setValue(TYPE, switch (type) {
                    case MIDDLE, RIGHT -> Type.RIGHT;
                    case LEFT, BOTH -> Type.BOTH;
                });
            }
        }
        return state;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public enum Type implements StringRepresentable {
        BOTH,
        LEFT,
        RIGHT,
        MIDDLE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public final class Model extends BlockModel {
        private final ItemDisplayElement main;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(getModel(state));
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            var yaw = state.getValue(FACING).toYRot();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = state.getValue(FACING).toYRot();
                this.main.setYaw(yaw);
                this.main.setItem(getModel(state));

                this.tick();
            }
        }
    }
}
