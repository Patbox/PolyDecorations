package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.WindChimeItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import java.util.concurrent.CompletableFuture;

class LootTables extends FabricBlockLootTableProvider {
    protected LootTables(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        DecorationsBlocks.SHELF.forEach((t, b) -> this.add(b, createSlabItemTable(b)));
        DecorationsBlocks.BENCH.forEach((t, b) -> this.dropSelf(b));
        DecorationsBlocks.TABLE.forEach((t, b) -> this.dropSelf(b));
        DecorationsBlocks.TOOL_RACK.forEach((t, b) -> this.dropSelf(b));
        DecorationsBlocks.WOODEN_MAILBOX.forEach((t, b) -> this.dropSelf(b));
        DecorationsBlocks.STUMP.forEach((t, b) -> this.dropSelf(b));
        DecorationsBlocks.STRIPPED_STUMP.forEach((t, b) -> this.dropSelf(b));
        DecorationsBlocks.SLEEPING_BAG.forEach((t, b) -> this.add(b, this.createSinglePropConditionTable(b, BedBlock.PART, BedPart.HEAD)));
        this.add(DecorationsBlocks.BASKET, this.createShulkerBoxDrop(DecorationsBlocks.BASKET));
        this.add(DecorationsBlocks.CARDBOARD_BOX, this.createShulkerBoxDrop(DecorationsBlocks.CARDBOARD_BOX));
        this.dropSelf(DecorationsBlocks.BRAZIER);
        this.dropSelf(DecorationsBlocks.SOUL_BRAZIER);
        this.dropSelf(DecorationsBlocks.COPPER_BRAZIER);
        this.add(DecorationsBlocks.COPPER_CAMPFIRE, (block) -> {
            return this.createSilkTouchDispatchTable(block,
                    this.applyExplosionCondition(block, LootItem.lootTableItem(Items.CHARCOAL)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
            );
        });
        this.dropSelf(DecorationsBlocks.GLOBE);
        this.dropSelf(DecorationsBlocks.GHOST_LIGHT);
        this.dropSelf(DecorationsBlocks.DISPLAY_CASE);
        this.dropSelf(DecorationsBlocks.LARGE_FLOWER_POT);
        this.dropSelf(DecorationsBlocks.LONG_FLOWER_POT);
        this.dropSelf(DecorationsBlocks.TRASHCAN);
        this.dropSelf(DecorationsBlocks.ROPE);
        this.add(DecorationsBlocks.WIND_CHIME, (drop) -> LootTable.lootTable().withPool(
                this.applyExplosionCondition(drop, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(drop).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                .include(WindChimeItem.WIND_CHIME_COLOR))))));
    }
}
