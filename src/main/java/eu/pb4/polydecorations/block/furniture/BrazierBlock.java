package eu.pb4.polydecorations.block.furniture;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import static eu.pb4.polydecorations.util.DecorationsUtil.id;

public class BrazierBlock extends Block implements FactoryBlock, BarrierBasedWaterloggable, CustomBreakingParticleBlock {
    public static final BooleanProperty LIT = Properties.LIT;
    private final ParticleEffect breakingParticle = new ItemStackParticleEffect(ParticleTypes.ITEM, ItemDisplayElementUtil.getModel(id("block/unlit_brazier")));

    public BrazierBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(LIT, true));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.IRON_BARS.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, LIT);
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerWorld serverWorld && projectile.isOnFire() && projectile.canModifyAt(serverWorld, blockPos) && !state.get(LIT) && !state.get(WATERLOGGED)) {
            world.setBlockState(blockPos, state.with(Properties.LIT, true), 11);
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = fluidState.getFluid() == Fluids.WATER;
        return this.getDefaultState().with(WATERLOGGED, bl).with(LIT, !bl);
    }

    @Override
    public ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockState newState = null;

        if (stack.isIn(ItemTags.SHOVELS) && state.get(LIT)) {
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            newState = state.with(LIT, false);
        } else if (!state.get(LIT) && !state.get(CampfireBlock.WATERLOGGED) && stack.isIn(ItemTags.CREEPER_IGNITERS)) {
            world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            newState = state.with(LIT, true);
        }

        if (newState != null) {
            world.setBlockState(pos, newState, 11);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, newState));
            stack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            return ActionResult.SUCCESS_SERVER;
        } else {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
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

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            if (!world.isClient()) {
                if (state.get(LIT)) {
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                }

                world.setBlockState(pos, (BlockState)state.with(Properties.WATERLOGGED, true).with(LIT, false), 3);
                world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public ParticleEffect getBreakingParticle(BlockState blockState) {
        return this.breakingParticle;
    }

    public static final class Model extends BlockModel {
        public static final ItemStack UNLIT = ItemDisplayElementUtil.getModel(id("block/unlit_brazier"));
        private final ItemDisplayElement main;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.get(LIT) ? ItemDisplayElementUtil.getModel(state.getBlock().asItem()) : UNLIT);
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            this.main.setBrightness(state.get(LIT) ? new Brightness(15, 15) : null);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                this.main.setBrightness(state.get(LIT) ? new Brightness(15, 15) : null);
                this.main.setItem(state.get(LIT) ? ItemDisplayElementUtil.getModel(state.getBlock().asItem()) : UNLIT);

                this.tick();
            }
        }
    }
}
