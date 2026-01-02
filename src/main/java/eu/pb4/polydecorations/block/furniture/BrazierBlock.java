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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Brightness;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import static eu.pb4.polydecorations.util.DecorationsUtil.id;

public class BrazierBlock extends Block implements FactoryBlock, BarrierBasedWaterloggable, CustomBreakingParticleBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private final ParticleOptions breakingParticle = new ItemParticleOption(ParticleTypes.ITEM, ItemDisplayElementUtil.getSolidModel(id("block/unlit_brazier")));

    public BrazierBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(LIT, true));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.IRON_BARS.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, LIT);
    }

    @Override
    public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerLevel serverWorld && projectile.isOnFire() && projectile.mayInteract(serverWorld, blockPos) && !state.getValue(LIT) && !state.getValue(WATERLOGGED)) {
            world.setBlock(blockPos, state.setValue(BlockStateProperties.LIT, true), 11);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        boolean bl = fluidState.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, bl).setValue(LIT, !bl);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockState newState = null;

        if (stack.is(ItemTags.SHOVELS) && state.getValue(LIT)) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            newState = state.setValue(LIT, false);
        } else if (!state.getValue(LIT) && !state.getValue(CampfireBlock.WATERLOGGED) && stack.is(ItemTags.CREEPER_IGNITERS)) {
            world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            newState = state.setValue(LIT, true);
        }

        if (newState != null) {
            world.setBlock(pos, newState, 11);
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            return InteractionResult.SUCCESS_SERVER;
        } else {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
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

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!world.isClientSide()) {
                if (state.getValue(LIT)) {
                    world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    world.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
                }

                world.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.WATERLOGGED, true).setValue(LIT, false), 3);
                world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public ParticleOptions getBreakingParticle(BlockState blockState) {
        return this.breakingParticle;
    }

    public static final class Model extends BlockModel {
        public static final ItemStack UNLIT = ItemDisplayElementUtil.getSolidModel(id("block/unlit_brazier"));
        private final ItemDisplayElement main;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getValue(LIT) ? ItemDisplayElementUtil.getSolidModel(state.getBlock().asItem()) : UNLIT);
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            this.main.setBrightness(state.getValue(LIT) ? new Brightness(15, 15) : null);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                this.main.setBrightness(state.getValue(LIT) ? new Brightness(15, 15) : null);
                this.main.setItem(state.getValue(LIT) ? ItemDisplayElementUtil.getSolidModel(state.getBlock().asItem()) : UNLIT);

                this.tick();
            }
        }
    }
}
