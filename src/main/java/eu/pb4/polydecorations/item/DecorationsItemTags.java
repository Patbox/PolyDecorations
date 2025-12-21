package eu.pb4.polydecorations.item;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

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
    public static final TagKey<Item> TIEABLE_CONTAINERS = of("tieable_containers");

    private static TagKey<Item> of(String path) {
        return TagKey.create(Registries.ITEM, ModInit.id(path));
    }
}
