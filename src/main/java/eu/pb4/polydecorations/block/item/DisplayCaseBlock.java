package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.other.GenericSingleItemBlockEntity;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class DisplayCaseBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable {
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public DisplayCaseBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return waterLog(ctx, this.getDefaultState().with(FACING, ctx.getSide().getAxis() != Direction.Axis.Y ? ctx.getSide() : ctx.getHorizontalPlayerFacing().getOpposite()));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.GLASS.getDefaultState();
    }

    @Override
    public ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.canModifyBlocks()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (hand == Hand.MAIN_HAND && !player.isSneaking() && world.getBlockEntity(pos) instanceof GenericSingleItemBlockEntity be && be.getStack().isEmpty() && !player.getStackInHand(hand).isEmpty()) {
            be.setStack(player.getStackInHand(hand).copyWithCount(1));
            player.getStackInHand(hand).decrement(1);
            return ActionResult.SUCCESS_SERVER;
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
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
        return GenericSingleItemBlockEntity.displayCase(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel implements GenericSingleItemBlockEntity.ItemSetter {
        private final ItemDisplayElement main;
        private final ItemDisplayElement item;
        private final TextDisplayElement text;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            this.main.setScale(new Vector3f(2));
            this.main.setDisplaySize(1, 1);

            var yaw = state.get(FACING).getPositiveHorizontalDegrees();
            this.main.setYaw(yaw);
            this.addElement(this.main);

            this.item = ItemDisplayElementUtil.createSimple();
            this.item.setViewRange(0.6f);
            this.item.setDisplaySize(1, 1);
            this.item.setItemDisplayContext(ItemDisplayContext.NONE);
            this.setDefaultScale();
            this.item.setLeftRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(180));
            this.item.setYaw(yaw);
            this.addElement(item);

            this.text = new TextDisplayElement();
            this.text.setViewRange(0.4f);
            this.text.setDisplaySize(2, 1);
            this.text.setTranslation(new Vector3f(0, 6 / 16f, 0));
            this.text.setScale(new Vector3f(10 / 16f));
            this.text.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
        }

        private void setDefaultScale() {
            this.item.setTranslation(new Vector3f(0, -2 / 16f, 0));
            this.item.setScale(new Vector3f(10 / 16f));
        }

        private void setUnScaled() {
            this.item.setTranslation(new Vector3f(0, 1 / 16f, 0));
            this.item.setScale(new Vector3f(1));
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var direction = state.get(FACING).getPositiveHorizontalDegrees();
                this.item.setYaw(direction);
                this.tick();
            }
        }

        @Override
        public void setItem(ItemStack stack) {
            this.item.setItem(stack.copy());
            this.item.setItemDisplayContext(stack.isIn(DecorationsItemTags.FORCE_FIXED_MODEL) ? ItemDisplayContext.FIXED : ItemDisplayContext.NONE);
            this.item.setLeftRotation(stack.isIn(DecorationsItemTags.FORCE_FIXED_MODEL)
                    ? Direction.UP.getRotationQuaternion()
                    : RotationAxis.NEGATIVE_Y.rotationDegrees(180));
            if (stack.isIn(DecorationsItemTags.UNSCALED_DISPLAY_CASE)) {
                this.setUnScaled();
            } else {
                this.setDefaultScale();
            }
            if (stack.contains(DataComponentTypes.CUSTOM_NAME) || stack.isOf(Items.PLAYER_HEAD)) {
                this.text.setText(stack.getName());
                if (this.getElements().contains(this.text)) {
                    this.text.tick();
                } else {
                    this.addElement(this.text);
                }
            } else {
                this.removeElement(this.text);
            }

            this.item.tick();
        }
    }
}
