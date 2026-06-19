package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polydecorations.item.DecorationsItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class ItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {
    public ItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, @Nullable FabricTagsProvider.BlockTagsProvider blockTagsProvider) {
        super(output, registriesFuture, blockTagsProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        this.tag(DecorationsItemTags.GLOBE_REPLACEMENT)
                .add(get(Items.POTATO))
                .add(get(Items.PLAYER_HEAD))
                .add(get(Items.HEAVY_CORE))
                .add(get(Items.ZOMBIE_HEAD))
                .add(get(Items.CREEPER_HEAD))
                .add(get(Items.SKELETON_SKULL))
                .add(get(Items.WITHER_SKELETON_SKULL));

        this.tag(DecorationsItemTags.TOOL_RACK_ACCEPTABLE)
                .addOptionalTag(ConventionalItemTags.TOOLS)
                .addOptionalTag(ConventionalItemTags.RODS)
                .addOptionalTag(ConventionalItemTags.FISHING_ROD_TOOLS)
                .add(get(Items.SPYGLASS))
                .add(get(Items.MACE))
                .add(get(Items.FLINT_AND_STEEL))
                .add(get(Items.CARROT_ON_A_STICK))
                .add(get(Items.WARPED_FUNGUS_ON_A_STICK))
                .add(get(Items.WARPED_FUNGUS_ON_A_STICK))
        ;

        this.tag(ConventionalItemTags.TOOLS)
                .add(get(DecorationsItems.TROWEL))
                .add(get(DecorationsItems.HAMMER))
        ;

        this.tag(DecorationsItemTags.UNSCALED_DISPLAY_CASE)
                .add(get(Items.PLAYER_HEAD))
                .add(get(Items.ZOMBIE_HEAD))
                .add(get(Items.HEAVY_CORE))
                .add(get(Items.CREEPER_HEAD))
                .add(get(Items.SKELETON_SKULL))
                .add(get(Items.WITHER_SKELETON_SKULL))
        ;


        this.tag(DecorationsItemTags.FORCE_FIXED_MODEL)
                .add(get(Items.SPYGLASS))
                .add(get(Items.TRIDENT))
                .add(get(Items.SHIELD))
        ;

        this.tag(DecorationsItemTags.CANVAS_CLEAR_PIXELS)
                .add(get(Items.PAPER))
                .add(get(Items.SPONGE))
                .add(get(Items.WET_SPONGE));

        this.tag(DecorationsItemTags.CANVAS_DARKEN_PIXELS)
                .addOptionalTag(ItemTags.COALS);

        this.tag(DecorationsItemTags.CANVAS_LIGHTEN_PIXELS)
                .add(get(Items.BONE_MEAL));

        this.tag(DecorationsItemTags.STATUES)
                .addAll(DecorationsItems.WOODEN_STATUE.values().stream().map(this::get))
                .addAll(DecorationsItems.OTHER_STATUE.values().stream().map(this::get))
                ;

        this.tag(DecorationsItemTags.STUMPS)
                .addAll(DecorationsItems.STUMP.values().stream().map(this::get))
                .addAll(DecorationsItems.STRIPPED_STUMP.values().stream().map(this::get))
        ;

        this.tag(DecorationsItemTags.SLEEPING_BAGS)
                .addAll(DecorationsItems.SLEEPING_BAG.values().stream().map(this::get))
        ;

        this.tag(DecorationsItemTags.TIEABLE_CONTAINERS)
                .add(get(DecorationsItems.BASKET))
                .add(get(DecorationsItems.CARDBOARD_BOX))
                .addOptionalTag(ItemTags.SHULKER_BOXES)
        ;
    }

    // Temp workaround
    private ResourceKey<Item> get(Item item) {
        return item.builtInRegistryHolder().key();
    }
}
