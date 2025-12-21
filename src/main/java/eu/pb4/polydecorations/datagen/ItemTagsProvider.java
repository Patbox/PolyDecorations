package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polydecorations.item.DecorationsItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class ItemTagsProvider extends FabricTagProvider.ItemTagProvider {
    public ItemTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, @Nullable FabricTagProvider.BlockTagProvider blockTagProvider) {
        super(output, registriesFuture, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        this.valueLookupBuilder(DecorationsItemTags.GLOBE_REPLACEMENT)
                .add(Items.POTATO)
                .add(Items.PLAYER_HEAD)
                .add(Items.HEAVY_CORE)
                .add(Items.ZOMBIE_HEAD)
                .add(Items.CREEPER_HEAD)
                .add(Items.SKELETON_SKULL)
                .add(Items.WITHER_SKELETON_SKULL);

        this.valueLookupBuilder(DecorationsItemTags.TOOL_RACK_ACCEPTABLE)
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

        this.valueLookupBuilder(ConventionalItemTags.TOOLS)
                .add(DecorationsItems.TROWEL)
                .add(DecorationsItems.HAMMER)
        ;

        this.valueLookupBuilder(DecorationsItemTags.UNSCALED_DISPLAY_CASE)
                .add(Items.PLAYER_HEAD)
                .add(Items.ZOMBIE_HEAD)
                .add(Items.HEAVY_CORE)
                .add(Items.CREEPER_HEAD)
                .add(Items.SKELETON_SKULL)
                .add(Items.WITHER_SKELETON_SKULL)
        ;


        this.valueLookupBuilder(DecorationsItemTags.FORCE_FIXED_MODEL)
                .add(Items.SPYGLASS)
                .add(Items.TRIDENT)
                .add(Items.SHIELD)
        ;

        this.valueLookupBuilder(DecorationsItemTags.CANVAS_CLEAR_PIXELS)
                .add(Items.PAPER)
                .add(Items.SPONGE)
                .add(Items.WET_SPONGE);

        this.valueLookupBuilder(DecorationsItemTags.CANVAS_DARKEN_PIXELS)
                .addOptionalTag(ItemTags.COALS);

        this.valueLookupBuilder(DecorationsItemTags.CANVAS_LIGHTEN_PIXELS)
                .add(Items.BONE_MEAL);

        this.valueLookupBuilder(DecorationsItemTags.STATUES)
                .add(DecorationsItems.WOODEN_STATUE.values().toArray(Item[]::new))
                .add(DecorationsItems.OTHER_STATUE.values().toArray(Item[]::new))
                ;

        this.valueLookupBuilder(DecorationsItemTags.STUMPS)
                .add(DecorationsItems.STUMP.values().toArray(Item[]::new))
                .add(DecorationsItems.STRIPPED_STUMP.values().toArray(Item[]::new))
        ;

        this.valueLookupBuilder(DecorationsItemTags.SLEEPING_BAGS)
                .add(DecorationsItems.SLEEPING_BAG.values().toArray(Item[]::new))
        ;

        this.valueLookupBuilder(DecorationsItemTags.TIEABLE_CONTAINERS)
                .add(DecorationsItems.BASKET)
                .add(DecorationsItems.CARDBOARD_BOX)
                .addOptionalTag(ItemTags.SHULKER_BOXES)
        ;
    }


}
