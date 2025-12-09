package eu.pb4.polydecorations.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;

class EntityTagsProvider extends FabricTagProvider.EntityTypeTagProvider {
    public EntityTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
    }
}
