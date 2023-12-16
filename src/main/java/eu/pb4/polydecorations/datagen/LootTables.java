package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

class LootTables extends FabricBlockLootTableProvider {
    protected LootTables(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        DecorationsBlocks.SHELF.forEach((t, b) -> this.addDrop(b));
        DecorationsBlocks.BENCH.forEach((t, b) -> this.addDrop(b));
        this.addDrop(DecorationsBlocks.BRAZIER);
        this.addDrop(DecorationsBlocks.SOUL_BRAZIER);
        this.addDrop(DecorationsBlocks.GLOBE);
    }
}
