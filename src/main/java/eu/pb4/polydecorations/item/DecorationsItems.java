package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.factorytools.api.item.MultiBlockItem;
import eu.pb4.factorytools.api.block.MultiBlock;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.entity.StatueEntity;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.block.Block;
import net.minecraft.block.WoodType;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static eu.pb4.polydecorations.ModInit.id;

public class DecorationsItems {

    public static final Item TROWEL = register("trowel", (settings) -> new TrowelItem(settings
            .attributeModifiers(AttributeModifiersComponent.builder()
                    .add(EntityAttributes.BLOCK_INTERACTION_RANGE, new EntityAttributeModifier(id("trowel_bonus"), 1, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND).build())
            .maxCount(1)));

    public static final Item HAMMER = register("hammer", (settings) -> new HammerItem(settings.maxCount(1)));
    public static final Item BRAZIER = register(DecorationsBlocks.BRAZIER);
    public static final Item SOUL_BRAZIER = register(DecorationsBlocks.SOUL_BRAZIER);
    public static final Item GLOBE = register(DecorationsBlocks.GLOBE);
    public static final Item TRASHCAN = register(DecorationsBlocks.TRASHCAN);
    public static final Map<WoodType, Item> SHELF = register(DecorationsBlocks.SHELF, WoodType::name);
    public static final Map<WoodType, Item> BENCH = register(DecorationsBlocks.BENCH, WoodType::name);
    public static final Map<WoodType, Item> TABLE = register(DecorationsBlocks.TABLE, WoodType::name);
    public static final Map<WoodType, Item> TOOL_RACK = register(DecorationsBlocks.TOOL_RACK, WoodType::name);
    public static final Map<WoodType, Item> WOODEN_MAILBOX = register(DecorationsBlocks.WOODEN_MAILBOX, WoodType::name);
    public static final Map<WoodType, SignPostItem> SIGN_POST = registerWood("sign_post", (x) -> (settings) -> new SignPostItem(settings.useBlockPrefixedTranslationKey()));
    public static final Map<WoodType, StatueItem> WOODEN_STATUE = registerWood("statue", (x) -> {
        var planks = Registries.BLOCK.get(Identifier.of(x.name() + "_planks"));
        return (settings) -> new StatueItem(StatueEntity.Type.of(x.name(), planks, false), settings.maxCount(16));
    });
    //public static final Map<DyeColor, Item> BANNER_BED = register(DecorationsBlocks.BANNER_BED);
    public static final Item GHOST_LIGHT = register(DecorationsBlocks.GHOST_LIGHT);
    public static final Item DISPLAY_CASE = register(DecorationsBlocks.DISPLAY_CASE);
    public static final Item ROPE = register("rope", (settings) -> new RopeItem(DecorationsBlocks.ROPE, settings.useBlockPrefixedTranslationKey()));
    public static final Item LARGE_FLOWER_POT = register(DecorationsBlocks.LARGE_FLOWER_POT);
    public static final Item CANVAS = register("canvas", (settings) -> new CanvasItem(settings.maxCount(16)));
    public static final Map<StatueEntity.Type, StatueItem> OTHER_STATUE = registerList(StatueEntity.Type.NON_WOOD,
            (t) -> t.type() + "_statue",
            (t) -> (settings) -> new StatueItem(t, settings.maxCount(16)));

    private static <T extends Block & PolymerBlock, B, U extends Comparable<? super U>> Map<B, Item> register(Map<B, T> blockMap, Function<B, U> toComparable) {
        var map = new LinkedHashMap<B, Item>();
        var keys = new ArrayList<>(blockMap.keySet());
        keys.sort(Comparator.comparing(toComparable));
        for (var key : keys) {
            map.put(key, register(blockMap.get(key)));
        }
        return map;
    }

    private static <T, I extends Item> Map<T, I> registerList(List<T> list, Function<T, String> statue, Function<T, Function<Item.Settings, I>> item) {
        var map = new LinkedHashMap<T, I>();
        for (var key : list) {
            map.putLast(key, register(statue.apply(key), item.apply(key)));
        }
        return map;
    }

    public static void register() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(ModInit.ID, "canvas_data"), CanvasItem.DATA_TYPE);
        PolymerComponent.registerDataComponent(CanvasItem.DATA_TYPE);
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(ModInit.ID, "a_group"), PolymerItemGroupUtils.builder()
                .icon(() -> BENCH.get(WoodType.OAK).getDefaultStack())
                .displayName(Text.translatable("itemgroup." + ModInit.ID))
                .entries(((context, entries) -> {
                    entries.add(TROWEL);
                    entries.add(HAMMER);
                    entries.add(BRAZIER);
                    entries.add(SOUL_BRAZIER);
                    entries.add(GHOST_LIGHT);
                    entries.add(LARGE_FLOWER_POT);
                    entries.add(DISPLAY_CASE);
                    entries.add(GLOBE);
                    entries.add(TRASHCAN);
                    entries.add(ROPE);
                    entries.add(CANVAS);
                    entries.add(Items.LANTERN);
                    entries.add(Items.SOUL_LANTERN);
                    WoodUtil.<Item>forEach(List.of(BENCH, TABLE, SHELF, TOOL_RACK, SIGN_POST, WOODEN_MAILBOX, WOODEN_STATUE), entries::add);
                    OTHER_STATUE.forEach((a, b) -> entries.add(b));
                })).build()
        );
    }

    private static <T extends Item> Map<WoodType, T> registerWood(String id, Function<WoodType, Function<Item.Settings, T>> object) {
        var map = new HashMap<WoodType, T>();

        WoodUtil.VANILLA.forEach(x -> {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name() + "_" + id, y));
            }
        });

        return map;
    }

    private static <T extends Item> Map<DyeColor, T> registerDye(String id, Function<DyeColor, Function<Item.Settings, T>> object) {
        var map = new LinkedHashMap<DyeColor, T>();

        for (var x : DyeColor.values()) {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name() + "_" + id, y));
            }
        };

        return map;
    }

    public static <T extends Item> T register(String path, Function<Item.Settings, T> function) {
        var id = Identifier.of(ModInit.ID, path);
        var item = function.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id)));
        Registry.register(Registries.ITEM, id, item);
        return item;
    }

    public static <E extends Block & PolymerBlock> BlockItem register(E block) {
        var id = Registries.BLOCK.getId(block);
        BlockItem item;
        var settings = new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id)).useBlockPrefixedTranslationKey();
        if (block instanceof MultiBlock multiBlock) {
            item = new MultiBlockItem(multiBlock, settings);
        } else {
            item = new FactoryBlockItem(block, settings);
        }

        Registry.register(Registries.ITEM, id, item);
        return item;
    }
}
