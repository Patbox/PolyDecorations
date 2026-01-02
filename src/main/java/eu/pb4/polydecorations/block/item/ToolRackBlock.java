package eu.pb4.polydecorations.block.item;

import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.block.SimpleParticleBlock;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class ToolRackBlock extends BaseEntityBlock implements FactoryBlock, PolymerTexturedBlock, QuickWaterloggable, SimpleParticleBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final Block base;

    public ToolRackBlock(Properties settings, Block base) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
        this.base = base;
        ModInit.LATE_INIT.add(() -> DecorationsBlockEntities.TOOL_RACK.addSupportedBlock(this));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return (blockState.getValue(WATERLOGGED) ? DecorationsUtil.TRAPDOOR_STATES_WATERLOGGED : DecorationsUtil.TRAPDOOR_STATES_REGULAR).get(blockState.getValue(FACING));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return this.base.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return waterLog(ctx, this.defaultBlockState().setValue(FACING,
                ctx.getClickedFace().getAxis() != Direction.Axis.Y ? ctx.getClickedFace() : ctx.getHorizontalDirection().getOpposite()));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof ToolRackBlockEntity be) {
            if (be.checkUnlocked(player)) {
                var dir = state.getValue(FACING);
                var center = Vec3.atCenterOf(pos);
                var neg = new Vec3(-0.5, -0.5, -0.5).yRot(-dir.toYRot() * Mth.DEG_TO_RAD);
                var posi = new Vec3(0.5, 0.5, -0.25).yRot(-dir.toYRot() * Mth.DEG_TO_RAD);
                var eye = player.getEyePosition().subtract(center);
                var off = eye.add(player.getLookAngle().scale(player.blockInteractionRange()));

                var res = new AABB(neg, posi).clip(eye, off);

                if (res.isEmpty()) {
                    return InteractionResult.PASS;
                }
                var target = res.get().yRot(dir.toYRot() * Mth.DEG_TO_RAD);
                var slot = (target.x < 0 ? 0 : 1) + (target.y < 0 ? 0 : 2);

                var currentStack = be.getItem(slot);
                var playerStack = player.getMainHandItem();
                if (currentStack.isEmpty() && !playerStack.isEmpty() && playerStack.is(DecorationsItemTags.TOOL_RACK_ACCEPTABLE)) {
                    be.setItem(slot, playerStack.copyWithCount(1));
                    playerStack.shrink(1);
                    be.setChanged();
                    return InteractionResult.SUCCESS_SERVER;
                } else if (!currentStack.isEmpty() && playerStack.isEmpty()) {
                    be.setItem(slot, ItemStack.EMPTY);
                    player.setItemInHand(InteractionHand.MAIN_HAND, currentStack);
                    be.setChanged();
                    return InteractionResult.SUCCESS_SERVER;
                }

            }

            return InteractionResult.FAIL;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
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
        return new ToolRackBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement main;
        private final ItemDisplayElement[] items = new ItemDisplayElement[4];

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSolid(state.getBlock().asItem());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            this.main.setDisplaySize(1, 1);

            var yaw = state.getValue(FACING).toYRot();
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
                item.setLeftRotation(Axis.YN.rotationDegrees(180));
                item.setYaw(yaw);
                items[i] = item;
                this.addElement(item);
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = state.getValue(FACING).toYRot();
                this.main.setYaw(yaw);
                for (int i = 0; i < 4; i++) {
                    this.items[i].setYaw(yaw);
                }
                this.tick();
            }
        }

        public void setItem(int i, ItemStack stack) {
            this.items[i].setItem(stack.copy());
            this.items[i].setItemDisplayContext(stack.is(DecorationsItemTags.FORCE_FIXED_MODEL) ? ItemDisplayContext.FIXED : ItemDisplayContext.NONE);
            this.items[i].setLeftRotation(stack.is(DecorationsItemTags.FORCE_FIXED_MODEL)
                    ? Direction.UP.getRotation()
                    : Axis.YN.rotationDegrees(180));
            this.items[i].tick();
        }

        public void updateItems(NonNullList<ItemStack> stacks) {
            for (int i = 0; i < 4; i++) {
                setItem(i, stacks.get(i));
            }
        }
    }
}
