package eu.pb4.polydecorations.datagen;

import com.google.common.collect.Maps;
import eu.pb4.polydecorations.block.DecorationsBlockTags;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.util.WoodUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
    public BlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .addAll(Maps.filterKeys(DecorationsBlocks.SHELF, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .addAll(Maps.filterKeys(DecorationsBlocks.WOOD_SIGN_POST, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .addAll(Maps.filterKeys(DecorationsBlocks.WOODEN_MAILBOX, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .addAll(Maps.filterKeys(DecorationsBlocks.BENCH, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .addAll(Maps.filterKeys(DecorationsBlocks.TABLE, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .addAll(Maps.filterKeys(DecorationsBlocks.TOOL_RACK, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .addAll(Maps.filterKeys(DecorationsBlocks.STUMP, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .addAll(Maps.filterKeys(DecorationsBlocks.STRIPPED_STUMP, WoodUtil.VANILLA::contains).values().stream().map(this::get))
                .add(get(DecorationsBlocks.COPPER_CAMPFIRE))
                .add(get(DecorationsBlocks.BASKET))
                .add(get(DecorationsBlocks.CARDBOARD_BOX))
        ;

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addAll(DecorationsBlocks.WALL_SIGN_POST.values().stream().map(this::get))
                .add(get(DecorationsBlocks.WALL_LANTERN))
                .add(get(DecorationsBlocks.WALL_SOUL_LANTERN))
                .addAll(DecorationsBlocks.WALL_COPPER_LANTERNS.asList().stream().map(this::get))
                .add(get(DecorationsBlocks.BRAZIER))
                .add(get(DecorationsBlocks.SOUL_BRAZIER))
                .add(get(DecorationsBlocks.COPPER_BRAZIER))
                .add(get(DecorationsBlocks.LARGE_FLOWER_POT))
                .add(get(DecorationsBlocks.DISPLAY_CASE))
                .add(get(DecorationsBlocks.TRASHCAN))
        ;

        this.tag(BlockTags.CAMPFIRES)
                .add(get(DecorationsBlocks.COPPER_CAMPFIRE));

        this.tag(BlockTags.CLIMBABLE).add(get(DecorationsBlocks.ROPE));

        this.tag(DecorationsBlockTags.BRAZIERS)
                .addAll(List.of(DecorationsBlocks.BRAZIER, DecorationsBlocks.SOUL_BRAZIER, DecorationsBlocks.COPPER_BRAZIER).stream().map(this::get));

        this.tag(DecorationsBlockTags.SHELVES)
                .addAll(DecorationsBlocks.SHELF.values().stream().map(this::get));

        this.tag(DecorationsBlockTags.TOOL_RACKS)
                .addAll(DecorationsBlocks.TOOL_RACK.values().stream().map(this::get));

        this.tag(DecorationsBlockTags.TABLES)
                .addAll(DecorationsBlocks.TABLE.values().stream().map(this::get));

        this.tag(DecorationsBlockTags.MAILBOXES)
                .addAll(DecorationsBlocks.WOODEN_MAILBOX.values().stream().map(this::get));

        this.tag(DecorationsBlockTags.SIGN_POSTS)
                .addAll(DecorationsBlocks.WOOD_SIGN_POST.values().stream().map(this::get))
                .addAll(DecorationsBlocks.WALL_SIGN_POST.values().stream().map(this::get));

        this.tag(DecorationsBlockTags.BENCHES)
                .addAll(DecorationsBlocks.BENCH.values().stream().map(this::get));

        this.tag(DecorationsBlockTags.UNCONNECTABLE)
                .addOptionalTag(DecorationsBlockTags.BENCHES)
                .addOptionalTag(DecorationsBlockTags.TABLES)
                .addOptionalTag(DecorationsBlockTags.SHELVES)
                .addOptionalTag(DecorationsBlockTags.BRAZIERS)
                .addOptionalTag(DecorationsBlockTags.MAILBOXES)
                .addOptionalTag(DecorationsBlockTags.STUMPS)
                .addOptionalTag(DecorationsBlockTags.SLEEPING_BAGS)
                .add(get(DecorationsBlocks.DISPLAY_CASE))
                .add(get(DecorationsBlocks.LARGE_FLOWER_POT))
                .add(get(DecorationsBlocks.BASKET))
                .add(get(DecorationsBlocks.LONG_FLOWER_POT))
                .add(get(DecorationsBlocks.WIND_CHIME))
                .add(get(DecorationsBlocks.CARDBOARD_BOX))
        ;

        this.tag(DecorationsBlockTags.ALLOWED_INTERACTIONS_BLOCKS)
                .addOptionalTag(DecorationsBlockTags.MAILBOXES);

        for (var x : List.of(
                BlockTags.SUPPORTS_VEGETATION,
                BlockTags.SUPPORTS_CROPS,
                BlockTags.SUPPORTS_SMALL_DRIPLEAF,
                BlockTags.SUPPORTS_BIG_DRIPLEAF
        )) {
            this.tag(x)
                    .add(get(DecorationsBlocks.LARGE_FLOWER_POT));
        }

        this.tag(DecorationsBlockTags.STUMPS)
                .addAll(DecorationsBlocks.STUMP.values().stream().map(this::get))
                .addAll(DecorationsBlocks.STRIPPED_STUMP.values().stream().map(this::get))
        ;

        this.tag(DecorationsBlockTags.SLEEPING_BAGS)
                .addAll(DecorationsBlocks.SLEEPING_BAG.values().stream().map(this::get))
        ;

        this.tag(DecorationsBlockTags.USE_BASE_SHAPE_OVER_SUPPORT_SHAPE)
                .addOptionalTag(DecorationsBlockTags.STUMPS);
    }


    // Temp workaround
    private ResourceKey<Block> get(Block item) {
        return item.builtInRegistryHolder().key();
    }
}
