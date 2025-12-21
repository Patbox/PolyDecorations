package eu.pb4.polydecorations.patch;

import eu.pb4.polydecorations.item.DecorationsDataComponents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

public class DecorationsLootTablePatches {
    public static void register() {
        LootTableEvents.MODIFY_DROPS.register(DecorationsLootTablePatches::modifyDrops);
    }

    private static void modifyDrops(Holder<LootTable> lootTableHolder, LootContext lootContext, List<ItemStack> itemStacks) {
        if (!lootContext.hasParameter(LootContext.BlockEntityTarget.BLOCK_ENTITY.contextParam())) {
            return;
        }

        var be = lootContext.getParameter(LootContext.BlockEntityTarget.BLOCK_ENTITY.contextParam());
        if (be.components().has(DecorationsDataComponents.TIED)) {
            for (var stack : itemStacks) {
                if (stack.has(DataComponents.CONTAINER)) {
                    stack.set(DecorationsDataComponents.TIED, Unit.INSTANCE);
                }
            }
        }
    }
}
