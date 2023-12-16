package eu.pb4.polydecorations.block.other;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class GlobeBlockEntity extends BlockEntity implements BlockEntityExtraListener, SingleStackInventory, SidedInventory {
    private static final int[] SLOTS = new int[] { 0 };
    private ItemStack item = ItemStack.EMPTY;
    private GlobeBlock.Model model;

    public GlobeBlockEntity(BlockPos pos, BlockState state) {
        super(DecorationsBlockEntities.GLOBE, pos, state);
    }

    public void setItem(ItemStack item) {
        this.item = item;
        if (this.model != null) {
            this.model.setItem(this.item.copy());
        }
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("item", this.item.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setItem(ItemStack.fromNbt(nbt.getCompound("item")));
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        this.model = (GlobeBlock.Model) BlockBoundAttachment.get(chunk, this.pos).holder();
        this.model.setItem(this.item.copy());
    }

    public void replaceItem(PlayerEntity player, ItemStack stack, @Nullable Hand hand) {
        if (!this.item.isEmpty()) {
            var out = this.item;
            this.item = ItemStack.EMPTY;
            ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, out);
        }

        var cpy = stack.copyWithCount(1);

        if (!stack.isEmpty() && !player.isCreative() && hand != null) {
            stack.decrement(1);
            player.setStackInHand(hand, stack);
        }

        this.setItem(cpy);
    }

    @Override
    public ItemStack getStack() {
        return this.item;
    }

    @Override
    public ItemStack decreaseStack(int count) {
        var item = this.item;
        setItem(ItemStack.EMPTY);
        return item;
    }

    @Override
    public void setStack(ItemStack stack) {
        setItem(stack);
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.item.isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
