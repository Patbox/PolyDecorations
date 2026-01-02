package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.block.SimpleParticleBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import static eu.pb4.polydecorations.ModInit.id;

public class MailboxBlock extends BaseEntityBlock implements FactoryBlock, BarrierBasedWaterloggable, SimpleParticleBlock {
    public static final ItemStack FLAG = ItemDisplayElementUtil.getSolidModel(id("block/mailbox_flag"));
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final Block base;

    public MailboxBlock(Properties settings, Block block) {
        super(settings.noOcclusion().strength(block.defaultDestroyTime() + 1f, block.getExplosionResistance() + 2f)
                .isRedstoneConductor(Blocks::never));
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
        this.base = block;
        ModInit.LATE_INIT.add(() -> DecorationsBlockEntities.MAILBOX.addSupportedBlock(this));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (placer instanceof Player player && world.getBlockEntity(pos) instanceof MailboxBlockEntity be) {
            be.setOwner(player.getGameProfile());
        }

    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return waterLog(ctx, this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,  BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof MailboxBlockEntity be) {
            return be.onUse(player);
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        Containers.updateNeighboursAfterDestroy(state, world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MailboxBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return this.base.defaultBlockState();
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement main;
        private final ItemDisplayElement flag;
        private boolean hasMail = false;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSolid(state.getBlock().asItem());
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            var yaw = state.getValue(FACING).toYRot();
            this.main.setYaw(yaw);

            this.flag = ItemDisplayElementUtil.createSimple(FLAG);
            this.flag.setDisplaySize(2, 2);
            this.flag.setYaw(yaw);
            this.flag.setInterpolationDuration(5);
            this.flag.setTranslation(new Vector3f(8.5f / 16f, -2 / 16f, 6 / 16f));
            this.flag.setLeftRotation(new Quaternionf().rotateX(-Mth.HALF_PI));
            this.addElement(this.main);
            this.addElement(this.flag);

        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = state.getValue(FACING).toYRot();
                this.main.setYaw(yaw);
                this.tick();
            }
        }

        public void setHasMail(boolean hasMail) {
            if (this.hasMail == hasMail) {
                return;
            }
            this.hasMail = hasMail;
            this.flag.setLeftRotation(new Quaternionf().rotateX(hasMail ? 0 : -Mth.HALF_PI));
            this.flag.startInterpolation();
            this.flag.tick();
        }
    }
}
