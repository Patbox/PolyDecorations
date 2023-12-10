package eu.pb4.polydecorations.util;

import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;

public class WoodUtil {
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
}
