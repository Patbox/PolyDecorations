package eu.pb4.polydecorations.block.other;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.util.SingleItemInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        if (!this.item.isEmpty()) {
            view.store("item", ItemStack.OPTIONAL_CODEC, this.item);
        }
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        setItem(view.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    public void onListenerUpdate(LevelChunk chunk) {
        this.model = (ItemSetter) BlockAwareAttachment.get(chunk, this.worldPosition).holder();
        this.model.setItem(this.item.copy());
    }

    public void dropReplaceItem(Player player, ItemStack stack, @Nullable InteractionHand hand) {
        if (!this.item.isEmpty()) {
            var out = this.item;
            this.item = ItemStack.EMPTY;
            Containers.dropItemStack(this.level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, out);
        }

        var cpy = stack.copyWithCount(1);

        if (!stack.isEmpty() && !player.isCreative() && hand != null) {
            stack.shrink(1);
            player.setItemInHand(hand, stack);
        }

        this.setItem(cpy);
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int count) {
        var item = this.item;
        setItem(ItemStack.EMPTY);
        return item;
    }

    @Override
    public void setTheItem(ItemStack stack) {
        setItem(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }


    public interface ItemSetter {
        void setItem(ItemStack stack);
    }
}
