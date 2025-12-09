package eu.pb4.polydecorations.block.furniture;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.util.SingleItemInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class LongFlowerPotBlockEntity extends BlockEntity implements BlockEntityExtraListener {
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private ItemSetter model;

    public LongFlowerPotBlockEntity(BlockPos pos, BlockState state) {
        super(DecorationsBlockEntities.LONG_FLOWER_POT, pos, state);
    }

    public void setItem(int slot, ItemStack item) {
        this.items.set(slot, item);
        if (this.model != null) {
            this.model.setItem(slot, item);
        }
        this.setChanged();
    }

    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        assert level != null;
        Containers.dropContents(level, pos, this.items);
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
            for (int i = 0; i < this.items.size(); i++) {
                this.model.setItem(i, this.items.get(i));
            }
        }
    }

    @Override
    public void onListenerUpdate(LevelChunk chunk) {
        this.model = (ItemSetter) BlockAwareAttachment.get(chunk, this.worldPosition).holder();
        for (int i = 0; i < this.items.size(); i++) {
            this.model.setItem(i, this.items.get(i));
        }
    }

    public interface ItemSetter {
        void setItem(int slot, ItemStack stack);
    }
}
