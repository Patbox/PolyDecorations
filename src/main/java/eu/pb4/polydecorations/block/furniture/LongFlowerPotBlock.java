package eu.pb4.polydecorations.block.furniture;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.mixin.FlowerPotBlockAccessor;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.IdentityHashMap;
import java.util.Map;

import static eu.pb4.polydecorations.ModInit.id;

public class LongFlowerPotBlock extends BlockWithEntity implements FactoryBlock {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

    private static final Map<Block, ItemStack> MODEL_MAP = new IdentityHashMap<>();

    public LongFlowerPotBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState());
    }

    public static void setupResourcesAndMapping() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(LongFlowerPotBlock::createModels);
        RegistryEntryAddedCallback.allEntries(Registries.BLOCK, (blockReference -> {
            if (blockReference.value() instanceof FlowerPotBlock flowerPotBlock) {
                if (flowerPotBlock.getContent() == Blocks.AIR) {
                    return;
                }
                MODEL_MAP.put(flowerPotBlock.getContent(),
                        ItemDisplayElementUtil.getModel(id("block/long_flower_pot/" + Registries.BLOCK.getId(flowerPotBlock.getContent()).toUnderscoreSeparatedString())));
            }
        }));
    }

    private static void createModels(ResourcePackBuilder builder) {
        for (var entry : FlowerPotBlockAccessor.getCONTENT_TO_POTTED().entrySet()) {
            if (entry.getKey() == Blocks.AIR) {
                continue;
            }
            var id = Registries.BLOCK.getId(entry.getKey());
            var output = id("long_flower_pot/" + id.toUnderscoreSeparatedString());
            builder.addData(AssetPaths.blockModel(output), ModelAsset.builder()
                    .parent(Registries.BLOCK.getId(entry.getValue()).withPrefixedPath("block/"))
                    .texture("flowerpot", "polydecorations:block/empty")
                    .texture("dirt", "polydecorations:block/empty")
                    .build());
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getHorizontalPlayerFacing().getAxis());
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.FLOWER_POT.getDefaultState();
    }

    @Override
    public ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.canModifyBlocks()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!player.isSneaking() && world.getBlockEntity(pos) instanceof LongFlowerPotBlockEntity be) {
            var offsetAxis = state.get(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            var offset = hit.getPos().getComponentAlongAxis(offsetAxis) - pos.getComponentAlongAxis(offsetAxis);
            int slot = 1;
            if (offset < 1 / 3f) {
                slot = 0;
            } else if (offset > 2 / 3f) {
                slot = 2;
            }

            if (stack.getItem() instanceof BlockItem blockItem && MODEL_MAP.containsKey(blockItem.getBlock())) {
                if (!be.getItem(slot).isEmpty()) {
                    return ActionResult.CONSUME;
                }
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.incrementStat(Stats.POT_FLOWER);
                be.setItem(slot, stack.copyWithCount(1));
                stack.decrementUnlessCreative(1, player);
                return ActionResult.SUCCESS_SERVER;
            }
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.canModifyBlocks()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!player.isSneaking() && world.getBlockEntity(pos) instanceof LongFlowerPotBlockEntity be) {
            var offsetAxis = state.get(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            var offset = hit.getPos().getComponentAlongAxis(offsetAxis) - pos.getComponentAlongAxis(offsetAxis);
            int slot = 1;
            if (offset < 1 / 3f) {
                slot = 0;
            } else if (offset > 2 / 3f) {
                slot = 2;
            }

            if (!be.getItem(slot).isEmpty()) {
                var stack = be.getItem(slot);
                be.setItem(slot, ItemStack.EMPTY);

                if (!player.giveItemStack(stack)) {
                    player.dropItem(stack, false);
                }
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return ActionResult.SUCCESS_SERVER;
            }
        }

        return super.onUse(state, world, pos, player, hit);
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

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LongFlowerPotBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel implements LongFlowerPotBlockEntity.ItemSetter {
        private final ItemDisplayElement main;

        private final ItemDisplayElement[] plants = new ItemDisplayElement[3];

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            this.main.setScale(new Vector3f(2));
            this.main.setDisplaySize(1, 1);

            var yaw = -state.get(AXIS).getPositiveDirection().getPositiveHorizontalDegrees();
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
                var yaw = -state.get(AXIS).getPositiveDirection().getPositiveHorizontalDegrees();
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
