package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.factorytools.api.item.MultiBlockItem;
import eu.pb4.factorytools.api.block.MultiBlock;
import eu.pb4.polydecorations.DecorationsFeature;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.block.item.PickableItemContainerBlock;
import eu.pb4.polydecorations.entity.StatueEntity;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
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

    public static final Item TROWEL = DecorationsFeature.TROWEL.isRegistered() ? register("trowel", (settings) -> new TrowelItem(settings
            .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(id("trowel_bonus"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build())
            .stacksTo(1))) : null;

    public static final Item HAMMER = DecorationsFeature.HAMMER.isRegistered() ? register("hammer", (settings) -> new HammerItem(settings.stacksTo(1))) : null;
    public static final Item BRAZIER = register(DecorationsBlocks.BRAZIER);
    public static final Item SOUL_BRAZIER = register(DecorationsBlocks.SOUL_BRAZIER);
    public static final Item COPPER_BRAZIER = register(DecorationsBlocks.COPPER_BRAZIER);
    public static final Item COPPER_CAMPFIRE = register(DecorationsBlocks.COPPER_CAMPFIRE );
    public static final Item GLOBE = register(DecorationsBlocks.GLOBE);
    public static final Item WIND_CHIME = DecorationsFeature.WIND_CHIME.isRegistered() ? register("wind_chime", (s) -> new WindChimeItem(DecorationsBlocks.WIND_CHIME, s.useBlockDescriptionPrefix())) : null;
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
    public static final Map<WoodType, SignPostItem> SIGN_POST = DecorationsFeature.SIGN_POST.isRegistered() ? registerWood("sign_post", (x) -> (settings) -> new SignPostItem(settings.useBlockDescriptionPrefix())) : Map.of();
    public static final Map<WoodType, StatueItem> WOODEN_STATUE = DecorationsFeature.STATUES.isRegistered() ? registerWood("statue", (x) -> {
        var planks = BuiltInRegistries.BLOCK.getValue(Identifier.parse(x.name() + "_planks"));
        return (settings) -> new StatueItem(StatueEntity.Type.of(WoodUtil.asPath(x), planks, false), settings.stacksTo(16));
    }) : Map.of();
    public static final Map<DyeColor, Item> SLEEPING_BAG = register(DecorationsBlocks.SLEEPING_BAG, DyeColor::name, x -> x.stacksTo(1));

    public static final Item GHOST_LIGHT = register(DecorationsBlocks.GHOST_LIGHT);
    public static final Item BURNING_GHOST_LIGHT = register(DecorationsBlocks.BURNING_GHOST_LIGHT);
    public static final Item COPPER_GHOST_LIGHT = register(DecorationsBlocks.COPPER_GHOST_LIGHT);
    public static final Item DISPLAY_CASE = register(DecorationsBlocks.DISPLAY_CASE);
    public static final Item ROPE = DecorationsFeature.ROPE.isRegistered() ? register("rope", (settings) -> new RopeItem(DecorationsBlocks.ROPE, settings.useBlockDescriptionPrefix())) : null;
    public static final Item LARGE_FLOWER_POT = register(DecorationsBlocks.LARGE_FLOWER_POT);
    public static final Item LONG_FLOWER_POT = register(DecorationsBlocks.LONG_FLOWER_POT);
    public static final Item CANVAS = DecorationsFeature.CANVAS.isRegistered() ? register("canvas", (settings) -> new CanvasItem(settings.stacksTo(16))) : null;
    public static final Map<StatueEntity.Type, StatueItem> OTHER_STATUE = DecorationsFeature.STATUES.isRegistered() ? registerList(StatueEntity.Type.NON_WOOD,
            (t) -> t.type() + "_statue",
            (t) -> (settings) -> new StatueItem(t, settings.stacksTo(16))) : Map.of();

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
        var icon = BENCH.containsKey(WoodType.OAK) ? BENCH.get(WoodType.OAK) : BuiltInRegistries.ITEM.stream()
                .filter(x -> BuiltInRegistries.ITEM.getKey(x).getNamespace().equals(ModInit.ID)).findFirst().orElse(null);
        if (icon == null) {
            return;
        }

        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.fromNamespaceAndPath(ModInit.ID, "a_group"), PolymerCreativeModeTabUtils.builder()
                .icon(icon::getDefaultInstance)
                .title(Component.translatable("itemgroup." + ModInit.ID))
                .displayItems(((context, entries) -> {
                    Consumer<Item> accept = (item) -> {
                        if (item != null) {
                            entries.accept(item);
                        }
                    };
                    accept.accept(TROWEL);
                    accept.accept(HAMMER);
                    accept.accept(BRAZIER);
                    accept.accept(SOUL_BRAZIER);
                    accept.accept(COPPER_BRAZIER);
                    accept.accept(COPPER_CAMPFIRE);
                    accept.accept(GHOST_LIGHT);
                    accept.accept(BURNING_GHOST_LIGHT);
                    accept.accept(COPPER_GHOST_LIGHT);
                    accept.accept(LARGE_FLOWER_POT);
                    accept.accept(LONG_FLOWER_POT);
                    accept.accept(DISPLAY_CASE);
                    accept.accept(GLOBE);
                    accept.accept(WIND_CHIME);
                    accept.accept(TRASHCAN);
                    accept.accept(BASKET);
                    accept.accept(CARDBOARD_BOX);
                    accept.accept(ROPE);
                    accept.accept(CANVAS);
                    if (DecorationsFeature.WALL_LANTERNS.isRegistered()) {
                        entries.accept(Items.LANTERN);
                        entries.accept(Items.SOUL_LANTERN);
                    }
                    WoodUtil.<Item>forEach(List.of(BENCH, STUMP, STRIPPED_STUMP, TABLE, SHELF, TOOL_RACK, SIGN_POST, WOODEN_MAILBOX, WOODEN_STATUE), entries::accept);
                    DecorationsUtil.COLORS_CREATIVE.forEach(a -> accept.accept(SLEEPING_BAG.get(a)));
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
        if (block == null) {
            // Block's feature is disabled
            return null;
        }
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
