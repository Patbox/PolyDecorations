package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.WindChimeItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

class LootTables extends FabricBlockLootTableProvider {
    protected LootTables(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        DecorationsBlocks.SHELF.forEach((t, b) -> this.addDrop(b, slabDrops(b)));
        DecorationsBlocks.BENCH.forEach((t, b) -> this.addDrop(b));
        DecorationsBlocks.TABLE.forEach((t, b) -> this.addDrop(b));
        DecorationsBlocks.TOOL_RACK.forEach((t, b) -> this.addDrop(b));
        DecorationsBlocks.WOODEN_MAILBOX.forEach((t, b) -> this.addDrop(b));
        DecorationsBlocks.STUMP.forEach((t, b) -> this.addDrop(b));
        DecorationsBlocks.STRIPPED_STUMP.forEach((t, b) -> this.addDrop(b));
        DecorationsBlocks.SLEEPING_BAG.forEach((t, b) -> this.addDrop(b, this.dropsWithProperty(b, BedBlock.PART, BedPart.HEAD)));
        this.addDrop(DecorationsBlocks.BASKET, this.shulkerBoxDrops(DecorationsBlocks.BASKET));
        this.addDrop(DecorationsBlocks.BRAZIER);
        this.addDrop(DecorationsBlocks.SOUL_BRAZIER);
        this.addDrop(DecorationsBlocks.GLOBE);
        this.addDrop(DecorationsBlocks.GHOST_LIGHT);
        this.addDrop(DecorationsBlocks.DISPLAY_CASE);
        this.addDrop(DecorationsBlocks.LARGE_FLOWER_POT);
        this.addDrop(DecorationsBlocks.LONG_FLOWER_POT);
        this.addDrop(DecorationsBlocks.TRASHCAN);
        this.addDrop(DecorationsBlocks.ROPE);
        this.addDrop(DecorationsBlocks.WIND_CHIME, (drop) -> LootTable.builder().pool(
                this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F))
                        .with(ItemEntry.builder(drop).apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY)
                                .include(WindChimeItem.WIND_CHIME_COLOR))))));
    }
}
