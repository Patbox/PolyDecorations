package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlockTags;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

class BlockTagsProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.valueLookupBuilder(BlockTags.AXE_MINEABLE)
                .add(DecorationsBlocks.SHELF.values().toArray(new Block[0]))
                .add(DecorationsBlocks.WOOD_SIGN_POST.values().toArray(new Block[0]))
                .add(DecorationsBlocks.WOODEN_MAILBOX.values().toArray(new Block[0]))
                .add(DecorationsBlocks.BENCH.values().toArray(new Block[0]))
                .add(DecorationsBlocks.TABLE.values().toArray(new Block[0]))
                .add(DecorationsBlocks.TOOL_RACK.values().toArray(new Block[0]))
                .add(DecorationsBlocks.STUMP.values().toArray(Block[]::new))
                .add(DecorationsBlocks.STRIPPED_STUMP.values().toArray(Block[]::new))
                .add(DecorationsBlocks.COPPER_CAMPFIRE)
                .add(DecorationsBlocks.BASKET)
        ;

        this.valueLookupBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(DecorationsBlocks.WALL_SIGN_POST.values().toArray(new Block[0]))
                .add(DecorationsBlocks.WALL_LANTERN)
                .add(DecorationsBlocks.WALL_SOUL_LANTERN)
                .add(DecorationsBlocks.WALL_COPPER_LANTERNS.getAll())
                .add(DecorationsBlocks.BRAZIER)
                .add(DecorationsBlocks.SOUL_BRAZIER)
                .add(DecorationsBlocks.COPPER_BRAZIER)
                .add(DecorationsBlocks.LARGE_FLOWER_POT)
                .add(DecorationsBlocks.DISPLAY_CASE)
                .add(DecorationsBlocks.TRASHCAN)
        ;

        this.valueLookupBuilder(BlockTags.CAMPFIRES)
                .add(DecorationsBlocks.COPPER_CAMPFIRE);

        this.valueLookupBuilder(BlockTags.CLIMBABLE).add(DecorationsBlocks.ROPE);

        this.valueLookupBuilder(DecorationsBlockTags.BRAZIERS)
                .add(DecorationsBlocks.BRAZIER, DecorationsBlocks.SOUL_BRAZIER, DecorationsBlocks.COPPER_BRAZIER);

        this.valueLookupBuilder(DecorationsBlockTags.SHELVES)
                .add(DecorationsBlocks.SHELF.values().toArray(new Block[0]));

        this.valueLookupBuilder(DecorationsBlockTags.TOOL_RACKS)
                .add(DecorationsBlocks.TOOL_RACK.values().toArray(new Block[0]));

        this.valueLookupBuilder(DecorationsBlockTags.TABLES)
                .add(DecorationsBlocks.TABLE.values().toArray(new Block[0]));

        this.valueLookupBuilder(DecorationsBlockTags.MAILBOXES)
                .add(DecorationsBlocks.WOODEN_MAILBOX.values().toArray(new Block[0]));

        this.valueLookupBuilder(DecorationsBlockTags.SIGN_POSTS)
                .add(DecorationsBlocks.WOOD_SIGN_POST.values().toArray(new Block[0]))
                .add(DecorationsBlocks.WALL_SIGN_POST.values().toArray(new Block[0]));

        this.valueLookupBuilder(DecorationsBlockTags.BENCHES)
                .add(DecorationsBlocks.BENCH.values().toArray(new Block[0]));

        this.valueLookupBuilder(DecorationsBlockTags.UNCONNECTABLE)
                .addOptionalTag(DecorationsBlockTags.BENCHES)
                .addOptionalTag(DecorationsBlockTags.TABLES)
                .addOptionalTag(DecorationsBlockTags.SHELVES)
                .addOptionalTag(DecorationsBlockTags.BRAZIERS)
                .addOptionalTag(DecorationsBlockTags.MAILBOXES)
                .addOptionalTag(DecorationsBlockTags.STUMPS)
                .addOptionalTag(DecorationsBlockTags.SLEEPING_BAGS)
                .add(DecorationsBlocks.DISPLAY_CASE)
                .add(DecorationsBlocks.LARGE_FLOWER_POT)
                .add(DecorationsBlocks.BASKET)
                .add(DecorationsBlocks.LONG_FLOWER_POT)
                .add(DecorationsBlocks.WIND_CHIME)
        ;

        this.valueLookupBuilder(DecorationsBlockTags.ALLOWED_INTERACTIONS_BLOCKS)
                .addOptionalTag(DecorationsBlockTags.MAILBOXES);

        this.valueLookupBuilder(BlockTags.BAMBOO_PLANTABLE_ON)
                .add(DecorationsBlocks.LARGE_FLOWER_POT);
        this.valueLookupBuilder(BlockTags.DRY_VEGETATION_MAY_PLACE_ON)
                .add(DecorationsBlocks.LARGE_FLOWER_POT);
        this.valueLookupBuilder(BlockTags.BIG_DRIPLEAF_PLACEABLE)
                .add(DecorationsBlocks.LARGE_FLOWER_POT);
        this.valueLookupBuilder(BlockTags.SMALL_DRIPLEAF_PLACEABLE)
                .add(DecorationsBlocks.LARGE_FLOWER_POT);

        this.valueLookupBuilder(DecorationsBlockTags.STUMPS)
                .add(DecorationsBlocks.STUMP.values().toArray(Block[]::new))
                .add(DecorationsBlocks.STRIPPED_STUMP.values().toArray(Block[]::new))
        ;

        this.valueLookupBuilder(DecorationsBlockTags.SLEEPING_BAGS)
                .add(DecorationsBlocks.SLEEPING_BAG.values().toArray(Block[]::new))
        ;
    }
}
