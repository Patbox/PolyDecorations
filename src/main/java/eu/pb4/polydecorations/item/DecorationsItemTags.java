package eu.pb4.polydecorations.item;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class DecorationsItemTags {
    public static final TagKey<Item> GLOBE_REPLACEMENT = of("globe_replacement");
    public static final TagKey<Item> UNSCALED_DISPLAY_CASE = of("unscaled_display_case");
    private static TagKey<Item> of(String path) {
        return TagKey.of(RegistryKeys.ITEM, ModInit.id(path));
    }
}
