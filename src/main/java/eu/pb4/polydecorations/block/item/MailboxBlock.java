package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.ItemUseLimiter;
import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static eu.pb4.polydecorations.ModInit.id;

public class MailboxBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable, ItemUseLimiter.All {
    public static final ItemStack FLAG = BaseItemProvider.requestModel(id("block/mailbox_flag"));
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    private final Block base;

    public MailboxBlock(Block block) {
        super(Settings.copy(block).nonOpaque().strength(block.getHardness() + 1f, block.getBlastResistance() + 2f)
                .solidBlock(Blocks::never));
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
        this.base = block;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (placer instanceof PlayerEntity player && world.getBlockEntity(pos) instanceof MailboxBlockEntity be) {
            be.setOwner(player.getGameProfile());
        }

    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return waterLog(ctx, this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,  BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof MailboxBlockEntity be) {
            return be.onUse(player);
        }

        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        tickWater(state, world, pos);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof MailboxBlockEntity be) {
            for (var value : be.inventories.values()) {
                ItemScatterer.spawn(world, pos, value);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MailboxBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return this.base.getDefaultState();
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement main;
        private final ItemDisplayElement flag;
        private boolean hasMail = false;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            var yaw = state.get(FACING).asRotation();
            this.main.setYaw(yaw);

            this.flag = ItemDisplayElementUtil.createSimple(FLAG);
            this.flag.setDisplaySize(2, 2);
            this.flag.setYaw(yaw);
            this.flag.setInterpolationDuration(5);
            this.flag.setTranslation(new Vector3f(8.5f / 16f, -2 / 16f, 6 / 16f));
            this.flag.setLeftRotation(new Quaternionf().rotateX(-MathHelper.HALF_PI));
            this.addElement(this.main);
            this.addElement(this.flag);

        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = state.get(FACING).asRotation();
                this.main.setYaw(yaw);
                this.tick();
            }
        }

        public void setHasMail(boolean hasMail) {
            if (this.hasMail == hasMail) {
                return;
            }
            this.hasMail = hasMail;
            this.flag.setLeftRotation(new Quaternionf().rotateX(hasMail ? 0 : -MathHelper.HALF_PI));
            this.flag.startInterpolation();
            this.flag.tick();
        }
    }
}
