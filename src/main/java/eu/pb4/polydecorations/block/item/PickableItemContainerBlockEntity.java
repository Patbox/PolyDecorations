package eu.pb4.polydecorations.block.item;

import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import eu.pb4.polydecorations.util.MinimalInventory;
import eu.pb4.sgui.api.GuiHelpers;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class PickableItemContainerBlockEntity extends LockableBlockEntity implements MinimalInventory, WorldlyContainer {
    private static final int[] ALL_SLOTS = IntStream.range(0, 5).toArray();
    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private final ContainerOpenersCounter stateManager = new ContainerOpenersCounter() {
        protected void onOpen(Level world, BlockPos pos, BlockState state) {
            PickableItemContainerBlockEntity.this.playSound(((PickableItemContainerBlock) state.getBlock()).openSoundEvent);
            PickableItemContainerBlockEntity.this.setOpen(state, true);
        }

        protected void onClose(Level world, BlockPos pos, BlockState state) {
            PickableItemContainerBlockEntity.this.playSound(((PickableItemContainerBlock) state.getBlock()).closeSoundEvent);
            PickableItemContainerBlockEntity.this.setOpen(state, false);
        }

        protected void openerCountChanged(Level world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player instanceof ServerPlayer serverPlayer && GuiHelpers.getCurrentGui(serverPlayer) instanceof PickableItemContainerBlockEntity.Gui gui && gui.isSource(PickableItemContainerBlockEntity.this);
        }
    };

    public PickableItemContainerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.GENERIC_PICKABLE_STORAGE, blockPos, blockState);
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
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.getItem().canFitInsideContainerItems();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            if (this.components().has(DecorationsDataComponents.TIED)) {
                this.setComponents(DataComponentMap.builder().addAll(this.components()).set(DecorationsDataComponents.TIED, null).build());
            }
            this.stateManager.incrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            this.stateManager.decrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void tick() {
        if (!this.remove) {
            this.stateManager.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    void setOpen(BlockState state, boolean open) {
        //noinspection DataFlowIssue
        this.level.setBlock(this.getBlockPos(), state.setValue(PickableItemContainerBlock.OPEN, open), 3);
    }

    private void playSound(SoundEvent soundEvent) {
        if (this.getBlockState().getValue(PickableItemContainerBlock.FORCE_OPEN)) {
            return;
        }
        var x = this.worldPosition.getX() + 0.5;
        var y = this.worldPosition.getY() + 0.8;
        var z = this.worldPosition.getZ() + 0.5;
        //noinspection DataFlowIssue
        this.level.playSound(null, x, y, z, soundEvent, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getStacks()));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.getStacks());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        super.removeComponentsFromTag(view);
        view.discard("Items");
    }

    private class Gui extends SimpleGui {
        public Gui(ServerPlayer player) {
            super(MenuType.HOPPER, player, false);
            this.setTitle(PickableItemContainerBlockEntity.this.getBlockState().getBlock().getName());

            for (int i = 0; i < 5; i++) {
                this.setSlotRedirect(i, new ShulkerBoxSlot(PickableItemContainerBlockEntity.this, i, 0, 0));
            }

            this.open();
            PickableItemContainerBlockEntity.this.startOpen(player);
        }

        @Override
        public void onClose() {
            super.onClose();
            PickableItemContainerBlockEntity.this.stopOpen(player);
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.position().distanceToSqr(Vec3.atCenterOf(PickableItemContainerBlockEntity.this.worldPosition)) > (18 * 18)) {
                this.close();
            }

            super.onTick();
        }

        public boolean isSource(PickableItemContainerBlockEntity itemStacks) {
            return PickableItemContainerBlockEntity.this == itemStacks;
        }
    }
}
