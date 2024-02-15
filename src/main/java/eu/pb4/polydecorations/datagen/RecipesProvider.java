package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.item.StatueItem;
import eu.pb4.polydecorations.recipe.NbtCloningCraftingRecipe;
import eu.pb4.polydecorations.recipe.ShapelessNbtCopyRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.*;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

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

        DecorationsItems.SIGN_POST.forEach(((woodType, item) -> {
            var planks = Registries.ITEM.get(new Identifier(woodType.name() + "_planks"));
            if (planks == null) {
                return;
            }
            new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, item, 2)
                    .group("polydecorations:sign_post")
                    .pattern("ss-")
                    .input('-', Items.STICK)
                    .input('s', planks)
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

        new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, DecorationsBlocks.GLOBE, 1)
                .pattern(" s")
                .pattern("sw")
                .pattern(" b")
                .input('s', Items.STICK)
                .input('b', Items.POLISHED_DEEPSLATE_SLAB)
                .input('w', ItemTags.WOOL)
                .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                .offerTo(exporter);

        new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, DecorationsItems.CANVAS, 1)
                .pattern("sss")
                .pattern("sxs")
                .pattern("sss")
                .input('s', Items.STICK)
                .input('x', Items.PAPER)
                .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.PAPER))
                .offerTo(exporter);

        new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, DecorationsItems.DISPLAY_CASE, 1)
                .pattern("g")
                .pattern("s")
                .input('g', Items.GLASS)
                .input('s', Items.SMOOTH_STONE_SLAB)
                .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.GLASS))
                .offerTo(exporter);

        new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, DecorationsItems.LARGE_FLOWER_POT, 1)
                .pattern("b b")
                .pattern("bdb")
                .pattern("bbb")
                .input('b', Items.BRICK)
                .input('d', Items.DIRT)
                .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.BRICK))
                .offerTo(exporter);

        {
            var waxed = new ItemStack(DecorationsItems.CANVAS);
            waxed.getOrCreateNbt().putBoolean("waxed", true);
            exporter.accept(id("canvas_waxing"), new ShapelessNbtCopyRecipe("", CraftingRecipeCategory.MISC,
                    waxed, Ingredient.ofItems(DecorationsItems.CANVAS), DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofItems(Items.HONEYCOMB))), null);

            var glowing = new ItemStack(DecorationsItems.CANVAS);
            glowing.getOrCreateNbt().putBoolean("glowing", true);

            exporter.accept(id("canvas_glowing"), new ShapelessNbtCopyRecipe("", CraftingRecipeCategory.MISC,
                    glowing, Ingredient.ofItems(DecorationsItems.CANVAS), DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofItems(Items.GLOW_INK_SAC))), null);

            var unglowing = new ItemStack(DecorationsItems.CANVAS);
            unglowing.getOrCreateNbt().putBoolean("glowing", false);

            exporter.accept(id("canvas_unglowing"), new ShapelessNbtCopyRecipe("", CraftingRecipeCategory.MISC,
                    unglowing, Ingredient.ofItems(DecorationsItems.CANVAS), DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofItems(Items.GLOW_INK_SAC))), null);

            exporter.accept(id("canvas_clone"), new NbtCloningCraftingRecipe("", DecorationsItems.CANVAS), null);
        }

        for (var x : Registries.ITEM) {
            if (x instanceof StatueItem item) {
                new ShapedRecipeJsonBuilder(RecipeCategory.DECORATIONS, item, 1)
                        .pattern(" x ")
                        .pattern("x#x")
                        .pattern(" x ")
                        .input('#', Items.ARMOR_STAND)
                        .input('x', item.getType().block())
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.ARMOR_STAND))
                        .offerTo(exporter);
            }
        }

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
