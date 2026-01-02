package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.factorytools.api.item.MultiBlockItem;
import eu.pb4.factorytools.api.block.MultiBlock;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.block.item.PickableItemContainerBlock;
import eu.pb4.polydecorations.entity.StatueEntity;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static eu.pb4.polydecorations.ModInit.id;

public class DecorationsItems {

    public static final Item TROWEL = register("trowel", (settings) -> new TrowelItem(settings
            .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(id("trowel_bonus"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
            .stacksTo(1)));

    public static final Item HAMMER = register("hammer", (settings) -> new HammerItem(settings.stacksTo(1)));
    public static final Item BRAZIER = register(DecorationsBlocks.BRAZIER);
    public static final Item SOUL_BRAZIER = register(DecorationsBlocks.SOUL_BRAZIER);
    public static final Item COPPER_BRAZIER = register(DecorationsBlocks.COPPER_BRAZIER);
    public static final Item COPPER_CAMPFIRE = register(DecorationsBlocks.COPPER_CAMPFIRE );
    public static final Item GLOBE = register(DecorationsBlocks.GLOBE);
    public static final Item WIND_CHIME = register("wind_chime", (s) -> new WindChimeItem(DecorationsBlocks.WIND_CHIME, s.useBlockDescriptionPrefix()));
    public static final Item TRASHCAN = register(DecorationsBlocks.TRASHCAN);
    public static final Item BASKET = register(DecorationsBlocks.BASKET, s -> s.stacksTo(1));
    public static final Item CARDBOARD_BOX = register(DecorationsBlocks.CARDBOARD_BOX, s -> s.stacksTo(1));
    public static final Map<WoodType, BlockItem> SHELF = registerWood(DecorationsBlocks.SHELF);
    public static final Map<WoodType, BlockItem> BENCH = registerWood(DecorationsBlocks.BENCH);
    public static final Map<WoodType, BlockItem> TABLE = registerWood(DecorationsBlocks.TABLE);
    public static final Map<WoodType, BlockItem> TOOL_RACK = registerWood(DecorationsBlocks.TOOL_RACK);
    public static final Map<WoodType, BlockItem> WOODEN_MAILBOX = registerWood(DecorationsBlocks.WOODEN_MAILBOX);
    public static final Map<WoodType, BlockItem> STUMP = registerWood(DecorationsBlocks.STUMP);
    public static final Map<WoodType, BlockItem> STRIPPED_STUMP = registerWood(DecorationsBlocks.STRIPPED_STUMP);
    public static final Map<WoodType, SignPostItem> SIGN_POST = registerWood("sign_post", (x) -> (settings) -> new SignPostItem(settings.useBlockDescriptionPrefix()));
    public static final Map<WoodType, StatueItem> WOODEN_STATUE = registerWood("statue", (x) -> {
        var planks = BuiltInRegistries.BLOCK.getValue(Identifier.parse(x.name() + "_planks"));
        return (settings) -> new StatueItem(StatueEntity.Type.of(WoodUtil.asPath(x), planks, false), settings.stacksTo(16));
    });
    public static final Map<DyeColor, Item> SLEEPING_BAG = register(DecorationsBlocks.SLEEPING_BAG, DyeColor::name, x -> x.stacksTo(1));

    public static final Item GHOST_LIGHT = register(DecorationsBlocks.GHOST_LIGHT);
    public static final Item BURNING_GHOST_LIGHT = register(DecorationsBlocks.BURNING_GHOST_LIGHT);
    public static final Item COPPER_GHOST_LIGHT = register(DecorationsBlocks.COPPER_GHOST_LIGHT);
    public static final Item DISPLAY_CASE = register(DecorationsBlocks.DISPLAY_CASE);
    public static final Item ROPE = register("rope", (settings) -> new RopeItem(DecorationsBlocks.ROPE, settings.useBlockDescriptionPrefix()));
    public static final Item LARGE_FLOWER_POT = register(DecorationsBlocks.LARGE_FLOWER_POT);
    public static final Item LONG_FLOWER_POT = register(DecorationsBlocks.LONG_FLOWER_POT);
    public static final Item CANVAS = register("canvas", (settings) -> new CanvasItem(settings.stacksTo(16)));
    public static final Map<StatueEntity.Type, StatueItem> OTHER_STATUE = registerList(StatueEntity.Type.NON_WOOD,
            (t) -> t.type() + "_statue",
            (t) -> (settings) -> new StatueItem(t, settings.stacksTo(16)));

    private static <T extends Block & PolymerBlock, B, U extends Comparable<? super U>> Map<B, Item> register(Map<B, T> blockMap, Function<B, U> toComparable) {
        return register(blockMap, toComparable, (s) -> {});
    }

    private static <T extends Block & PolymerBlock, B, U extends Comparable<? super U>> Map<B, Item> register(Map<B, T> blockMap, Function<B, U> toComparable, Consumer<Item.Properties> settingsConsumer) {
        var map = new LinkedHashMap<B, Item>();
        var keys = new ArrayList<>(blockMap.keySet());
        keys.sort(Comparator.comparing(toComparable));
        for (var key : keys) {
            map.put(key, register(blockMap.get(key), settingsConsumer));
        }
        return map;
    }

    private static <T, I extends Item> Map<T, I> registerList(List<T> list, Function<T, String> statue, Function<T, Function<Item.Properties, I>> item) {
        var map = new LinkedHashMap<T, I>();
        for (var key : list) {
            map.putLast(key, register(statue.apply(key), item.apply(key)));
        }
        return map;
    }

    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.fromNamespaceAndPath(ModInit.ID, "a_group"), PolymerItemGroupUtils.builder()
                .icon(() -> BENCH.get(WoodType.OAK).getDefaultInstance())
                .title(Component.translatable("itemgroup." + ModInit.ID))
                .displayItems(((context, entries) -> {
                    entries.accept(TROWEL);
                    entries.accept(HAMMER);
                    entries.accept(BRAZIER);
                    entries.accept(SOUL_BRAZIER);
                    entries.accept(COPPER_BRAZIER);
                    entries.accept(COPPER_CAMPFIRE);
                    entries.accept(GHOST_LIGHT);
                    entries.accept(BURNING_GHOST_LIGHT);
                    entries.accept(COPPER_GHOST_LIGHT);
                    entries.accept(LARGE_FLOWER_POT);
                    entries.accept(LONG_FLOWER_POT);
                    entries.accept(DISPLAY_CASE);
                    entries.accept(GLOBE);
                    entries.accept(WIND_CHIME);
                    entries.accept(TRASHCAN);
                    entries.accept(BASKET);
                    entries.accept(CARDBOARD_BOX);
                    entries.accept(ROPE);
                    entries.accept(CANVAS);
                    entries.accept(Items.LANTERN);
                    entries.accept(Items.SOUL_LANTERN);
                    WoodUtil.<Item>forEach(List.of(BENCH, STUMP, STRIPPED_STUMP, TABLE, SHELF, TOOL_RACK, SIGN_POST, WOODEN_MAILBOX, WOODEN_STATUE), entries::accept);
                    DecorationsUtil.COLORS_CREATIVE.forEach(a -> entries.accept(SLEEPING_BAG.get(a)));
                    OTHER_STATUE.forEach((a, b) -> entries.accept(b));
                })).build()
        );
    }

