package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.util.DecorationSoundEvents;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Supplier;

import static eu.pb4.polydecorations.ModInit.id;

public class TrashCanBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable, CustomBreakingParticleBlock {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = Properties.OPEN;
    public static final EnumProperty<OpenState> FORCE_OPEN = EnumProperty.of("force_open", OpenState.class);
    private final ParticleEffect breakingParticle = new ItemStackParticleEffect(ParticleTypes.ITEM, ItemDisplayElementUtil.getModel(id("block/trashcan_bin")));

    public TrashCanBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(OPEN, false).with(FORCE_OPEN, OpenState.FALSE));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, OPEN, FORCE_OPEN);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return waterLog(ctx, this.getDefaultState().with(FACING, ctx.getSide().getAxis() != Direction.Axis.Y ? ctx.getSide() : ctx.getHorizontalPlayerFacing().getOpposite()));
    }


    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.IRON_BLOCK.getDefaultState();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.isSneaking() && world.getBlockEntity(pos) instanceof TrashCanBlockEntity be) {
            be.openGui((ServerPlayerEntity) player);
            return ActionResult.SUCCESS_SERVER;
        }

        if (player.isSneaking() && player.getStackInHand(Hand.MAIN_HAND).isEmpty() && player.canModifyBlocks()) {
            if (!state.get(OPEN)) {
                var x = pos.getX() + 0.5;
                var z = pos.getY() + 1;
                var y = pos.getZ() + 0.5;
                //noinspection DataFlowIssue
                world.playSound(null, x, z, y, state.get(FORCE_OPEN) == OpenState.LIDLESS ? DecorationSoundEvents.TRASHCAN_CLOSE : DecorationSoundEvents.TRASHCAN_OPEN,
                        SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            }

            world.setBlockState(pos, state.cycle(FORCE_OPEN));
            return ActionResult.SUCCESS_SERVER;
        }

        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof TrashCanBlockEntity be) {
            be.tick();
        }
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
        return new TrashCanBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public ParticleEffect getBreakingParticle(BlockState blockState) {
        return this.breakingParticle;
    }

    public enum OpenState implements StringIdentifiable {
        FALSE("false", true),
        TRUE("true", false),
        LIDLESS("lidless", false);

        private final boolean playSound;
        private final String id;

        OpenState(String id, boolean playSound) {
            this.id = id;
            this.playSound = playSound;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public boolean playSound() {
            return this.playSound;
        }
    }

    public static final class Model extends BlockModel {
        private static final ItemStack BIN = ItemDisplayElementUtil.getModel(id("block/trashcan_bin"));
        private static final ItemStack LID = ItemDisplayElementUtil.getModel(id("block/trashcan_lid"));
        private final ItemDisplayElement main;
        private final ItemDisplayElement lid;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(BIN);
            this.main.setScale(new Vector3f(2));
            this.main.setDisplaySize(1, 1);

            this.lid = ItemDisplayElementUtil.createSimple();
            this.lid.setScale(new Vector3f(2));
            this.lid.setDisplaySize(1, 1);

            this.updateState(state);
            this.addElement(this.main);
            this.addElement(this.lid);
        }

        private void updateState(BlockState state) {
            var direction = state.get(FACING).getPositiveHorizontalDegrees();
            this.main.setYaw(direction);
            this.lid.setYaw(direction);
            this.lid.setItem(state.get(FORCE_OPEN) == OpenState.LIDLESS ? ItemStack.EMPTY : LID);

            if (state.get(OPEN) || state.get(FORCE_OPEN) == OpenState.TRUE) {
                this.lid.setTranslation(new Vector3f(0, 0.25f, 0));
                this.lid.setRightRotation(new Quaternionf().rotateX(-MathHelper.PI / 6));
            } else {
                this.lid.setTranslation(new Vector3f());
                this.lid.setRightRotation(new Quaternionf());
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                updateState(this.blockState());
                this.tick();
            }
        }
    }
}
