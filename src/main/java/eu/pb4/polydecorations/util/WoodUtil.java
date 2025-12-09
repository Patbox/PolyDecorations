package eu.pb4.polydecorations.util;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.level.block.state.properties.WoodType;

public class WoodUtil {
    public static List<WoodType> VANILLA = List.of(
            WoodType.OAK, WoodType.SPRUCE, WoodType.BIRCH, WoodType.ACACIA,
            WoodType.CHERRY, WoodType.JUNGLE, WoodType.DARK_OAK, WoodType.PALE_OAK,
            WoodType.CRIMSON, WoodType.WARPED, WoodType.MANGROVE,
            WoodType.BAMBOO);

    public static boolean isWood(WoodType type) {
        return !isHyphae(type) && !isBlock(type);
    }

    public static boolean isHyphae(WoodType type) {
        return type == WoodType.CRIMSON || type == WoodType.WARPED;
    }

    public static boolean isBlock(WoodType type) {
        return type == WoodType.BAMBOO;
    }


    public static String getLogName(WoodType type) {
        if (isBlock(type)) {
            return type.name() + "_block";
        }

        if (isHyphae(type)) {
            return type.name() + "_stem";
        }

        return type.name() + "_log";
    }

    public static <T> void forEach(Map<WoodType, T> map, Consumer<T> consumer) {
        WoodType.values().forEach(x -> {
            var y = map.get(x);
            if (y != null) {
                consumer.accept(y);
            }
        });
    }

    public static <T> void forEach(List<Map<WoodType, ?>> list, Consumer<T> consumer) {
        WoodType.values().forEach(x -> {
            for (var map : list) {
                var y = map.get(x);
                if (y != null) {
                    consumer.accept((T) y);
                }
            }
        });
    }

    public static <T> void forEachByItem(List<Map<WoodType, ?>> list, Consumer<T> consumer) {
        for (var map : list) {
            WoodType.values().forEach(x -> {
                var y = map.get(x);
                if (y != null) {
                    consumer.accept((T) y);
                }
            });
        }
    }
}
