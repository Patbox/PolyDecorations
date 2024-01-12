package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BaseModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polydecorations.block.other.GenericSingleItemBlockEntity;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class DisplayCaseBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable {
    public static final DirectionProperty FACING = Properties.FACING;

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
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return Blocks.GLASS.getDefaultState();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && !player.isSneaking() && world.getBlockEntity(pos) instanceof GenericSingleItemBlockEntity be && be.getStack().isEmpty() && !player.getStackInHand(hand).isEmpty()) {
            be.setStack(player.getStackInHand(hand).copyWithCount(1));
            player.getStackInHand(hand).decrement(1);
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
        return GenericSingleItemBlockEntity.displayCase(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BaseModel implements GenericSingleItemBlockEntity.ItemSetter {
        private final LodItemDisplayElement main;
        private final LodItemDisplayElement item;
        private final TextDisplayElement text;

        public Model(BlockState state) {
            this.main = LodItemDisplayElement.createSimple(state.getBlock().asItem());
            this.main.setScale(new Vector3f(2));
            this.main.setDisplaySize(1, 1);

            var yaw = state.get(FACING).asRotation();
            this.main.setYaw(yaw);
            this.addElement(this.main);

            this.item = LodItemDisplayElement.createSimple();
            this.item.setViewRange(0.6f);
            this.item.setDisplaySize(1, 1);
            this.item.setModelTransformation(ModelTransformationMode.NONE);
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
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockBound().getBlockState();
                var direction = state.get(FACING).asRotation();
                this.item.setYaw(direction);
            }
        }
        @Override
        public void setItem(ItemStack stack) {
            this.item.setItem(stack.copy());
            if (stack.isIn(DecorationsItemTags.UNSCALED_DISPLAY_CASE)) {
                this.setUnScaled();
            } else {
                this.setDefaultScale();
            }
            if (stack.hasCustomName() || stack.isOf(Items.PLAYER_HEAD)) {
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