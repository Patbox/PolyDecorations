package eu.pb4.polydecorations.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import eu.pb4.polydecorations.ModInit;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.WoodType;

public class WoodUtil {
    public static List<WoodType> VANILLA = List.of(
            WoodType.OAK, WoodType.SPRUCE, WoodType.BIRCH, WoodType.ACACIA,
            WoodType.CHERRY, WoodType.JUNGLE, WoodType.DARK_OAK, WoodType.PALE_OAK,
            WoodType.CRIMSON, WoodType.WARPED, WoodType.MANGROVE,
            WoodType.BAMBOO);

    public static List<WoodType> MODDED = new ArrayList<>();

    private static Set<WoodType> IS_HYPNAE = new ReferenceArraySet<>(List.of(WoodType.CRIMSON,  WoodType.WARPED));
    private static Set<WoodType> IS_BLOCK = new ReferenceArraySet<>(List.of(WoodType.BAMBOO));
    private static final List<Consumer<WoodType>> CONSUMERS = new ArrayList<>();

    public static boolean isWood(WoodType type) {
        return !isHyphae(type) && !isBlock(type);
    }

    public static boolean isHyphae(WoodType type) {
        return IS_HYPNAE.contains(type);
    }

    public static boolean isBlock(WoodType type) {
        return IS_BLOCK.contains(type);
    }

    public static boolean addModded(WoodType type) {
        var id = Identifier.tryParse(type.name());
        if (id == null) {
            ModInit.LOGGER.warn("Invalid wood type name '" + type.name() + "'! Skipping...");
            return false;
        }
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            if (!VANILLA.contains(type)) {
                ModInit.LOGGER.warn("Missing vanilla or misnamed modded wood type name '" + type.name() + "'! Skipping...");
            }
            return false;
        }

        if (!BuiltInRegistries.BLOCK.containsKey(id.withSuffix("_planks"))) {
            ModInit.LOGGER.warn("Missing planks of wood type '" + type.name() + "'! Skipping...");
            return false;
        }

        if (BuiltInRegistries.BLOCK.containsKey(id.withSuffix("_log"))) {

        } else if (BuiltInRegistries.BLOCK.containsKey(id.withSuffix("_stem"))) {
            IS_HYPNAE.add(type);
        } else if (BuiltInRegistries.BLOCK.containsKey(id.withSuffix("_block"))) {
            IS_BLOCK.add(type);
        } else {
            ModInit.LOGGER.warn("Missing log of wood type '" + type.name() + "'! Skipping...");
            return false;
        }

        MODDED.add(type);
        CONSUMERS.forEach(x -> x.accept(type));
        return true;
    }


    public static Identifier getLogName(WoodType type) {
        var id = Identifier.parse(type.name());

        if (isBlock(type)) {
            return id.withSuffix("_block");
        }

        if (isHyphae(type)) {
            return id.withSuffix("_stem");
        }

        return id.withSuffix("_log");
    }

    public static String getLogSuffix(WoodType type) {
        if (isBlock(type)) {
            return "block";
        }

        if (isHyphae(type)) {
            return "stem";
        }

        return "log";
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

    public static void registerVanillaAndWaitForModded(Consumer<WoodType> consumer) {
        for (var type : VANILLA) {
            consumer.accept(type);
        }
        CONSUMERS.add(consumer);
    }

    public static String asPath(WoodType type) {
        return type.name().replace(':', '/');
    }
}
