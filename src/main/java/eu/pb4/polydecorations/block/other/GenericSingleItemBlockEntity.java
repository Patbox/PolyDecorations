package eu.pb4.polydecorations.block.other;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.util.SingleItemInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public class GenericSingleItemBlockEntity extends BlockEntity implements BlockEntityExtraListener, SingleItemInventory {
    private ItemStack item = ItemStack.EMPTY;
    private ItemSetter model;

    public GenericSingleItemBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public static GenericSingleItemBlockEntity globe(BlockPos pos, BlockState state) {
        return new GenericSingleItemBlockEntity(DecorationsBlockEntities.GLOBE, pos, state);
    }

    public static GenericSingleItemBlockEntity displayCase(BlockPos pos, BlockState state) {
        return new GenericSingleItemBlockEntity(DecorationsBlockEntities.DISPLAY_CASE, pos, state);
    }

    public void setItem(ItemStack item) {
        this.item = item;
        if (this.model != null) {
            this.model.setItem(this.item.copy());
        }
        this.markDirty();
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.item.isEmpty()) {
            view.put("item", ItemStack.OPTIONAL_CODEC, this.item);
        }
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        setItem(view.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        this.model = (ItemSetter) BlockAwareAttachment.get(chunk, this.pos).holder();
        this.model.setItem(this.item.copy());
    }

    public void dropReplaceItem(PlayerEntity player, ItemStack stack, @Nullable Hand hand) {
        if (!this.item.isEmpty()) {
            var out = this.item;
            this.item = ItemStack.EMPTY;
            ItemScatterer.spawn(this.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, out);
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
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }


    public interface ItemSetter {
        void setItem(ItemStack stack);
    }
}
