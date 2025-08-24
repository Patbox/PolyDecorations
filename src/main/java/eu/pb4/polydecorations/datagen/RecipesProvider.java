package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.item.StatueItem;
import eu.pb4.polydecorations.recipe.CloneCanvasCraftingRecipe;
import eu.pb4.polydecorations.recipe.CanvasTransformRecipe;
import eu.pb4.polydecorations.recipe.ColorWindChimeRecipe;
import eu.pb4.polydecorations.util.WoodUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.recipe.*;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static eu.pb4.polydecorations.ModInit.id;

class RecipesProvider extends FabricRecipeProvider {


    public RecipesProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                var itemWrap = registryLookup.getOrThrow(RegistryKeys.ITEM);

                //noinspection unchecked
                var dyes = (List<DyeItem>) (Object) List.of(Items.BLACK_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.CYAN_DYE, Items.GRAY_DYE, Items.GREEN_DYE, Items.LIGHT_BLUE_DYE, Items.LIGHT_GRAY_DYE, Items.LIME_DYE, Items.MAGENTA_DYE, Items.ORANGE_DYE, Items.PINK_DYE, Items.PURPLE_DYE, Items.RED_DYE, Items.YELLOW_DYE, Items.WHITE_DYE);

                DecorationsItems.SHELF.forEach(((woodType, item) -> {
                    var slab = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_slab"));
                    var planks = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:shelf")
                            .pattern("-s-")
                            .input('-', Items.STICK)
                            .input('s', slab)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                            .offerTo(exporter);
                }));

                DecorationsItems.BENCH.forEach(((woodType, item) -> {
                    var slab = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_slab"));
                    var planks = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:bench")
                            .pattern("sss")
                            .pattern("- -")
                            .input('-', Items.STICK)
                            .input('s', slab)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                            .offerTo(exporter);
                }));

