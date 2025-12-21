package eu.pb4.polydecorations.item;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.canvas.CanvasData;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;

import java.util.*;

public class DecorationsDataComponents {
    public static final DataComponentType<CanvasData> CANVAS_DATA = register("canvas_data",
            DataComponentType.<CanvasData>builder().persistent(CanvasData.CODEC).cacheEncoding());

    public static final DataComponentType<IntList> WIND_CHIME_COLOR = register("wind_chime_color",
            DataComponentType.<IntList>builder().persistent(ExtraCodecs.RGB_COLOR_CODEC.listOf().xmap(IntArrayList::new, List::copyOf)));

    public static final DataComponentType<Unit> TIED = register("tied", DataComponentType.<Unit>builder().persistent(Unit.CODEC));

    public static void register() {

    }

    public static <T> DataComponentType<T> register(String path, DataComponentType.Builder<T> function) {
        var id = Identifier.fromNamespaceAndPath(ModInit.ID, path);
        var type = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, function.build());
        PolymerComponent.registerDataComponent(type);
        return type;
    }
}
