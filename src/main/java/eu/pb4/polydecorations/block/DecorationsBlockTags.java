package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class DecorationsBlockTags {
    public static final TagKey<Block> UNCONNECTABLE = of("unconnectable");
    public static final TagKey<Block> MAILBOXES = of("mailboxes");
    public static final TagKey<Block> SHELVES = of("shelves");
    public static final TagKey<Block> BENCHES = of("benches");
    public static final TagKey<Block> BRAZIERS = of("braziers");
    public static final TagKey<Block> SIGN_POSTS = of("sign_posts");

    public static final TagKey<Block> ALLOWED_INTERACTIONS_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("goml", "allowed_interactions"));

    private static TagKey<Block> of(String path) {
        return TagKey.of(RegistryKeys.BLOCK, ModInit.id(path));
    }
}
