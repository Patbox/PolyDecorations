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
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static eu.pb4.polydecorations.ModInit.id;

class RecipesProvider extends FabricRecipeProvider {


    public RecipesProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                var itemWrap = registryLookup.lookupOrThrow(Registries.ITEM);

                //noinspection unchecked
                var dyes = (List<DyeItem>) (Object) List.of(Items.BLACK_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.CYAN_DYE, Items.GRAY_DYE, Items.GREEN_DYE, Items.LIGHT_BLUE_DYE, Items.LIGHT_GRAY_DYE, Items.LIME_DYE, Items.MAGENTA_DYE, Items.ORANGE_DYE, Items.PINK_DYE, Items.PURPLE_DYE, Items.RED_DYE, Items.YELLOW_DYE, Items.WHITE_DYE);

                DecorationsItems.SHELF.forEach(((woodType, item) -> {
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:shelf")
                            .pattern("-s-")
                            .define('-', Items.STICK)
                            .define('s', slab)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                DecorationsItems.BENCH.forEach(((woodType, item) -> {
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:bench")
                            .pattern("sss")
                            .pattern("- -")
                            .define('-', Items.STICK)
                            .define('s', slab)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                DecorationsItems.TABLE.forEach(((woodType, item) -> {
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 1)
                            .group("polydecorations:table")
                            .pattern("sss")
                            .pattern("- -")
                            .pattern("- -")
                            .define('-', Items.STICK)
                            .define('s', slab)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                DecorationsItems.TOOL_RACK.forEach(((woodType, item) -> {
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 1)
                            .group("polydecorations:toolrack")
                            .pattern("s-s")
                            .pattern("-i-")
                            .pattern("s-s")
                            .define('-', Items.STICK)
                            .define('i', Items.IRON_INGOT)
                            .define('s', slab)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                DecorationsItems.WOODEN_MAILBOX.forEach(((woodType, item) -> {
                    var log = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(WoodUtil.getLogName(woodType)));
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:mailbox")
                            .pattern(" lc")
                            .pattern("sps")
                            .define('p', Items.PAPER)
                            .define('c', Items.COPPER_INGOT)
                            .define('l', log)
                            .define('s', slab)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                DecorationsItems.STUMP.forEach(((woodType, item) -> {
                    var log = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(WoodUtil.getLogName(woodType)));
                    if (log == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:stump")
                            .pattern("s")
                            .pattern("s")
                            .define('s', log)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(log))
                            .save(output);
                }));

                DecorationsItems.STRIPPED_STUMP.forEach(((woodType, item) -> {
                    var log = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("stripped_" + WoodUtil.getLogName(woodType)));
                    if (log == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:stump")
                            .pattern("s")
                            .pattern("s")
                            .define('s', log)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(log))
                            .save(output);
                }));

                DecorationsItems.SLEEPING_BAG.forEach(((color, item) -> {
                    var wool = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_wool"));
                    if (wool == Items.AIR) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item)
                            .group("polydecorations:sleeping_bag")
                            .pattern("sss")
                            .define('s', wool)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(wool))
                            .save(output);

                }));
                colorItemWithDye(List.copyOf(dyes), List.copyOf(DecorationsItems.SLEEPING_BAG.values()), "polydecorations:sleeping_bag_dying", RecipeCategory.DECORATIONS);

                DecorationsItems.SIGN_POST.forEach(((woodType, item) -> {
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(woodType.name() + "_planks"));
                    if (planks == null) {
                        return;
                    }
                    ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:sign_post")
                            .pattern("ss-")
                            .define('-', Items.STICK)
                            .define('s', planks)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.HAMMER, 1)
                        .pattern("nI ")
                        .pattern(" s ")
                        .define('n', Items.IRON_NUGGET)
                        .define('I', Items.IRON_INGOT)
                        .define('s', Items.STICK)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.TROWEL, 1)
                        .pattern("nI")
                        .pattern("sn")
                        .define('n', Items.IRON_NUGGET)
                        .define('I', Items.IRON_INGOT)
                        .define('s', Items.STICK)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);


                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.BRAZIER, 1)
                        .group("polydecorations:brazier")
                        .pattern("ici")
                        .pattern(" i ")
                        .define('c', Items.CAMPFIRE)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.SOUL_BRAZIER, 1)
                        .group("polydecorations:brazier")
                        .pattern("ici")
                        .pattern(" i ")
                        .define('c', Items.SOUL_CAMPFIRE)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.COPPER_BRAZIER, 1)
                        .group("polydecorations:brazier")
                        .pattern("ici")
                        .pattern(" i ")
                        .define('c', DecorationsItems.COPPER_CAMPFIRE)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                ShapelessRecipeBuilder.shapeless(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.COPPER_CAMPFIRE, 1)
                        .requires(Items.CAMPFIRE)
                        .requires(Items.RAW_COPPER)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.RAW_COPPER))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.TRASHCAN, 1)
                        .pattern("i i")
                        .pattern("ici")
                        .pattern("iii")
                        .define('c', Items.CACTUS)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.BASKET, 1)
                        .pattern("r r")
                        .pattern("sws")
                        .pattern("sss")
                        .define('r', DecorationsItems.ROPE)
                        .define('w', ItemTags.PLANKS)
                        .define('s', Items.STICK)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.ROPE, Items.STICK))
                        .save(output);

                ShapelessRecipeBuilder.shapeless(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.ROPE, 4)
                        .requires(Items.WHEAT)
                        .requires(Items.STRING, 4)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STRING))
                        .save(output);


                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.GLOBE, 1)
                        .pattern(" s")
                        .pattern("sw")
                        .pattern(" b")
                        .define('s', Items.STICK)
                        .define('b', Items.POLISHED_DEEPSLATE_SLAB)
                        .define('w', ItemTags.WOOL)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.CANVAS, 1)
                        .pattern("sss")
                        .pattern("sxs")
                        .pattern("sss")
                        .define('s', Items.STICK)
                        .define('x', Items.PAPER)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PAPER))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.DISPLAY_CASE, 1)
                        .pattern("g")
                        .pattern("s")
                        .define('g', Items.GLASS)
                        .define('s', Items.SMOOTH_STONE_SLAB)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLASS))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.LARGE_FLOWER_POT, 1)
                        .pattern("b b")
                        .pattern("bdb")
                        .pattern("bbb")
                        .define('b', Items.BRICK)
                        .define('d', Items.DIRT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BRICK))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.LONG_FLOWER_POT, 1)
                        .pattern("bfb")
                        .define('b', Items.BRICK)
                        .define('f', Items.FLOWER_POT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BRICK))
                        .save(output);

                ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.WIND_CHIME, 1)
                        .pattern(" c ")
                        .pattern("ipi")
                        .pattern("iii")
                        .define('c', Items.IRON_CHAIN)
                        .define('p', ItemTags.PLANKS)
                        .define('i', Items.IRON_NUGGET)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                ShapelessRecipeBuilder.shapeless(itemWrap, RecipeCategory.DECORATIONS, DecorationsItems.GHOST_LIGHT, 1)
                        .requires(Items.FIRE_CHARGE)
                        .requires(ItemTags.SOUL_FIRE_BASE_BLOCKS)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BRICK))
                        .save(output);

                output.accept(key("wind_chime_coloring"), new ColorWindChimeRecipe(CraftingBookCategory.BUILDING), null);

                {
                    output.accept(key("canvas_waxing"), new CanvasTransformRecipe("", "wax", CraftingBookCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.HONEYCOMB))), null);

                    output.accept(key("canvas_glowing"), new CanvasTransformRecipe("", "glow", CraftingBookCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.GLOW_INK_SAC))), null);

                    output.accept(key("canvas_unglowing"), new CanvasTransformRecipe("", "unglow", CraftingBookCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.INK_SAC))), null);

                    output.accept(key("canvas_cut"), new CanvasTransformRecipe("", "cut", CraftingBookCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.SHEARS))), null);

                    output.accept(key("canvas_uncut"), new CanvasTransformRecipe("", "uncut", CraftingBookCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.PAPER))), null);

                    output.accept(key("canvas_dye"), new CanvasTransformRecipe("", "dye", CraftingBookCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(itemWrap.getOrThrow(ConventionalItemTags.DYES)))), null);
                    output.accept(key("canvas_undye"), new CanvasTransformRecipe("", "dye", CraftingBookCategory.MISC,
                            new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.WATER_BUCKET))), null);


                    output.accept(key("canvas_clone"), new CloneCanvasCraftingRecipe("", DecorationsItems.CANVAS), null);
                }

                for (var x : BuiltInRegistries.ITEM) {
                    if (x instanceof StatueItem item) {
                        ShapedRecipeBuilder.shaped(itemWrap, RecipeCategory.DECORATIONS, item, 1)
                                .pattern(" x ")
                                .pattern("x#x")
                                .pattern(" x ")
                                .define('#', Items.ARMOR_STAND)
                                .define('x', item.getType().block())
                                .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ARMOR_STAND))
                                .save(output);
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

            public ResourceKey<Recipe<?>> key(String path) {
                return ResourceKey.create(Registries.RECIPE, id(path));
            }

            public void of(RecipeOutput exporter, RecipeHolder<?>... recipes) {
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