                DecorationsItems.TABLE.forEach(((woodType, item) -> {
                    var slab = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_slab"));
                    var planks = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 1)
                            .group("polydecorations:table")
                            .pattern("sss")
                            .pattern("- -")
                            .pattern("- -")
                            .input('-', Items.STICK)
                            .input('s', slab)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                            .offerTo(exporter);
                }));

                DecorationsItems.TOOL_RACK.forEach(((woodType, item) -> {
                    var slab = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_slab"));
                    var planks = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 1)
                            .group("polydecorations:toolrack")
                            .pattern("s-s")
                            .pattern("-i-")
                            .pattern("s-s")
                            .input('-', Items.STICK)
                            .input('i', Items.IRON_INGOT)
                            .input('s', slab)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                            .offerTo(exporter);
                }));

                DecorationsItems.WOODEN_MAILBOX.forEach(((woodType, item) -> {
                    var log = Registries.ITEM.get(Identifier.ofVanilla(WoodUtil.getLogName(woodType)));
                    var slab = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_slab"));
                    var planks = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:mailbox")
                            .pattern(" lc")
                            .pattern("sps")
                            .input('p', Items.PAPER)
                            .input('c', Items.COPPER_INGOT)
                            .input('l', log)
                            .input('s', slab)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                            .offerTo(exporter);
                }));

                DecorationsItems.STUMP.forEach(((woodType, item) -> {
                    var log = Registries.ITEM.get(Identifier.ofVanilla(WoodUtil.getLogName(woodType)));
                    if (log == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:stump")
                            .pattern("s")
                            .pattern("s")
                            .input('s', log)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(log))
                            .offerTo(exporter);
                }));

                DecorationsItems.STRIPPED_STUMP.forEach(((woodType, item) -> {
                    var log = Registries.ITEM.get(Identifier.ofVanilla("stripped_" + WoodUtil.getLogName(woodType)));
                    if (log == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:stump")
                            .pattern("s")
                            .pattern("s")
                            .input('s', log)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(log))
                            .offerTo(exporter);
                }));

                DecorationsItems.SLEEPING_BAG.forEach(((color, item) -> {
                    var wool = Registries.ITEM.get(Identifier.ofVanilla(color.asString() + "_wool"));
                    if (wool == Items.AIR) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item)
                            .group("polydecorations:sleeping_bag")
                            .pattern("sss")
                            .input('s', wool)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(wool))
                            .offerTo(exporter);

                }));
                offerDyeableRecipes(List.copyOf(dyes), List.copyOf(DecorationsItems.SLEEPING_BAG.values()), "polydecorations:sleeping_bag_dying", RecipeCategory.DECORATIONS);

                DecorationsItems.SIGN_POST.forEach(((woodType, item) -> {
                    var planks = Registries.ITEM.get(Identifier.ofVanilla(woodType.name() + "_planks"));
                    if (planks == null) {
                        return;
                    }
                    ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:sign_post")
                            .pattern("ss-")
                            .input('-', Items.STICK)
                            .input('s', planks)
                            .criterion("planks", InventoryChangedCriterion.Conditions.items(planks))
                            .offerTo(exporter);
                }));

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.HAMMER, 1)
                        .pattern("nI ")
                        .pattern(" s ")
                        .input('n', Items.IRON_NUGGET)
                        .input('I', Items.IRON_INGOT)
                        .input('s', Items.STICK)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.TROWEL, 1)
                        .pattern("nI")
                        .pattern("sn")
                        .input('n', Items.IRON_NUGGET)
                        .input('I', Items.IRON_INGOT)
                        .input('s', Items.STICK)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                        .offerTo(exporter);


                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.BRAZIER, 1)
                        .group("polydecorations:brazier")
                        .pattern("ici")
                        .pattern(" i ")
                        .input('c', Items.CAMPFIRE)
                        .input('i', Items.IRON_INGOT)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.SOUL_BRAZIER, 1)
                        .group("polydecorations:brazier")
                        .pattern("ici")
                        .pattern(" i ")
                        .input('c', Items.SOUL_CAMPFIRE)
                        .input('i', Items.IRON_INGOT)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.TRASHCAN, 1)
                        .pattern("i i")
                        .pattern("ici")
                        .pattern("iii")
                        .input('c', Items.CACTUS)
                        .input('i', Items.IRON_INGOT)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.BASKET, 1)
                        .pattern("r r")
                        .pattern("sws")
                        .pattern("sss")
                        .input('r', DecorationsItems.ROPE)
                        .input('w', ItemTags.PLANKS)
                        .input('s', Items.STICK)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(DecorationsItems.ROPE, Items.STICK))
                        .offerTo(exporter);

                ShapelessRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.ROPE, 4)
                        .input(Items.WHEAT)
                        .input(Items.STRING, 4)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.STRING))
                        .offerTo(exporter);


                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.GLOBE, 1)
                        .pattern(" s")
                        .pattern("sw")
                        .pattern(" b")
                        .input('s', Items.STICK)
                        .input('b', Items.POLISHED_DEEPSLATE_SLAB)
                        .input('w', ItemTags.WOOL)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.CANVAS, 1)
                        .pattern("sss")
                        .pattern("sxs")
                        .pattern("sss")
                        .input('s', Items.STICK)
                        .input('x', Items.PAPER)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.PAPER))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.DISPLAY_CASE, 1)
                        .pattern("g")
                        .pattern("s")
                        .input('g', Items.GLASS)
                        .input('s', Items.SMOOTH_STONE_SLAB)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.GLASS))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.LARGE_FLOWER_POT, 1)
                        .pattern("b b")
                        .pattern("bdb")
                        .pattern("bbb")
                        .input('b', Items.BRICK)
                        .input('d', Items.DIRT)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.BRICK))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.LONG_FLOWER_POT, 1)
                        .pattern("bfb")
                        .input('b', Items.BRICK)
                        .input('f', Items.FLOWER_POT)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.BRICK))
                        .offerTo(exporter);

                ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.WIND_CHIME, 1)
                        .pattern(" c ")
                        .pattern("ipi")
                        .pattern("iii")
                        .input('c', Items.CHAIN)
                        .input('p', ItemTags.PLANKS)
                        .input('i', Items.IRON_NUGGET)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.IRON_INGOT))
                        .offerTo(exporter);

                ShapelessRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.GHOST_LIGHT, 1)
                        .input(Items.FIRE_CHARGE)
                        .input(ItemTags.SOUL_FIRE_BASE_BLOCKS)
                        .criterion("planks", InventoryChangedCriterion.Conditions.items(Items.BRICK))
                        .offerTo(exporter);

                exporter.accept(key("wind_chime_coloring"), new ColorWindChimeRecipe(CraftingRecipeCategory.BUILDING), null);

                {
                    exporter.accept(key("canvas_waxing"), new CanvasTransformRecipe("", "wax", CraftingRecipeCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.ofItems(DecorationsItems.CANVAS), List.of(Ingredient.ofItems(Items.HONEYCOMB))), null);

                    exporter.accept(key("canvas_glowing"), new CanvasTransformRecipe("", "glow", CraftingRecipeCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.ofItems(DecorationsItems.CANVAS), List.of(Ingredient.ofItems(Items.GLOW_INK_SAC))), null);

                    exporter.accept(key("canvas_unglowing"), new CanvasTransformRecipe("", "unglow", CraftingRecipeCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.ofItems(DecorationsItems.CANVAS), List.of(Ingredient.ofItems(Items.INK_SAC))), null);

                    exporter.accept(key("canvas_cut"), new CanvasTransformRecipe("", "cut", CraftingRecipeCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.ofItems(DecorationsItems.CANVAS), List.of(Ingredient.ofItems(Items.SHEARS))), null);

                    exporter.accept(key("canvas_uncut"), new CanvasTransformRecipe("", "uncut", CraftingRecipeCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.ofItems(DecorationsItems.CANVAS), List.of(Ingredient.ofItems(Items.PAPER))), null);

                    exporter.accept(key("canvas_dye"), new CanvasTransformRecipe("", "dye", CraftingRecipeCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.ofItems(DecorationsItems.CANVAS), List.of(Ingredient.ofTag(itemWrap.getOrThrow(ConventionalItemTags.DYES)))), null);
                    exporter.accept(key("canvas_undye"), new CanvasTransformRecipe("", "dye", CraftingRecipeCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.ofItems(DecorationsItems.CANVAS), List.of(Ingredient.ofItems(Items.WATER_BUCKET))), null);


                    exporter.accept(key("canvas_clone"), new CloneCanvasCraftingRecipe("", DecorationsItems.CANVAS), null);
                }

                for (var x : Registries.ITEM) {
                    if (x instanceof StatueItem item) {
                        ShapedRecipeJsonBuilder.create(itemWrap, RecipeCategory.DECORATIONS, item, 1)
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

            public RegistryKey<Recipe<?>> key(String path) {
                return RegistryKey.of(RegistryKeys.RECIPE, id(path));
            }

            public void of(RecipeExporter exporter, RecipeEntry<?>... recipes) {
                for (var recipe : recipes) {
                    exporter.accept(recipe.id(), recipe.value(), null);
                }
            }
        };
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
