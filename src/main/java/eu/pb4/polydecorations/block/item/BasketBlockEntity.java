package eu.pb4.polydecorations.block.item;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.util.DecorationSoundEvents;
import eu.pb4.polydecorations.util.MinimalInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.block.enums.SlabType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class BasketBlockEntity extends LockableBlockEntity implements MinimalInventory, SidedInventory {
    private static final int[] ALL_SLOTS = IntStream.range(0, 5).toArray();
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private final ViewerCountManager stateManager = new ViewerCountManager() {
        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            BasketBlockEntity.this.playSound(DecorationSoundEvents.BASKET_OPEN);
            BasketBlockEntity.this.setOpen(state, true);
        }

        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            BasketBlockEntity.this.playSound(DecorationSoundEvents.BASKET_CLOSE);
            BasketBlockEntity.this.setOpen(state, false);
        }

        protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        }

        @Override
        public boolean isPlayerViewing(PlayerEntity player) {
            return player instanceof ServerPlayerEntity serverPlayer && GuiHelpers.getCurrentGui(serverPlayer) instanceof BasketBlockEntity.Gui gui && gui.isSource(BasketBlockEntity.this);
        }
    };


    public BasketBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.BASKET, blockPos, blockState);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.items);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, this.items);
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
        return ALL_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void onOpen(ContainerUser user) {
        if (!this.removed && !user.asLivingEntity().isSpectator()) {
            this.stateManager.openContainer(user.asLivingEntity(), this.getWorld(), this.getPos(), this.getCachedState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
    }

    @Override
    public void onClose(ContainerUser user) {
        if (!this.removed && !user.asLivingEntity().isSpectator()) {
            this.stateManager.closeContainer(user.asLivingEntity(), this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    public void tick() {
        if (!this.removed) {
            this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    void setOpen(BlockState state, boolean open) {
        //noinspection DataFlowIssue
        this.world.setBlockState(this.getPos(), state.with(BasketBlock.OPEN, open), 3);
    }

    private void playSound(SoundEvent soundEvent) {
        var x = this.pos.getX() + 0.5;
        var z = this.pos.getY() + 1;
        var y = this.pos.getZ() + 0.5;
        //noinspection DataFlowIssue
        this.world.playSound(null, x, z, y, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(this.getStacks()));
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(this.getStacks());
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("Items");
    }

    private class Gui extends SimpleGui {
        public Gui(ServerPlayerEntity player) {
            super(ScreenHandlerType.HOPPER, player, false);
            this.setTitle(BasketBlockEntity.this.getCachedState().getBlock().getName());

            for (int i = 0; i < 5; i++) {
                this.setSlotRedirect(i, new ShulkerBoxSlot(BasketBlockEntity.this, i, 0, 0));
            }

            this.open();
            BasketBlockEntity.this.onOpen(player);
        }

        @Override
        public void onClose() {
            super.onClose();
            BasketBlockEntity.this.onClose(player);
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.getEntityPos().squaredDistanceTo(Vec3d.ofCenter(BasketBlockEntity.this.pos)) > (18 * 18)) {
                this.close();
            }

            super.onTick();
        }

        public boolean isSource(BasketBlockEntity itemStacks) {
            return BasketBlockEntity.this == itemStacks;
        }
    }
}
