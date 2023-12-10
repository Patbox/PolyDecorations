package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class DecorationsBlockTags {
    //public static final TagKey<Block> CONVEYORS = of("conveyors");
    private static TagKey<Block> of(String path) {
        return TagKey.of(RegistryKeys.BLOCK, ModInit.id(path));
    }
}
