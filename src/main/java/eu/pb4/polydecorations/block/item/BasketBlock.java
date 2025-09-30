package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

import static eu.pb4.polydecorations.util.DecorationsUtil.id;

public class BasketBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable {
    public static final Identifier CONTENTS_DYNAMIC_DROP_ID = ShulkerBoxBlock.CONTENTS_DYNAMIC_DROP_ID;

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = Properties.OPEN;
    public static final BooleanProperty HANGING = Properties.HANGING;

    public BasketBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(OPEN, false).with(HANGING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, OPEN, HANGING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return waterLog(ctx, this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(HANGING, canHangingOn(ctx.getWorld(), ctx.getBlockPos().up())));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.isSneaking() && world.getBlockEntity(pos) instanceof BasketBlockEntity be) {
            be.openGui((ServerPlayerEntity) player);
            return ActionResult.SUCCESS_SERVER;
        }

        return ActionResult.PASS;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof BasketBlockEntity be) {
            be.tick();
        }
    }


    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockEntity(pos) instanceof BasketBlockEntity be) {
            if (!world.isClient() && player.shouldSkipBlockDrops() && !be.isEmpty()) {
                var itemStack = this.asItem().getDefaultStack();
                itemStack.applyComponentsFrom(be.createComponentMap());
                ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            } else {
                //be.generateLoot(player);
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof ShulkerBoxBlockEntity be) {
            builder = builder.addDynamicDrop(CONTENTS_DYNAMIC_DROP_ID, (lootConsumer) -> {
                for (int i = 0; i < be.size(); ++i) {
                    lootConsumer.accept(be.getStack(i));
                }

            });
        }

        return super.getDroppedStacks(state, builder);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_PLANKS.getDefaultState();
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        tickWater(state, world, tickView, pos);
        state = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        if (direction == Direction.UP) {
            state = state.with(HANGING, canHangingOn(world, pos.up()));
        }
        return state;
    }

    private boolean canHangingOn(WorldView world, BlockPos up) {
        var state = world.getBlockState(up);

        return state.isOf(DecorationsBlocks.ROPE) || state.getBlock() instanceof ChainBlock && state.get(ChainBlock.AXIS) == Direction.Axis.Y;
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, initialBlockState);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BasketBlockEntity(pos, state);
    }

    public static final class Model extends BlockModel {
        public static final ItemStack BASKET_OPEN = ItemDisplayElementUtil.getModel(id("block/basket_open"));
        public static final ItemStack BASKET_CLOSE = ItemDisplayElementUtil.getModel(id("block/basket_closed"));
        private final ItemDisplayElement main;

        public Model(ServerWorld world, BlockState state) {
            var direction = state.get(FACING).getPositiveHorizontalDegrees();

            this.main = ItemDisplayElementUtil.createSimple(state.get(OPEN) ? BASKET_OPEN : BASKET_CLOSE);
            this.main.setScale(new Vector3f(2));
            this.main.setTranslation(new Vector3f(0, state.get(HANGING) ? 3 / 16f - 0.005f : 0.005f, 0));
            this.main.setYaw(direction);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var direction = state.get(FACING).getPositiveHorizontalDegrees();
                this.main.setYaw(direction);
                this.main.setTranslation(new Vector3f(0, state.get(HANGING) ? 3 / 16f - 0.005f : 0.005f, 0));
                this.main.setItem(state.get(OPEN) ? BASKET_OPEN : BASKET_CLOSE);
                this.tick();
            }
        }
    }
}
