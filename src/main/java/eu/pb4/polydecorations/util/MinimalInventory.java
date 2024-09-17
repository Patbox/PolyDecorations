package eu.pb4.polydecorations.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Iterator;
import java.util.List;

public interface MinimalInventory extends Inventory {
    List<ItemStack> getStacks();

    @Override
    default int size() {
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
    default ItemStack getStack(int slot) {
        return getStacks().get(slot);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.getStacks(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    @Override
    default ItemStack removeStack(int slot) {
        var itemStack = Inventories.removeStack(this.getStacks(), slot);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }
        return itemStack;
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        this.getStacks().set(slot, stack);
        this.markDirty();
    }


    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    default void clear() {
        this.getStacks().clear();
    }
}
