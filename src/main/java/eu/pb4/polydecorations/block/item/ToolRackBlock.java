package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class ToolRackBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    private final Block base;

    public ToolRackBlock(Settings settings, Block base) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
        this.base = base;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return this.base.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return waterLog(ctx, this.getDefaultState().with(FACING,
                ctx.getSide().getAxis() != Direction.Axis.Y ? ctx.getSide() : ctx.getHorizontalPlayerFacing().getOpposite()));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.isSneaking() && world.getBlockEntity(pos) instanceof ToolRackBlockEntity be) {
            if (be.checkUnlocked(player)) {
                var dir = state.get(FACING);
                var center = Vec3d.ofCenter(pos);
                var neg = new Vec3d(-0.5, -0.5, -0.5).rotateY(-dir.getPositiveHorizontalDegrees() * MathHelper.RADIANS_PER_DEGREE);
                var posi = new Vec3d(0.5, 0.5, -0.25).rotateY(-dir.getPositiveHorizontalDegrees() * MathHelper.RADIANS_PER_DEGREE);
                var eye = player.getEyePos().subtract(center);
                var off = eye.add(player.getRotationVector().multiply(player.getBlockInteractionRange()));

                var res = new Box(neg, posi).raycast(eye, off);

                if (res.isEmpty()) {
                    return ActionResult.PASS;
                }
                var target = res.get().rotateY(dir.getPositiveHorizontalDegrees() * MathHelper.RADIANS_PER_DEGREE);
                var slot = (target.x < 0 ? 0 : 1) + (target.y < 0 ? 0 : 2);

                var currentStack = be.getStack(slot);
                var playerStack = player.getMainHandStack();
                if (currentStack.isEmpty() && !playerStack.isEmpty() && playerStack.isIn(DecorationsItemTags.TOOL_RACK_ACCEPTABLE)) {
                    be.setStack(slot, playerStack.copyWithCount(1));
                    playerStack.decrement(1);
                    be.markDirty();
                    return ActionResult.SUCCESS_SERVER;
                } else if (!currentStack.isEmpty() && playerStack.isEmpty()) {
                    be.setStack(slot, ItemStack.EMPTY);
                    player.setStackInHand(Hand.MAIN_HAND, currentStack);
                    be.markDirty();
                    return ActionResult.SUCCESS_SERVER;
                }

            }

            return ActionResult.FAIL;
        }

        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        tickWater(state, world, tickView, pos);
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
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
        return new ToolRackBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement main;
        private final ItemDisplayElement[] items = new ItemDisplayElement[4];

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            this.main.setDisplaySize(1, 1);

            var yaw = state.get(FACING).getPositiveHorizontalDegrees();
            this.main.setYaw(yaw);
            this.addElement(this.main);
            for (int i = 0; i < 4; i++) {
                var item = ItemDisplayElementUtil.createSimple();

                var x = i % 2;
                var y = i / 2;

                item.setViewRange(0.6f);
                item.setDisplaySize(1, 1);
                item.setItemDisplayContext(ItemDisplayContext.NONE);
                item.setTranslation(new Vector3f(-3.5f / 16f + x * 7 / 16f, -3.5f / 16f + y * 7 / 16f, -4.5f / 16f));
                item.setScale(new Vector3f(7 / 16f));
                item.setLeftRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(180));
                item.setYaw(yaw);
                items[i] = item;
                this.addElement(item);
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = state.get(FACING).getPositiveHorizontalDegrees();
                this.main.setYaw(yaw);
                for (int i = 0; i < 4; i++) {
                    this.items[i].setYaw(yaw);
                }
                this.tick();
            }
        }

        public void setItem(int i, ItemStack stack) {
            this.items[i].setItem(stack.copy());
            this.items[i].setItemDisplayContext(stack.isIn(DecorationsItemTags.FORCE_FIXED_MODEL) ? ItemDisplayContext.FIXED : ItemDisplayContext.NONE);
            this.items[i].setLeftRotation(stack.isIn(DecorationsItemTags.FORCE_FIXED_MODEL)
                    ? Direction.UP.getRotationQuaternion()
                    : RotationAxis.NEGATIVE_Y.rotationDegrees(180));
            this.items[i].tick();
        }

        public void updateItems(DefaultedList<ItemStack> stacks) {
            for (int i = 0; i < 4; i++) {
                setItem(i, stacks.get(i));
            }
        }
    }
}
