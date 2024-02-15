package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.SlabType;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ShelfBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final EnumProperty<SlabType> TYPE = Properties.SLAB_TYPE;
    private final Block base;
    private final ItemStack topModel;
    private final ItemStack doubleModel;

    public ShelfBlock(Settings settings, Block base, Identifier identifier) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(TYPE, SlabType.BOTTOM));
        this.base = base;
        this.topModel = BaseItemProvider.requestModel(identifier.withPrefixedPath("block/").withSuffixedPath("_top"));
        this.doubleModel = BaseItemProvider.requestModel(identifier.withPrefixedPath("block/").withSuffixedPath("_double"));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return this.base.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState = ctx.getWorld().getBlockState(blockPos);
        if (blockState.isOf(this)) {
            return blockState.with(TYPE, SlabType.DOUBLE);
        } else {
            BlockState blockState2 = waterLog(ctx, this.getDefaultState().with(FACING,
                    ctx.getSide().getAxis() != Direction.Axis.Y ? ctx.getSide() : ctx.getHorizontalPlayerFacing().getOpposite()));
            Direction direction = ctx.getSide();
            return direction != Direction.DOWN && (direction == Direction.UP || !(ctx.getHitPos().y - (double) blockPos.getY() > 0.5)) ? blockState2 : blockState2.with(TYPE, SlabType.TOP);
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        ItemStack itemStack = context.getStack();
        SlabType slabType = state.get(TYPE);
        if (slabType != SlabType.DOUBLE && itemStack.isOf(this.asItem())) {
            if (context.canReplaceExisting()) {
                boolean bl = context.getHitPos().y - (double) context.getBlockPos().getY() > 0.5;
                Direction direction = context.getSide();
                if (slabType == SlabType.BOTTOM) {
                    return direction == Direction.UP || bl && direction.getAxis().isHorizontal();
                } else {
                    return direction == Direction.DOWN || !bl && direction.getAxis().isHorizontal();
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && (state.get(TYPE) == SlabType.DOUBLE || !player.getStackInHand(hand).isOf(this.asItem())) && !player.isSneaking() && world.getBlockEntity(pos) instanceof ShelfBlockEntity be) {
            be.openGui((ServerPlayerEntity) player);
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        tickWater(state, world, pos);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShelfBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public final class Model extends BlockModel {
        private final LodItemDisplayElement main;
        private final LodItemDisplayElement[] items = new LodItemDisplayElement[6];

        public Model(BlockState state) {
            this.main = LodItemDisplayElement.createSimple(this.getModel(state));
            this.main.setScale(new Vector3f(2));
            this.main.setDisplaySize(1, 1);

            var yaw = state.get(FACING).asRotation();
            this.main.setYaw(yaw);
            this.addElement(this.main);
            for (int i = 0; i < 6; i++) {
                var item = LodItemDisplayElement.createSimple();

                var x = i % 3;
                var y = i / 3;

                item.setViewRange(0.6f);
                item.setDisplaySize(1, 1);
                item.setModelTransformation(ModelTransformationMode.NONE);
                item.setTranslation(new Vector3f(-5 / 16f + x * (5 / 16f), -4f / 16f + y * 8 / 16f, -2 / 16f));
                item.setScale(new Vector3f(4.5f / 16f));
                item.setLeftRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(180));
                item.setYaw(yaw);
                items[i] = item;
                this.addElement(item);
            }
        }

        private ItemStack getModel(BlockState state) {
            return switch (state.get(TYPE)) {
                case BOTTOM -> LodItemDisplayElement.getModel(state.getBlock().asItem());
                case DOUBLE -> doubleModel;
                case TOP -> topModel;
            };
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = state.get(FACING).asRotation();
                this.main.setYaw(yaw);
                this.main.setItem(getModel(state));
                for (int i = 0; i < 6; i++) {
                    this.items[i].setYaw(yaw);
                }
                this.tick();
            }
        }

        public void setItem(int i, ItemStack stack) {
            this.items[i].setItem(stack.copy());
            this.items[i].tick();
        }

        public void updateItems(DefaultedList<ItemStack> stacks) {
            for (int i = 0; i < 6; i++) {
                setItem(i, stacks.get(i));
            }
        }
    }
}
