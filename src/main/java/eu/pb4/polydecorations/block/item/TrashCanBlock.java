package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.util.DecorationsSoundEvents;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import static eu.pb4.polydecorations.ModInit.id;

public class TrashCanBlock extends BaseEntityBlock implements FactoryBlock, BarrierBasedWaterloggable, CustomBreakingParticleBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<OpenState> FORCE_OPEN = EnumProperty.create("force_open", OpenState.class);
    private final ParticleOptions breakingParticle = new ItemParticleOption(ParticleTypes.ITEM, ItemDisplayElementUtil.getSolidModel(id("block/trashcan_bin")));

    public TrashCanBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(OPEN, false).setValue(FORCE_OPEN, OpenState.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, OPEN, FORCE_OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return waterLog(ctx, this.defaultBlockState().setValue(FACING, ctx.getClickedFace().getAxis() != Direction.Axis.Y ? ctx.getClickedFace() : ctx.getHorizontalDirection().getOpposite()));
    }


    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.IRON_BLOCK.defaultBlockState();
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof TrashCanBlockEntity be) {
            be.openGui((ServerPlayer) player);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (player.isShiftKeyDown() && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.mayBuild()) {
            if (!state.getValue(OPEN)) {
                var x = pos.getX() + 0.5;
                var z = pos.getY() + 1;
                var y = pos.getZ() + 0.5;
                //noinspection DataFlowIssue
                world.playSound(null, x, z, y, state.getValue(FORCE_OPEN) == OpenState.LIDLESS ? DecorationsSoundEvents.TRASHCAN_CLOSE : DecorationsSoundEvents.TRASHCAN_OPEN,
                        SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            }

            world.setBlockAndUpdate(pos, state.cycle(FORCE_OPEN));
            return InteractionResult.SUCCESS_SERVER;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (world.getBlockEntity(pos) instanceof TrashCanBlockEntity be) {
            be.tick();
        }
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
        return new TrashCanBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public ParticleOptions getBreakingParticle(BlockState blockState) {
        return this.breakingParticle;
    }

    public enum OpenState implements StringRepresentable {
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
        public String getSerializedName() {
            return this.id;
        }

        public boolean playSound() {
            return this.playSound;
        }
    }

    public static final class Model extends BlockModel {
        private static final ItemStack BIN = ItemDisplayElementUtil.getSolidModel(id("block/trashcan_bin"));
        private static final ItemStack LID = ItemDisplayElementUtil.getSolidModel(id("block/trashcan_lid"));
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
            var direction = state.getValue(FACING).toYRot();
            this.main.setYaw(direction);
            this.lid.setYaw(direction);
            this.lid.setItem(state.getValue(FORCE_OPEN) == OpenState.LIDLESS ? ItemStack.EMPTY : LID);

            if (state.getValue(OPEN) || state.getValue(FORCE_OPEN) == OpenState.TRUE) {
                this.lid.setTranslation(new Vector3f(0, 0.25f, 0));
                this.lid.setRightRotation(new Quaternionf().rotateX(-Mth.PI / 6));
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
