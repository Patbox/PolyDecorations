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
        this.getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(DecorationsBlocks.SHELF.values().toArray(new Block[0]))
                .add(DecorationsBlocks.SIGN_POST.values().toArray(new Block[0]))
                .add(DecorationsBlocks.BENCH.values().toArray(new Block[0]));

        this.getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(DecorationsBlocks.WALL_LANTERN)
                .add(DecorationsBlocks.WALL_SOUL_LANTERN)
                .add(DecorationsBlocks.BRAZIER)
                .add(DecorationsBlocks.SOUL_BRAZIER)
                .add(DecorationsBlocks.LARGE_FLOWER_POT)
                .add(DecorationsBlocks.DISPLAY_CASE)
        ;

        this.getOrCreateTagBuilder(DecorationsBlockTags.BRAZIERS)
                .add(DecorationsBlocks.BRAZIER, DecorationsBlocks.SOUL_BRAZIER);

        this.getOrCreateTagBuilder(DecorationsBlockTags.SHELVES)
                .add(DecorationsBlocks.SHELF.values().toArray(new Block[0]));

        this.getOrCreateTagBuilder(DecorationsBlockTags.SIGN_POSTS)
                .add(DecorationsBlocks.SIGN_POST.values().toArray(new Block[0]));

        this.getOrCreateTagBuilder(DecorationsBlockTags.BENCHES)
                .add(DecorationsBlocks.BENCH.values().toArray(new Block[0]));

        this.getOrCreateTagBuilder(DecorationsBlockTags.UNCONNECTABLE)
                .addOptionalTag(DecorationsBlockTags.BENCHES)
                .addOptionalTag(DecorationsBlockTags.SHELVES)
                .addOptionalTag(DecorationsBlockTags.BRAZIERS)
                .add(DecorationsBlocks.DISPLAY_CASE)
                .add(DecorationsBlocks.LARGE_FLOWER_POT)
        ;
    }
}
