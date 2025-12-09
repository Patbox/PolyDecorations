package eu.pb4.polydecorations.util;

import java.util.Iterator;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface MinimalInventory extends Container {
    List<ItemStack> getStacks();

    @Override
    default int getContainerSize() {
        return getStacks().size();
    }

    @Override
    default boolean isEmpty() {
        for (var stack : this.getStacks()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        return getStacks().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(this.getStacks(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        var itemStack = ContainerHelper.takeItem(this.getStacks(), slot);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }
        return itemStack;
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        this.getStacks().set(slot, stack);
        this.setChanged();
    }


    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default void clearContent() {
        this.getStacks().clear();
    }
}
