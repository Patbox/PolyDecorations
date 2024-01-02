package eu.pb4.polydecorations.util;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public interface SingleItemInventory extends SingleStackInventory, SidedInventory {
    int[] SLOTS = new int[] { 0 };

    @Override
    default int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    default boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.getStack().isEmpty();
    }

    @Override
    default boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
