package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import eu.pb4.polydecorations.item.WindChimeItem;
import eu.pb4.polydecorations.util.WoodUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static eu.pb4.polydecorations.util.DecorationsUtil.getValues;

public class LootTables extends FabricBlockLootTableProvider {
    protected LootTables(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
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
        this.dropSelf(DecorationsBlocks.BURNING_GHOST_LIGHT);
        this.dropSelf(DecorationsBlocks.COPPER_GHOST_LIGHT);
        this.dropSelf(DecorationsBlocks.DISPLAY_CASE);
        this.dropSelf(DecorationsBlocks.LARGE_FLOWER_POT);
        this.dropSelf(DecorationsBlocks.LONG_FLOWER_POT);
        this.dropSelf(DecorationsBlocks.TRASHCAN);
        this.dropSelf(DecorationsBlocks.ROPE);
        this.add(DecorationsBlocks.WIND_CHIME, (drop) -> LootTable.lootTable().withPool(
                this.applyExplosionCondition(drop, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(drop).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                .include(DecorationsDataComponents.WIND_CHIME_COLOR))))));
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        super.generate(biConsumer);
        new WoodLootTables(WoodUtil.VANILLA, this.registries).generate(biConsumer);
    }

    public static class WoodLootTables extends BlockLootSubProvider {
        private final List<WoodType> woodTypes;

        public WoodLootTables(List<WoodType> woodTypeList, HolderLookup.Provider provider) {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), provider);
            this.woodTypes = woodTypeList;
        }

        @Override
        public void generate() {
            getValues(DecorationsBlocks.SHELF, woodTypes, (t, b) -> this.add(b, createSlabItemTable(b)));
            getValues(DecorationsBlocks.BENCH, woodTypes, (t, b) -> this.dropSelf(b));
            getValues(DecorationsBlocks.TABLE, woodTypes, (t, b) -> this.dropSelf(b));
            getValues(DecorationsBlocks.TOOL_RACK, woodTypes, (t, b) -> this.dropSelf(b));
            getValues(DecorationsBlocks.WOODEN_MAILBOX, woodTypes, (t, b) -> this.dropSelf(b));
            getValues(DecorationsBlocks.STUMP, woodTypes, (t, b) -> this.dropSelf(b));
            getValues(DecorationsBlocks.STRIPPED_STUMP, woodTypes, (t, b) -> this.dropSelf(b));
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
            this.generate();
            for (Map.Entry<ResourceKey<LootTable>, LootTable.Builder> entry : map.entrySet()) {
                ResourceKey<LootTable> registryKey = entry.getKey();
                biConsumer.accept(registryKey, entry.getValue());
            }
        }
    }
}
