package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.factorytools.api.item.ModeledItem;
import eu.pb4.factorytools.api.item.MultiBlockItem;
import eu.pb4.factorytools.api.block.MultiBlock;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.block.furniture.ShelfBlock;
import eu.pb4.polydecorations.block.plus.BedWithBannerBlock;
import eu.pb4.polydecorations.block.plus.SignPostBlock;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polydecorations.ModInit;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.WoodType;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

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
    //public static final Map<DyeColor, Item> BANNER_BED = register(DecorationsBlocks.BANNER_BED);
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
                    WoodUtil.<Item>forEach(List.of(BENCH, SHELF, SIGN_POST), entries::add);
                    entries.add(BRAZIER);
                    entries.add(SOUL_BRAZIER);
                    entries.add(GLOBE);
                })).build()
        );
    }

    private static <T extends Item> Map<WoodType, T> registerWood(String id, Function<WoodType, T> object) {
        var map = new HashMap<WoodType, T>();

        WoodType.stream().forEach(x -> {
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
        if (block instanceof BedWithBannerBlock bed) {
            item = new PolymerBlockItem(bed, new Item.Settings().maxCount(1), bed.getBacking().asItem());
        } else if (block instanceof MultiBlock multiBlock) {
            item = new MultiBlockItem(multiBlock, new Item.Settings());
        } else {
            item = new FactoryBlockItem(block, new Item.Settings());
        }

        Registry.register(Registries.ITEM, id, item);
        return item;
    }
}
