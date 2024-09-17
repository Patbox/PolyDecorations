package eu.pb4.polydecorations.block.item;

import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.util.DecorationSoundEvents;
import eu.pb4.polydecorations.util.MinimalInventory;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class TrashCanBlockEntity extends LockableBlockEntity implements MinimalInventory, SidedInventory {
    private static final int SIZE = 9 * 4;
    private static final int[] SLOTS = IntStream.range(0, SIZE).toArray();
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
    private final ViewerCountManager stateManager = new ViewerCountManager() {
        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            if (state.get(TrashCanBlock.FORCE_OPEN).playSound()) {
                TrashCanBlockEntity.this.playSound(DecorationSoundEvents.TRASHCAN_OPEN);
            }
            TrashCanBlockEntity.this.setOpen(state, true);
        }

        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            if (state.get(TrashCanBlock.FORCE_OPEN).playSound()) {
                TrashCanBlockEntity.this.playSound(DecorationSoundEvents.TRASHCAN_CLOSE);
            }
            TrashCanBlockEntity.this.setOpen(state, false);
        }

        protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        }

        protected boolean isPlayerViewing(PlayerEntity player) {
            return player instanceof ServerPlayerEntity serverPlayer && GuiHelpers.getCurrentGui(serverPlayer) instanceof Gui gui && gui.isSource(TrashCanBlockEntity.this);
        }
    };

    private int lastClearTick = -1;

    public TrashCanBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.TRASHCAN, blockPos, blockState);
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
    }

    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }

    }
    public void tick() {
        if (!this.removed) {
            this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    void setOpen(BlockState state, boolean open) {
        //noinspection DataFlowIssue
        this.world.setBlockState(this.getPos(), state.with(TrashCanBlock.OPEN, open), 3);
    }

    private void playSound(SoundEvent soundEvent) {
        var x = this.pos.getX() + 0.5;
        var z = this.pos.getY() + 1;
        var y = this.pos.getZ() + 0.5;
        //noinspection DataFlowIssue
        this.world.playSound(null, x, z, y, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        MinimalInventory.super.setStack(slot, stack);
        this.clearOldItems();
    }

    private void clearOldItems() {
        for (var stack : this.getStacks()) {
            if (stack.isEmpty()) {
                return;
            }
        }
        for (int i = 0; i < SIZE - 9; i++) {
            this.items.set(i, this.items.get(i + 9));
        }
        for (int i = SIZE - 9; i < SIZE; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }

        if (this.world != null && this.lastClearTick != (int) this.world.getTime()) {
            playSound(DecorationSoundEvents.TRASHCAN_CLEAR);
            this.lastClearTick = (int) this.world.getTime();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
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
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }


    private class Gui extends SimpleGui {
        public BlockState state = getCachedState();

        public Gui(ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X4, player, false);
            this.setTitle(GuiTextures.TRASHCAN.apply(TrashCanBlockEntity.this.getCachedState().getBlock().getName()));

            for (var i = 0; i < SIZE; i++) {
                this.setSlotRedirect(i, new Slot(TrashCanBlockEntity.this, i, i, 0));
            }

            this.open();
            TrashCanBlockEntity.this.onOpen(player);
        }

        @Override
        public void onClose() {
            super.onClose();
            TrashCanBlockEntity.this.onClose(player);
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.getPos().squaredDistanceTo(Vec3d.ofCenter(TrashCanBlockEntity.this.pos)) > (18 * 18)) {
                this.close();
            }

            super.onTick();
        }

        public boolean isSource(TrashCanBlockEntity trashCanBlockEntity) {
            return TrashCanBlockEntity.this == trashCanBlockEntity;
        }
    }
}
