package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import eu.pb4.polydecorations.item.WindChimeItem;
import eu.pb4.polydecorations.mixin.BlockLootSubProviderAccessor;
import eu.pb4.polydecorations.mixin.FabricBlockLootSubProviderAccessor;
import eu.pb4.polydecorations.util.WoodUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.impl.datagen.loot.FabricLootTableContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
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
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static eu.pb4.polydecorations.util.DecorationsUtil.getValues;

public class LootTables extends FabricBlockLootSubProvider {
    protected LootTables(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
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
                            .apply(SetItemCountFunction.setCount(ContextIntProviders.exactly(2))))
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
                this.applyExplosionCondition(drop, LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(drop).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                .include(DecorationsDataComponents.WIND_CHIME_COLOR))))));
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        super.generate(biConsumer);
        var registries = ((FabricBlockLootSubProviderAccessor) LootTables.this).getRegistriesFuture().join();

        new WoodLootTables(WoodUtil.VANILLA, new Context() {
            @Override
            public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder value) {
                biConsumer.accept(key, value);
                return Holder.Reference.createStandAlone(registries.lookupOrThrow(Registries.LOOT_TABLE), key);
            }

            @Override
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                return registries.lookupOrThrow(key);
            }

            @Override
            public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
                return registries.lookupOrThrow(key).listElements();
            }
        }).run();
    }

    public static class WoodLootTables extends BlockLootSubProvider {
        private final List<WoodType> woodTypes;
        private final LootTableSubProvider.Context output;

        public WoodLootTables(List<WoodType> woodTypeList, LootTableSubProvider.Context output) {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), output);
            this.woodTypes = woodTypeList;
            this.output = output;
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
        public void run() {
            this.generate();
            for (Map.Entry<ResourceKey<LootTable>, LootTable.Builder> entry : ((BlockLootSubProviderAccessor) this).getMap().entrySet()) {
                ResourceKey<LootTable> registryKey = entry.getKey();
                output.accept(registryKey, entry.getValue());
            }
        }
    }
}
