package eu.pb4.polydecorations.block.item;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.block.SimpleParticleBlock;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.util.MinimalInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.sgui.api.gui.SimpleGui;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ShelfBlockEntity extends LockableBlockEntity implements MinimalInventory, WorldlyContainer, BlockEntityExtraListener, SimpleParticleBlock {
    private static final int[] ALL_SLOTS = IntStream.range(0, 6).toArray();
    private static final int[] TOP_SLOTS = IntStream.range(3, 6).toArray();
    private static final int[] BOTTOM_SLOTS = IntStream.range(0, 3).toArray();
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private PlainShelfBlock.Model model;

    public ShelfBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.SHELF, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.items);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ContainerHelper.loadAllItems(view, this.items);
        if (this.model != null) {
            this.model.updateItems(this.getStacks());
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.model != null) {
            this.model.updateItems(this.getStacks());
        }
    }

    @Override
    protected void createGui(ServerPlayer playerEntity) {
        new Gui(playerEntity);
    }

    @Override
    public NonNullList<ItemStack> getStacks() {
        return this.items;
    }

    @Override
    public void onListenerUpdate(LevelChunk chunk) {
        try {
            this.model = (PlainShelfBlock.Model) BlockAwareAttachment.get(chunk, this.getBlockPos()).holder();
            this.model.updateItems(this.items);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (getBlockState().getValue(PlainShelfBlock.TYPE)) {
            case TOP -> TOP_SLOTS;
            case BOTTOM -> BOTTOM_SLOTS;
            case DOUBLE -> ALL_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return switch (getBlockState().getValue(PlainShelfBlock.TYPE)) {
            case TOP -> slot > 2;
            case BOTTOM -> slot < 3;
            case DOUBLE -> true;
        };
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }


    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getStacks()));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        var container = components.get(DataComponents.CONTAINER);
        if (container != null) {
            container.copyInto(this.getStacks());
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        super.removeComponentsFromTag(view);
        view.discard("Items");
    }

    private class Gui extends SimpleGui {
        public BlockState state = getBlockState();

        public Gui(ServerPlayer player) {
            super(getBlockState().getValue(PlainShelfBlock.TYPE) == SlabType.DOUBLE ? MenuType.GENERIC_9x2 : MenuType.GENERIC_9x1, player, false);
            this.setTitle((getBlockState().getValue(PlainShelfBlock.TYPE) == SlabType.DOUBLE ? GuiTextures.SHELF_2 : GuiTextures.SHELF)
                    .apply(ShelfBlockEntity.this.getBlockState().getBlock().getName()));

            switch (getBlockState().getValue(PlainShelfBlock.TYPE)) {
                case BOTTOM -> {
                    this.setSlotRedirect(3, new Slot(ShelfBlockEntity.this, 0, 0, 0));
                    this.setSlotRedirect(4, new Slot(ShelfBlockEntity.this, 1, 1, 0));
                    this.setSlotRedirect(5, new Slot(ShelfBlockEntity.this, 2, 2, 0));
                }
                case TOP -> {
                    this.setSlotRedirect(3, new Slot(ShelfBlockEntity.this, 3, 0, 0));
                    this.setSlotRedirect(4, new Slot(ShelfBlockEntity.this, 4, 1, 0));
                    this.setSlotRedirect(5, new Slot(ShelfBlockEntity.this, 5, 2, 0));
                }
                case DOUBLE -> {
                    this.setSlotRedirect(3, new Slot(ShelfBlockEntity.this, 3, 0, 0));
                    this.setSlotRedirect(4, new Slot(ShelfBlockEntity.this, 4, 1, 0));
                    this.setSlotRedirect(5, new Slot(ShelfBlockEntity.this, 5, 2, 0));
                    this.setSlotRedirect(3 + 9, new Slot(ShelfBlockEntity.this, 0, 0, 0));
                    this.setSlotRedirect(4 + 9, new Slot(ShelfBlockEntity.this, 1, 1, 0));
                    this.setSlotRedirect(5 + 9, new Slot(ShelfBlockEntity.this, 2, 2, 0));
                }
            }


            this.open();
        }

        @Override
        public void onClose() {
            super.onClose();
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.position().distanceToSqr(Vec3.atCenterOf(ShelfBlockEntity.this.worldPosition)) > (18 * 18)) {
                this.close();
            }
            if (this.state != getBlockState()) {
                if (this.state.getValue(PlainShelfBlock.TYPE) != getBlockState().getValue(PlainShelfBlock.TYPE)) {
                    this.close();
                    return;
                }

                this.state = getBlockState();
            }

            super.onTick();
        }
    }
}
