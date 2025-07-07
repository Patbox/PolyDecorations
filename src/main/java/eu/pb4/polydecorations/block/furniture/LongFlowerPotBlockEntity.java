package eu.pb4.polydecorations.block.furniture;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.util.SingleItemInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class LongFlowerPotBlockEntity extends BlockEntity implements BlockEntityExtraListener {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private ItemSetter model;

    public LongFlowerPotBlockEntity(BlockPos pos, BlockState state) {
        super(DecorationsBlockEntities.LONG_FLOWER_POT, pos, state);
    }

    public void setItem(int slot, ItemStack item) {
        this.items.set(slot, item);
        if (this.model != null) {
            this.model.setItem(slot, item);
        }
        this.markDirty();
    }

    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        assert world != null;
        ItemScatterer.spawn(world, pos, this.items);
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
        if (this.model != null) {
            for (int i = 0; i < this.items.size(); i++) {
                this.model.setItem(i, this.items.get(i));
            }
        }
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        this.model = (ItemSetter) BlockAwareAttachment.get(chunk, this.pos).holder();
        for (int i = 0; i < this.items.size(); i++) {
            this.model.setItem(i, this.items.get(i));
        }
    }

    public interface ItemSetter {
        void setItem(int slot, ItemStack stack);
    }
}
