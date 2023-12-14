package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.recipe.BedBannerCraftingRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.*;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

import static eu.pb4.polydecorations.ModInit.id;

class RecipesProvider extends FabricRecipeProvider {
    public RecipesProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        //noinspection unchecked
        var dyes = (List<DyeItem>) (Object) List.of(Items.BLACK_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.CYAN_DYE, Items.GRAY_DYE, Items.GREEN_DYE, Items.LIGHT_BLUE_DYE, Items.LIGHT_GRAY_DYE, Items.LIME_DYE, Items.MAGENTA_DYE, Items.ORANGE_DYE, Items.PINK_DYE, Items.PURPLE_DYE, Items.RED_DYE, Items.YELLOW_DYE, Items.WHITE_DYE);

        DecorationsItems.SHELF.forEach(((woodType, item) -> {
            var slab = Registries.ITEM.get(new Identifier(woodType.name() + "_slab"));
            var planks = Registries.ITEM.get(new Identifier(woodType.name() + "_planks"));
            if (slab == Items.AIR) {
                return;
            }
            new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, item, 2)
                    .group("polydecorations:shelf")
                    .pattern("-s-")
                    .input('-', Items.STICK)
                    .input('s', slab)
                    .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                    .offerTo(exporter);
        }));

        DecorationsItems.BENCH.forEach(((woodType, item) -> {
            var slab = Registries.ITEM.get(new Identifier(woodType.name() + "_slab"));
            var planks = Registries.ITEM.get(new Identifier(woodType.name() + "_planks"));
            if (slab == Items.AIR) {
                return;
            }
            new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, item, 2)
                    .group("polydecorations:bench")
                    .pattern("sss")
                    .pattern("- -")
                    .input('-', Items.STICK)
                    .input('s', slab)
                    .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                    .offerTo(exporter);
        }));

        new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, DecorationsBlocks.BRAZIER, 1)
                .group("polydecorations:brazier")
                .pattern("ici")
                .pattern(" i ")
                .input('c', Items.CAMPFIRE)
                .input('i', Items.IRON_INGOT)
                .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                .offerTo(exporter);

        new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, DecorationsBlocks.SOUL_BRAZIER, 1)
                .group("polydecorations:brazier")
                .pattern("ici")
                .pattern(" i ")
                .input('c', Items.SOUL_CAMPFIRE)
                .input('i', Items.IRON_INGOT)
                .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                .offerTo(exporter);

        /*DecorationsBlocks.BANNER_BED.forEach(((dye, item) -> {
            exporter.accept(
                    id(getRecipeName(item)),
                    new BedBannerCraftingRecipe(item.getBacking().asItem(), item.asItem()),
                    null
            );
        }));*/
    }
    public void of(RecipeExporter exporter, RecipeEntry<?>... recipes) {
        for (var recipe : recipes) {
            exporter.accept(recipe.id(), recipe.value(), null);
        }
    }
}
