package eu.pb4.polydecorations.item;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class DecorationsItemTags {
    public static final TagKey<Item> GLOBE_REPLACEMENT = of("globe_replacement");
    public static final TagKey<Item> UNSCALED_DISPLAY_CASE = of("unscaled_display_case");
    public static final TagKey<Item> FORCE_FIXED_MODEL = of("force_fixed_model");
    public static final TagKey<Item> TOOL_RACK_ACCEPTABLE = of("tool_rack_acceptable");
    public static final TagKey<Item> STATUES = of("statues");
    public static final TagKey<Item> STUMPS = of("stumps");
    public static final TagKey<Item> SLEEPING_BAGS = of("sleeping_bags");
    public static final TagKey<Item> CANVAS_CLEAR_PIXELS = of("canvas/clear_pixels");
    public static final TagKey<Item> CANVAS_DARKEN_PIXELS = of("canvas/darken_pixels");
    public static final TagKey<Item> CANVAS_LIGHTEN_PIXELS = of("canvas/lighten_pixels");

    private static TagKey<Item> of(String path) {
        return TagKey.of(RegistryKeys.ITEM, ModInit.id(path));
    }
}
