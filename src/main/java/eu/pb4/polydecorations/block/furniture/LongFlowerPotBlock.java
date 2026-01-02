package eu.pb4.polydecorations.block.furniture;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.mixin.FlowerPotBlockAccessor;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.IdentityHashMap;
import java.util.Map;

import static eu.pb4.polydecorations.ModInit.id;

public class LongFlowerPotBlock extends BaseEntityBlock implements FactoryBlock, PolymerTexturedBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final Map<Block, ItemStack> MODEL_MAP = new IdentityHashMap<>();

    public LongFlowerPotBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState());
    }

    public static void setupResourcesAndMapping() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(LongFlowerPotBlock::createModels);
        RegistryEntryAddedCallback.allEntries(BuiltInRegistries.BLOCK, (blockReference -> {
            if (blockReference.value() instanceof FlowerPotBlock flowerPotBlock) {
                if (flowerPotBlock.getPotted() == Blocks.AIR) {
                    return;
                }
                MODEL_MAP.put(flowerPotBlock.getPotted(),
                        ItemDisplayElementUtil.getSolidModel(id("block/long_flower_pot/" + BuiltInRegistries.BLOCK.getKey(flowerPotBlock.getPotted()).toDebugFileName())));
            }
        }));
    }

    private static void createModels(ResourcePackBuilder builder) {
        for (var entry : FlowerPotBlockAccessor.getPOTTED_BY_CONTENT().entrySet()) {
            if (entry.getKey() == Blocks.AIR) {
                continue;
            }
            var id = BuiltInRegistries.BLOCK.getKey(entry.getKey());
            var output = id("long_flower_pot/" + id.toDebugFileName());
            builder.addData(AssetPaths.blockModel(output), ModelAsset.builder()
                    .parent(BuiltInRegistries.BLOCK.getKey(entry.getValue()).withPrefix("block/"))
                    .texture("flowerpot", "polydecorations:block/empty")
                    .texture("dirt", "polydecorations:block/empty")
                    .build());
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(AXIS, ctx.getHorizontalDirection().getAxis());
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return DecorationsUtil.CAMPFIRE_STATE;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.FLOWER_POT.defaultBlockState();
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.mayBuild()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof LongFlowerPotBlockEntity be) {
            var offsetAxis = state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            var offset = hit.getLocation().get(offsetAxis) - pos.get(offsetAxis);
            int slot = 1;
            if (offset < 1 / 3f) {
                slot = 0;
            } else if (offset > 2 / 3f) {
                slot = 2;
            }

            if (stack.getItem() instanceof BlockItem blockItem && MODEL_MAP.containsKey(blockItem.getBlock())) {
                if (!be.getItem(slot).isEmpty()) {
                    return InteractionResult.CONSUME;
                }
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.awardStat(Stats.POT_FLOWER);
                be.setItem(slot, stack.copyWithCount(1));
                stack.consume(1, player);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.mayBuild()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof LongFlowerPotBlockEntity be) {
            var offsetAxis = state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            var offset = hit.getLocation().get(offsetAxis) - pos.get(offsetAxis);
            int slot = 1;
            if (offset < 1 / 3f) {
                slot = 0;
            } else if (offset > 2 / 3f) {
                slot = 2;
            }

            if (!be.getItem(slot).isEmpty()) {
                var stack = be.getItem(slot);
                be.setItem(slot, ItemStack.EMPTY);

                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return super.useWithoutItem(state, world, pos, player, hit);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LongFlowerPotBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel implements LongFlowerPotBlockEntity.ItemSetter {
        private final ItemDisplayElement main;

        private final ItemDisplayElement[] plants = new ItemDisplayElement[3];

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSolid(state.getBlock().asItem());
            this.main.setScale(new Vector3f(2));
            this.main.setDisplaySize(1, 1);

            var yaw = -state.getValue(AXIS).getPositive().toYRot();
            this.main.setYaw(yaw);
            this.addElement(this.main);

            for (var i = 0; i < 3; i++) {
                this.plants[i] = ItemDisplayElementUtil.createSimple();
                this.plants[i].setViewRange(1);
                this.plants[i].setTranslation(new Vector3f(0.333f * (i - 1), 0, 0));
                this.plants[i].setDisplaySize(1, 1);
                this.plants[i].setItemDisplayContext(ItemDisplayContext.NONE);
                this.plants[i].setYaw(yaw);
                this.addElement(plants[i]);
            }

        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                var yaw = -state.getValue(AXIS).getPositive().toYRot();
                this.main.setYaw(yaw);
                for (var plant : this.plants) {
                    plant.setYaw(yaw);
                }
                this.tick();
            }
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            ItemStack model;
            if (stack.isEmpty()) {
                model = ItemStack.EMPTY;
            } else if (stack.getItem() instanceof BlockItem blockItem) {
                model = MODEL_MAP.get(blockItem.getBlock());
                if (model == null) {
                    model = stack.copy();
                }
            } else {
                model = stack.copy();
            }

            this.plants[slot].setItem(model);
            this.plants[slot].tick();
        }
    }
}
