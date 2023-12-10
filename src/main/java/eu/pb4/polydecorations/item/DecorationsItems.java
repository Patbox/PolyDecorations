package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.factorytools.api.item.ModeledItem;
import eu.pb4.factorytools.api.item.MultiBlockItem;
import eu.pb4.factorytools.api.block.MultiBlock;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.block.furniture.ShelfBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polydecorations.ModInit;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.WoodType;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

public class DecorationsItems {
    public static final Item MOD_ICON = register("mod_icon", new ModeledItem(new Item.Settings()));
    public static final Map<WoodType, Item> SHELF = Util.make(() -> {
        var map = new HashMap<WoodType, Item>();

        DecorationsBlocks.SHELF.forEach((type, block) -> map.put(type, register(block)));

        return map;
    });
    public static void register() {

        PolymerItemGroupUtils.registerPolymerItemGroup(new Identifier(ModInit.ID, "a_group"), ItemGroup.create(ItemGroup.Row.BOTTOM, -1)
                //.icon(WINDMILL_SAIL::getDefaultStack)
                .displayName(Text.translatable("itemgroup." + ModInit.ID))
                .entries(((context, entries) -> {
                    SHELF.forEach((i, x) -> entries.add(x));
                })).build()
        );
    }


    public static <T extends Item> T register(String path, T item) {
        Registry.register(Registries.ITEM, new Identifier(ModInit.ID, path), item);
        return item;
    }

    public static <E extends Block & PolymerBlock> FactoryBlockItem register(E block) {
        var id = Registries.BLOCK.getId(block);
        FactoryBlockItem item;
        if (block instanceof MultiBlock multiBlock) {
            item = new MultiBlockItem(multiBlock, new Item.Settings());
        } else {
            item = new FactoryBlockItem(block, new Item.Settings());
        }

        Registry.register(Registries.ITEM, id, item);
        item.onRegistered(id);
        return item;
    }
}
