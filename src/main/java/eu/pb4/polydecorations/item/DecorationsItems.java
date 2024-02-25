package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.factorytools.api.item.ModeledItem;
import eu.pb4.factorytools.api.item.MultiBlockItem;
import eu.pb4.factorytools.api.block.MultiBlock;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.entity.StatueEntity;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polydecorations.ModInit;
import net.minecraft.block.Block;
import net.minecraft.block.WoodType;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DecorationsItems {
    public static final Item MOD_ICON = register("mod_icon", new ModeledItem(new Item.Settings()));

    public static final Item BRAZIER = register(DecorationsBlocks.BRAZIER);
    public static final Item SOUL_BRAZIER = register(DecorationsBlocks.SOUL_BRAZIER);
    public static final Item GLOBE = register(DecorationsBlocks.GLOBE);
    public static final Map<WoodType, Item> SHELF = register(DecorationsBlocks.SHELF);
    public static final Map<WoodType, Item> BENCH = register(DecorationsBlocks.BENCH);
    public static final Map<WoodType, SignPostItem> SIGN_POST = registerWood("sign_post", (x) -> new SignPostItem(new Item.Settings()));
    public static final Map<WoodType, StatueItem> WOODEN_STATUE = registerWood("statue", (x) -> {
        var planks = Registries.BLOCK.get(new Identifier(x.name() + "_planks"));
        return new StatueItem(StatueEntity.Type.of(x.name(), planks, false), new Item.Settings().maxCount(16));
    });
    //public static final Map<DyeColor, Item> BANNER_BED = register(DecorationsBlocks.BANNER_BED);

    public static final Item DISPLAY_CASE = register(DecorationsBlocks.DISPLAY_CASE);
    public static final Item LARGE_FLOWER_POT = register(DecorationsBlocks.LARGE_FLOWER_POT);
    public static final Item CANVAS = register("canvas", new CanvasItem(new Item.Settings().maxCount(16)));
    public static final StatueItem STONE_STATUE = register("stone_statue", new StatueItem(StatueEntity.Type.STONE, new Item.Settings().maxCount(16)));
    public static final StatueItem DEEPSLATE_STATUE = register("deepslate_statue", new StatueItem(StatueEntity.Type.DEEPSLATE, new Item.Settings().maxCount(16)));
    public static final StatueItem BLACKSTONE_STATUE = register("blackstone_statue", new StatueItem(StatueEntity.Type.BLACKSTONE, new Item.Settings().maxCount(16)));
    public static final StatueItem PRISMARINE_STATUE = register("prismarine_statue", new StatueItem(StatueEntity.Type.PRISMARINE, new Item.Settings().maxCount(16)));
    public static final StatueItem SANDSTONE_STATUE = register("sandstone_statue", new StatueItem(StatueEntity.Type.SANDSTONE, new Item.Settings().maxCount(16)));
    public static final StatueItem RED_SANDSTONE_STATUE = register("red_sandstone_statue", new StatueItem(StatueEntity.Type.RED_SANDSTONE, new Item.Settings().maxCount(16)));
    public static final StatueItem QUARTZ_STATUE = register("quartz_statue", new StatueItem(StatueEntity.Type.QUARTZ, new Item.Settings().maxCount(16)));

    private static <T extends Block & PolymerBlock, B> Map<B, Item> register(Map<B, T> blockMap) {
        var map = new HashMap<B, Item>();
        blockMap.forEach((a, b) -> map.put(a, register(b)));
        return map;
    }

    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(new Identifier(ModInit.ID, "a_group"), ItemGroup.create(ItemGroup.Row.BOTTOM, -1)
                .icon(() -> BENCH.get(WoodType.OAK).getDefaultStack())
                .displayName(Text.translatable("itemgroup." + ModInit.ID))
                .entries(((context, entries) -> {
                    entries.add(BRAZIER);
                    entries.add(SOUL_BRAZIER);
                    entries.add(LARGE_FLOWER_POT);
                    entries.add(DISPLAY_CASE);
                    entries.add(GLOBE);
                    entries.add(CANVAS);
                    WoodUtil.<Item>forEach(List.of(BENCH, SHELF, SIGN_POST, WOODEN_STATUE), entries::add);
                    entries.add(STONE_STATUE);
                    entries.add(DEEPSLATE_STATUE);
                    entries.add(BLACKSTONE_STATUE);
                    entries.add(SANDSTONE_STATUE);
                    entries.add(RED_SANDSTONE_STATUE);
                    entries.add(QUARTZ_STATUE);
                    entries.add(PRISMARINE_STATUE);
                })).build()
        );
    }

    private static <T extends Item> Map<WoodType, T> registerWood(String id, Function<WoodType, T> object) {
        var map = new HashMap<WoodType, T>();

        WoodUtil.VANILLA.forEach(x -> {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name() + "_" + id, y));
            }
        });

        return map;
    }

    private static <T extends Item> Map<DyeColor, T> registerDye(String id, Function<DyeColor, T> object) {
        var map = new HashMap<DyeColor, T>();

        for (var x : DyeColor.values()) {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name() + "_" + id, y));
            }
        };

        return map;
    }

    public static <T extends Item> T register(String path, T item) {
        Registry.register(Registries.ITEM, new Identifier(ModInit.ID, path), item);
        return item;
    }

    public static <E extends Block & PolymerBlock> BlockItem register(E block) {
        var id = Registries.BLOCK.getId(block);
        BlockItem item;
        if (block instanceof MultiBlock multiBlock) {
            item = new MultiBlockItem(multiBlock, new Item.Settings());
        } else {
            item = new FactoryBlockItem(block, new Item.Settings());
        }

        Registry.register(Registries.ITEM, id, item);
        return item;
    }
}
