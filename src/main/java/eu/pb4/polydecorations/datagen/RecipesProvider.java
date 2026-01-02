package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.item.StatueItem;
import eu.pb4.polydecorations.recipe.CanvasTransformRecipe;
import eu.pb4.polydecorations.recipe.CloneCanvasCraftingRecipe;
import eu.pb4.polydecorations.recipe.ColorWindChimeRecipe;
import eu.pb4.polydecorations.recipe.ComponentApplyCraftingRecipe;
import eu.pb4.polydecorations.util.WoodUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static eu.pb4.polydecorations.ModInit.id;
import static eu.pb4.polydecorations.util.DecorationsUtil.getValues;

public class RecipesProvider extends FabricRecipeProvider {


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

                woodRecipeProvider(WoodUtil.VANILLA, registryLookup, exporter).buildRecipes();

                DecorationsItems.SLEEPING_BAG.forEach(((color, item) -> {
                    var wool = BuiltInRegistries.ITEM.getValue(Identifier.parse(color.getSerializedName() + "_wool"));
                    if (wool == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item)
                            .group("polydecorations:sleeping_bag")
                            .pattern("sss")
                            .define('s', wool)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(wool))
                            .save(output);

                }));
                colorItemWithDye(List.copyOf(dyes), List.copyOf(DecorationsItems.SLEEPING_BAG.values()), "polydecorations:sleeping_bag_dying", RecipeCategory.DECORATIONS);

                shaped(RecipeCategory.DECORATIONS, DecorationsItems.HAMMER, 1)
                        .pattern("nI ")
                        .pattern(" s ")
                        .define('n', Items.IRON_NUGGET)
                        .define('I', Items.IRON_INGOT)
                        .define('s', Items.STICK)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsItems.TROWEL, 1)
                        .pattern("nI")
                        .pattern("sn")
                        .define('n', Items.IRON_NUGGET)
                        .define('I', Items.IRON_INGOT)
                        .define('s', Items.STICK)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);


                shaped(RecipeCategory.DECORATIONS, DecorationsBlocks.BRAZIER, 1)
                        .group("polydecorations:brazier")
                        .pattern("ici")
                        .pattern(" i ")
                        .define('c', Items.CAMPFIRE)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsBlocks.SOUL_BRAZIER, 1)
                        .group("polydecorations:brazier")
                        .pattern("ici")
                        .pattern(" i ")
                        .define('c', Items.SOUL_CAMPFIRE)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsBlocks.COPPER_BRAZIER, 1)
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

                shaped(RecipeCategory.DECORATIONS, DecorationsBlocks.TRASHCAN, 1)
                        .pattern("i i")
                        .pattern("ici")
                        .pattern("iii")
                        .define('c', Items.CACTUS)
                        .define('i', Items.IRON_INGOT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsBlocks.BASKET, 1)
                        .pattern("r r")
                        .pattern("sws")
                        .pattern("sss")
                        .define('r', DecorationsItems.ROPE)
                        .define('w', ItemTags.PLANKS)
                        .define('s', Items.STICK)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.ROPE, Items.STICK))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsBlocks.CARDBOARD_BOX, 1)
                        .pattern("pwp")
                        .pattern("wsw")
                        .pattern("pwp")
                        .define('p', Items.PAPER)
                        .define('w', ItemTags.PLANKS)
                        .define('s', Items.SLIME_BALL)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PAPER))
                        .save(output);

                ShapelessRecipeBuilder.shapeless(itemWrap, RecipeCategory.DECORATIONS, DecorationsBlocks.ROPE, 4)
                        .requires(Items.WHEAT)
                        .requires(Items.STRING, 4)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STRING))
                        .save(output);


                shaped(RecipeCategory.DECORATIONS, DecorationsBlocks.GLOBE, 1)
                        .pattern(" s")
                        .pattern("sw")
                        .pattern(" b")
                        .define('s', Items.STICK)
                        .define('b', Items.POLISHED_DEEPSLATE_SLAB)
                        .define('w', ItemTags.WOOL)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsItems.CANVAS, 1)
                        .pattern("sss")
                        .pattern("sxs")
                        .pattern("sss")
                        .define('s', Items.STICK)
                        .define('x', Items.PAPER)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PAPER))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsItems.DISPLAY_CASE, 1)
                        .pattern("g")
                        .pattern("s")
                        .define('g', Items.GLASS)
                        .define('s', Items.SMOOTH_STONE_SLAB)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GLASS))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsItems.LARGE_FLOWER_POT, 1)
                        .pattern("b b")
                        .pattern("bdb")
                        .pattern("bbb")
                        .define('b', Items.BRICK)
                        .define('d', Items.DIRT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BRICK))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsItems.LONG_FLOWER_POT, 1)
                        .pattern("bfb")
                        .define('b', Items.BRICK)
                        .define('f', Items.FLOWER_POT)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BRICK))
                        .save(output);

                shaped(RecipeCategory.DECORATIONS, DecorationsItems.WIND_CHIME, 1)
                        .pattern(" c ")
                        .pattern("ipi")
                        .pattern("iii")
                        .define('c', Items.IRON_CHAIN)
                        .define('p', ItemTags.PLANKS)
                        .define('i', Items.IRON_NUGGET)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                        .save(output);

                shapeless(RecipeCategory.DECORATIONS, DecorationsItems.GHOST_LIGHT, 1)
                        .requires(Items.FIRE_CHARGE)
                        .requires(ItemTags.SOUL_FIRE_BASE_BLOCKS)
                        .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SOUL_SAND, Items.SOUL_SAND))
                        .save(output);

                shapeless(RecipeCategory.DECORATIONS, DecorationsItems.BURNING_GHOST_LIGHT).requires(DecorationsItems.GHOST_LIGHT).requires(ItemTags.COALS)
                        .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.GHOST_LIGHT))
                        .unlockedBy("item2", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SOUL_SAND))
                        .unlockedBy("item3", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SOUL_SOIL))
                        .save(output);

                shapeless(RecipeCategory.DECORATIONS, DecorationsItems.COPPER_GHOST_LIGHT).requires(DecorationsItems.GHOST_LIGHT).requires(Items.COPPER_NUGGET)
                        .unlockedBy("item", InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.GHOST_LIGHT))
                        .unlockedBy("item2", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SOUL_SAND))
                        .unlockedBy("item3", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SOUL_SOIL))
                        .save(output);

                output.accept(key("wind_chime_coloring"), new ColorWindChimeRecipe(CraftingBookCategory.BUILDING), null);

                {

                    acceptWithUnlock(output, key("canvas_waxing"), new CanvasTransformRecipe("", "wax", CraftingBookCategory.MISC,
                                    new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.HONEYCOMB))),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));

                    acceptWithUnlock(output, key("canvas_glowing"), new CanvasTransformRecipe("", "glow", CraftingBookCategory.MISC,
                                    new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.GLOW_INK_SAC))),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));

                    acceptWithUnlock(output, key("canvas_unglowing"), new CanvasTransformRecipe("", "unglow", CraftingBookCategory.MISC,
                                    new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.INK_SAC))),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));

                    acceptWithUnlock(output, key("canvas_cut"), new CanvasTransformRecipe("", "cut", CraftingBookCategory.MISC,
                                    new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.SHEARS))),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));

                    acceptWithUnlock(output, key("canvas_uncut"), new CanvasTransformRecipe("", "uncut", CraftingBookCategory.MISC,
                                    new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.PAPER))),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));

                    acceptWithUnlock(output, key("canvas_dye"), new CanvasTransformRecipe("", "dye", CraftingBookCategory.MISC,
                                    new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(itemWrap.getOrThrow(ConventionalItemTags.DYES)))),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));
                    acceptWithUnlock(output, key("canvas_undye"), new CanvasTransformRecipe("", "dye", CraftingBookCategory.MISC,
                                    new ItemStack(DecorationsItems.CANVAS), Ingredient.of(DecorationsItems.CANVAS), List.of(Ingredient.of(Items.WATER_BUCKET))),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));
                    acceptWithUnlock(output, key("canvas_clone"), new CloneCanvasCraftingRecipe("", DecorationsItems.CANVAS),
                            InventoryChangeTrigger.TriggerInstance.hasItems(DecorationsItems.CANVAS));
                }

                acceptWithUnlock(output, key("tie_container"), new ComponentApplyCraftingRecipe("", CraftingBookCategory.MISC,
                        this.tag(DecorationsItemTags.TIEABLE_CONTAINERS), List.of(Ingredient.of(Items.STRING)),
                        DataComponentPatch.builder().set(DecorationsDataComponents.TIED, Unit.INSTANCE).build()
                ), InventoryChangeTrigger.TriggerInstance.hasItems(Items.STRING));


                DecorationsItems.OTHER_STATUE.forEach((type, item) -> {
                    shaped(RecipeCategory.DECORATIONS, item, 1)
                            .pattern(" x ")
                            .pattern("x#x")
                            .pattern(" x ")
                            .define('#', Items.ARMOR_STAND)
                            .define('x', item.getType().block())
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ARMOR_STAND))
                            .save(output);
                });
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

            private void acceptWithUnlock(RecipeOutput output, ResourceKey<Recipe<?>> resourceKey, CraftingRecipe wax, Criterion<?> criterion) {
                var advancement = output.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceKey))
                        .rewards(AdvancementRewards.Builder.recipe(resourceKey))
                        .requirements(AdvancementRequirements.Strategy.OR)
                        .addCriterion("item", criterion);
                output.accept(resourceKey, wax, advancement.build(resourceKey.identifier().withPrefix("recipes/" + wax.category().getSerializedName() + "/")));
            }
        };
    }
    
    public static RecipeProvider woodRecipeProvider(List<WoodType> woodTypes, HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                getValues(DecorationsItems.WOODEN_STATUE, woodTypes, (type, item) -> {
                    shaped(RecipeCategory.DECORATIONS, item, 1)
                            .pattern(" x ")
                            .pattern("x#x")
                            .pattern(" x ")
                            .define('#', Items.ARMOR_STAND)
                            .define('x', item.getType().block())
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ARMOR_STAND))
                            .save(output);
                });

                getValues(DecorationsItems.SHELF, woodTypes, ((woodType, item) -> {
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:shelf")
                            .pattern("-s-")
                            .define('-', Items.STICK)
                            .define('s', slab)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                getValues(DecorationsItems.BENCH, woodTypes, ((woodType, item) -> {
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:bench")
                            .pattern("sss")
                            .pattern("- -")
                            .define('-', Items.STICK)
                            .define('s', slab)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));

                getValues(DecorationsItems.TABLE, woodTypes, ((woodType, item) -> {
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 1)
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
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 1)
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
                    var log = BuiltInRegistries.ITEM.getValue(WoodUtil.getLogName(woodType));
                    var slab = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_slab"));
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_planks"));
                    if (slab == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 2)
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

                getValues(DecorationsItems.STUMP, woodTypes, ((woodType, item) -> {
                    var log = BuiltInRegistries.ITEM.getValue(WoodUtil.getLogName(woodType));
                    if (log == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:stump")
                            .pattern("s")
                            .pattern("s")
                            .define('s', log)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(log))
                            .save(output);
                }));

                DecorationsItems.STRIPPED_STUMP.forEach(((woodType, item) -> {
                    var log = BuiltInRegistries.ITEM.getValue(WoodUtil.getLogName(woodType).withPrefix("stripped_"));
                    if (log == Items.AIR) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:stump")
                            .pattern("s")
                            .pattern("s")
                            .define('s', log)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(log))
                            .save(output);
                }));

                DecorationsItems.SIGN_POST.forEach(((woodType, item) -> {
                    var planks = BuiltInRegistries.ITEM.getValue(Identifier.parse(woodType.name() + "_planks"));
                    if (planks == null) {
                        return;
                    }
                    shaped(RecipeCategory.DECORATIONS, item, 2)
                            .group("polydecorations:sign_post")
                            .pattern("ss-")
                            .define('-', Items.STICK)
                            .define('s', planks)
                            .unlockedBy("planks", InventoryChangeTrigger.TriggerInstance.hasItems(planks))
                            .save(output);
                }));
            }
        };
    }

    @Override
    public String getName() {
        return "recipes";
    }
}
