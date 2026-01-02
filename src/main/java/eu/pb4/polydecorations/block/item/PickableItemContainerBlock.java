package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.mixin.PropertiesAccessor;
import eu.pb4.polydecorations.util.DecorationsSoundEvents;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class PickableItemContainerBlock extends BaseEntityBlock implements FactoryBlock, QuickWaterloggable, PolymerTexturedBlock {
    public static final Identifier CONTENTS_DYNAMIC_DROP_ID = ShulkerBoxBlock.CONTENTS;

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty FORCE_OPEN = BooleanProperty.create("force_open");


    public final SoundEvent openSoundEvent;
    public final SoundEvent closeSoundEvent;
    protected final ItemStack modelOpen;
    protected final ItemStack modelClosed;

    public PickableItemContainerBlock(Properties settings, SoundEvent openSoundEvent, SoundEvent closeSoundEvent) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false)
                .setValue(OPEN, false).setValue(FORCE_OPEN, false));
        this.openSoundEvent = openSoundEvent;
        this.closeSoundEvent = closeSoundEvent;

        var id = ((PropertiesAccessor) settings).getId().identifier();

        this.modelOpen = ItemDisplayElementUtil.getSolidModel(id.withPrefix("block/").withSuffix("_open"));
        this.modelClosed = ItemDisplayElementUtil.getSolidModel(id.withPrefix("block/").withSuffix("_closed"));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, OPEN, FORCE_OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return waterLog(ctx, this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof PickableItemContainerBlockEntity be) {
            be.openGui((ServerPlayer) player);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (player.isShiftKeyDown() && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.mayBuild()) {
            if (!state.getValue(OPEN)) {
                var x = pos.getX() + 0.5;
                var y = pos.getY() + 0.8;
                var z = pos.getZ() + 0.5;
                //noinspection DataFlowIssue
                world.playSound(null, x, y, z, state.getValue(FORCE_OPEN) && !state.getValue(OPEN) ? closeSoundEvent : openSoundEvent,
                        SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            }

            world.setBlockAndUpdate(pos, state.cycle(FORCE_OPEN));
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (world.getBlockEntity(pos) instanceof PickableItemContainerBlockEntity be) {
            be.tick();
        }
    }


    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (world.getBlockEntity(pos) instanceof PickableItemContainerBlockEntity be) {
            if (!world.isClientSide() && player.preventsBlockDrops() && !be.isEmpty()) {
                var itemStack = this.asItem().getDefaultInstance();
                itemStack.applyComponents(be.collectComponents());
                ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
                itemEntity.setDefaultPickUpDelay();
                world.addFreshEntity(itemEntity);
            } else {
                //be.generateLoot(player);
            }
        }

        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ShulkerBoxBlockEntity be) {
            builder = builder.withDynamicDrop(CONTENTS_DYNAMIC_DROP_ID, (lootConsumer) -> {
                for (int i = 0; i < be.getContainerSize(); ++i) {
                    lootConsumer.accept(be.getItem(i));
                }

            });
        }

        return super.getDrops(state, builder);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.BARRIER.defaultBlockState().setValue(WATERLOGGED, blockState.getValue(WATERLOGGED));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_PLANKS.defaultBlockState();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    private boolean canHangingOn(LevelReader world, BlockPos up) {
        var state = world.getBlockState(up);

        return state.is(DecorationsBlocks.ROPE) || state.getBlock() instanceof ChainBlock && state.getValue(ChainBlock.AXIS) == Direction.Axis.Y;
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        Containers.updateNeighboursAfterDestroy(state, world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, initialBlockState);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PickableItemContainerBlockEntity(pos, state);
    }

    public final class Model extends BlockModel {

        private final ItemDisplayElement main;

        public Model(ServerLevel world, BlockState state) {
            var direction = state.getValue(FACING).toYRot();

            this.main = ItemDisplayElementUtil.createSimple(state.getValue(OPEN) || state.getValue(FORCE_OPEN) ? modelOpen : modelClosed);
            this.main.setTranslation(new Vector3f(0, 1 / 64f, 0));
            this.main.setScale(new Vector3f(2));
            this.main.setYaw(direction);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var direction = state.getValue(FACING).toYRot();
                this.main.setYaw(direction);
                this.main.setItem(state.getValue(OPEN) || state.getValue(FORCE_OPEN) ? modelOpen : modelClosed);
                this.tick();
            }
        }
    }
}
