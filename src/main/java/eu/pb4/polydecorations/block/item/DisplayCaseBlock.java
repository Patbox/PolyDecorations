package eu.pb4.polydecorations.block.item;

import com.mojang.math.Axis;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class DisplayCaseBlock extends BaseEntityBlock implements FactoryBlock, BarrierBasedWaterloggable {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public DisplayCaseBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return waterLog(ctx, this.defaultBlockState().setValue(FACING, ctx.getClickedFace().getAxis() != Direction.Axis.Y ? ctx.getClickedFace() : ctx.getHorizontalDirection().getOpposite()));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.GLASS.defaultBlockState();
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.mayBuild()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (hand == InteractionHand.MAIN_HAND && !player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof GenericSingleItemBlockEntity be && be.getTheItem().isEmpty() && !player.getItemInHand(hand).isEmpty()) {
            be.setTheItem(player.getItemInHand(hand).copyWithCount(1));
            player.getItemInHand(hand).shrink(1);
            return InteractionResult.SUCCESS_SERVER;
        }

        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        Containers.updateNeighboursAfterDestroy(state, world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);

        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return GenericSingleItemBlockEntity.displayCase(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel implements GenericSingleItemBlockEntity.ItemSetter {
        private final ItemDisplayElement main;
        private final ItemDisplayElement item;
        private final TextDisplayElement text;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSolid(state.getBlock().asItem());
            this.main.setScale(new Vector3f(2));
            this.main.setDisplaySize(1, 1);

            var yaw = state.getValue(FACING).toYRot();
            this.main.setYaw(yaw);
            this.addElement(this.main);

            this.item = ItemDisplayElementUtil.createSimple();
            this.item.setViewRange(0.6f);
            this.item.setDisplaySize(1, 1);
            this.item.setItemDisplayContext(ItemDisplayContext.NONE);
            this.setDefaultScale();
            this.item.setLeftRotation(Axis.YN.rotationDegrees(180));
            this.item.setYaw(yaw);
            this.addElement(item);

            this.text = new TextDisplayElement();
            this.text.setViewRange(0.4f);
            this.text.setDisplaySize(2, 1);
            this.text.setTranslation(new Vector3f(0, 6 / 16f, 0));
            this.text.setScale(new Vector3f(10 / 16f));
            this.text.setBillboardMode(Display.BillboardConstraints.VERTICAL);
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
                var direction = state.getValue(FACING).toYRot();
                this.item.setYaw(direction);
                this.tick();
            }
        }

        @Override
        public void setItem(ItemStack stack) {
            this.item.setItem(stack.copy());
            this.item.setItemDisplayContext(stack.is(DecorationsItemTags.FORCE_FIXED_MODEL) ? ItemDisplayContext.FIXED : ItemDisplayContext.NONE);
            this.item.setLeftRotation(stack.is(DecorationsItemTags.FORCE_FIXED_MODEL)
                    ? Direction.UP.getRotation()
                    : Axis.YN.rotationDegrees(180));
            if (stack.is(DecorationsItemTags.UNSCALED_DISPLAY_CASE)) {
                this.setUnScaled();
            } else {
                this.setDefaultScale();
            }
            if (stack.has(DataComponents.CUSTOM_NAME) || stack.is(Items.PLAYER_HEAD)) {
                this.text.setText(stack.getHoverName());
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
