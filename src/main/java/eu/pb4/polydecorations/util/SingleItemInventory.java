package eu.pb4.polydecorations.util;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

public interface SingleItemInventory extends ContainerSingleItem, WorldlyContainer {
    int[] SLOTS = new int[] { 0 };

    @Override
    default int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    default boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.getTheItem().isEmpty();
    }

    @Override
    default boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
