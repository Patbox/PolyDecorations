package eu.pb4.polydecorations.block.item;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.util.MinimalInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class ShelfBlockEntity extends LockableBlockEntity implements MinimalInventory, SidedInventory, BlockEntityExtraListener {
    private static final int[] ALL_SLOTS = IntStream.range(0, 6).toArray();
    private static final int[] TOP_SLOTS = IntStream.range(3, 6).toArray();
    private static final int[] BOTTOM_SLOTS = IntStream.range(0, 3).toArray();
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private ShelfBlock.Model model;

    public ShelfBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.SHELF, blockPos, blockState);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        Inventories.writeNbt(nbt, this.items, lookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        Inventories.readNbt(nbt, this.items, lookup);
        if (this.model != null) {
            this.model.updateItems(this.getStacks());
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.model != null) {
            this.model.updateItems(this.getStacks());
        }
    }

    @Override
    protected void createGui(ServerPlayerEntity playerEntity) {
        new Gui(playerEntity);
    }

    @Override
    public DefaultedList<ItemStack> getStacks() {
        return this.items;
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        try {
            this.model = (ShelfBlock.Model) BlockAwareAttachment.get(chunk, this.getPos()).holder();
            this.model.updateItems(this.items);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (getCachedState().get(ShelfBlock.TYPE)) {
            case TOP -> TOP_SLOTS;
            case BOTTOM -> BOTTOM_SLOTS;
            case DOUBLE -> ALL_SLOTS;
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return switch (getCachedState().get(ShelfBlock.TYPE)) {
            case TOP -> slot > 2;
            case BOTTOM -> slot < 3;
            case DOUBLE -> true;
        };
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }


    private class Gui extends SimpleGui {
        public BlockState state = getCachedState();

        public Gui(ServerPlayerEntity player) {
            super(getCachedState().get(ShelfBlock.TYPE) == SlabType.DOUBLE ? ScreenHandlerType.GENERIC_9X2 : ScreenHandlerType.GENERIC_9X1, player, false);
            this.setTitle((getCachedState().get(ShelfBlock.TYPE) == SlabType.DOUBLE ? GuiTextures.SHELF_2 : GuiTextures.SHELF)
                    .apply(ShelfBlockEntity.this.getCachedState().getBlock().getName()));

            switch (getCachedState().get(ShelfBlock.TYPE)) {
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
            if (isRemoved() || player.getPos().squaredDistanceTo(Vec3d.ofCenter(ShelfBlockEntity.this.pos)) > (18 * 18)) {
                this.close();
            }
            if (this.state != getCachedState()) {
                if (this.state.get(ShelfBlock.TYPE) != getCachedState().get(ShelfBlock.TYPE)) {
                    this.close();
                    return;
                }

                this.state = getCachedState();
            }

            super.onTick();
        }
    }
}
