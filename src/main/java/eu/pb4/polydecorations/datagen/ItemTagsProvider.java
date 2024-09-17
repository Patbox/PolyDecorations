package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polydecorations.item.DecorationsItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

class ItemTagsProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, @Nullable FabricTagProvider.BlockTagProvider blockTagProvider) {
        super(output, registriesFuture, blockTagProvider);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        this.getOrCreateTagBuilder(DecorationsItemTags.GLOBE_REPLACEMENT)
                .add(Items.POTATO)
                .add(Items.PLAYER_HEAD)
                .add(Items.HEAVY_CORE)
                .add(Items.ZOMBIE_HEAD)
                .add(Items.CREEPER_HEAD)
                .add(Items.SKELETON_SKULL)
                .add(Items.WITHER_SKELETON_SKULL);

        this.getOrCreateTagBuilder(DecorationsItemTags.TOOL_RACK_ACCEPTABLE)
                .addOptionalTag(ConventionalItemTags.TOOLS)
                .addOptionalTag(ConventionalItemTags.RODS)
                .addOptionalTag(ConventionalItemTags.FISHING_ROD_TOOLS)
                .add(Items.SPYGLASS)
                .add(Items.MACE)
                .add(Items.FLINT_AND_STEEL)
                .add(Items.CARROT_ON_A_STICK)
                .add(Items.WARPED_FUNGUS_ON_A_STICK)
                .add(Items.WARPED_FUNGUS_ON_A_STICK)
        ;

        this.getOrCreateTagBuilder(ConventionalItemTags.TOOLS)
                .add(DecorationsItems.TROWEL)
                .add(DecorationsItems.HAMMER)
        ;

        this.getOrCreateTagBuilder(DecorationsItemTags.UNSCALED_DISPLAY_CASE)
                .add(Items.PLAYER_HEAD)
                .add(Items.ZOMBIE_HEAD)
                .add(Items.HEAVY_CORE)
                .add(Items.CREEPER_HEAD)
                .add(Items.SKELETON_SKULL)
                .add(Items.WITHER_SKELETON_SKULL)
        ;


        this.getOrCreateTagBuilder(DecorationsItemTags.FORCE_FIXED_MODEL)
                .add(Items.SPYGLASS)
                .add(Items.TRIDENT)
                .add(Items.SHIELD)
        ;

        this.getOrCreateTagBuilder(DecorationsItemTags.CANVAS_CLEAR_PIXELS)
                .add(Items.PAPER)
                .add(Items.SPONGE)
                .add(Items.WET_SPONGE);

        this.getOrCreateTagBuilder(DecorationsItemTags.CANVAS_DARKEN_PIXELS)
                .addOptionalTag(ItemTags.COALS);

        this.getOrCreateTagBuilder(DecorationsItemTags.CANVAS_LIGHTEN_PIXELS)
                .add(Items.BONE_MEAL);
    }


}
