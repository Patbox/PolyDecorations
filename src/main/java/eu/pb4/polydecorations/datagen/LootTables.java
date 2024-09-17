package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
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
        this.addDrop(DecorationsBlocks.BRAZIER);
        this.addDrop(DecorationsBlocks.SOUL_BRAZIER);
        this.addDrop(DecorationsBlocks.GLOBE);
        this.addDrop(DecorationsBlocks.GHOST_LIGHT);
        this.addDrop(DecorationsBlocks.DISPLAY_CASE);
        this.addDrop(DecorationsBlocks.LARGE_FLOWER_POT);
        this.addDrop(DecorationsBlocks.TRASHCAN);
        this.addDrop(DecorationsBlocks.ROPE);
    }
}