    private static <E extends Block & PolymerBlock> Map<WoodType, BlockItem> registerWood(Map<WoodType, E> blockMap) {
        var map = new HashMap<WoodType, BlockItem>();

        WoodUtil.registerVanillaAndWaitForModded(x -> {
            var y = blockMap.get(x);
            if (y != null) {
                map.put(x, register(y));
            }
        });

        return map;
    }

    private static <T extends Item> Map<WoodType, T> registerWood(String id, Function<WoodType, Function<Item.Properties, T>> object) {
        var map = new HashMap<WoodType, T>();

        WoodUtil.registerVanillaAndWaitForModded(x -> {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name().replace(':', '/') + "_" + id, y));
            }
        });

        return map;
    }

    private static <T extends Item> Map<DyeColor, T> registerDye(String id, Function<DyeColor, Function<Item.Properties, T>> object) {
        var map = new LinkedHashMap<DyeColor, T>();

        for (var x : DyeColor.values()) {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name() + "_" + id, y));
            }
        };

        return map;
    }

    public static <T extends Item> T register(String path, Function<Item.Properties, T> function) {
        var id = Identifier.fromNamespaceAndPath(ModInit.ID, path);
        var item = function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }

    public static <E extends Block & PolymerBlock> BlockItem register(E block) {
        return register(block, (s) -> {});
    }
    public static <E extends Block & PolymerBlock> BlockItem register(E block, Consumer<Item.Properties> settingsConsumer) {
        var id = BuiltInRegistries.BLOCK.getKey(block);
        BlockItem item;
        var settings = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix();
        settingsConsumer.accept(settings);
        if (block instanceof MultiBlock multiBlock) {
            item = new MultiBlockItem(multiBlock, settings);
        } else if (block instanceof PickableItemContainerBlock) {
            item = new FactoryBlockItem(block, settings) {
                @Override
                public boolean canFitInsideContainerItems() {
                    return false;
                }
            };
        } else {
            item = new FactoryBlockItem(block, settings);
        }

        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }
}
