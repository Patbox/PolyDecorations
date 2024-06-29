package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.entity.CanvasEntity;
import eu.pb4.polydecorations.item.CanvasItem;
import eu.pb4.polymer.core.api.item.PolymerRecipe;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class CanvasTransformRecipe extends ShapelessRecipe implements PolymerRecipe {
    private final ItemStack result;
    private final String action;
    private final Ingredient source;
    private final DefaultedList<Ingredient> ingredientsOg;
    public static MapCodec<CanvasTransformRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                        Codec.STRING.optionalFieldOf( "group", "").forGetter(CanvasTransformRecipe::getGroup),
                        Codec.STRING.optionalFieldOf( "action", "wax").forGetter(CanvasTransformRecipe::action),
                        CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(CanvasTransformRecipe::getCategory),
                        ItemStack.CODEC.fieldOf("result").forGetter(t -> t.result),
                        Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("source").forGetter(t -> t.source),
                        Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients").flatXmap((ingredients) -> {
                            Ingredient[] ingredients2 = (Ingredient[]) ingredients.stream().filter((ingredient) -> {
                                return !ingredient.isEmpty();
                            }).toArray((i) -> {
                                return new Ingredient[i];
                            });

                            return ingredients2.length > 8 ? DataResult.error(() -> {
                                return "Too many ingredients for shapeless recipe";
                            }) : DataResult.success(DefaultedList.copyOf(Ingredient.EMPTY, ingredients2));
                        }, DataResult::success).forGetter(t -> t.ingredientsOg))
                .apply(instance, CanvasTransformRecipe::new);
    });

    private String action() {
        return this.action;
    }

    private final Recipe<?> vanilla;

    public CanvasTransformRecipe(String group, String action, CraftingRecipeCategory category, ItemStack result, Ingredient source, DefaultedList<Ingredient> ingredients) {
        super(group, category, result, merge(ingredients, source));
        this.ingredientsOg = ingredients;
        this.action = action;
        this.result = result;
        this.source = source;

        this.vanilla = new ShapelessRecipe(group, category, result, this.getIngredients());
    }

    private static DefaultedList<Ingredient> merge(DefaultedList<Ingredient> ingredients, Ingredient source) {
        var list = DefaultedList.<Ingredient>of();
        list.add(source);
        list.addAll(ingredients);
        return list;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput recipeInputInventory, RegistryWrapper.WrapperLookup wrapperLookup) {
        var stack = super.craft(recipeInputInventory, wrapperLookup);
        ItemStack dye = recipeInputInventory.getStacks().stream().filter(tmp -> tmp.isIn(ConventionalItemTags.DYES)).findFirst().orElse(ItemStack.EMPTY);
        for (var tmp : recipeInputInventory.getStacks()) {
            if (this.source.test(tmp)) {
                stack.applyComponentsFrom(tmp.getComponents());
                stack.apply(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT, (x) -> switch (this.action) {
                    case "wax" -> new CanvasItem.Data(x.image(), x.background(), x.glowing(), true, x.cut());
                    case "glow" -> new CanvasItem.Data(x.image(), x.background(),true, x.waxed(), x.cut());
                    case "unglow" -> new CanvasItem.Data(x.image(), x.background(),false, x.waxed(), x.cut());
                    case "dye" -> new CanvasItem.Data(x.image(), CanvasEntity.getColor(dye),x.glowing(), x.waxed(), x.cut());
                    case "cut" -> {
                        byte[] image;
                        if (x.image().isPresent()) {
                            var source = x.image().get();
                            image = new byte[source.length];
                            for (var i = 0; i < source.length; i++) {
                                var c = source[i];
                                if (c == 0) {
                                    c = 1;
                                }
                                image[i] = c;
                            }
                        } else {
                            image = new byte[16 * 16];
                            Arrays.fill(image, (byte) 1);
                        }
                        yield new CanvasItem.Data(Optional.ofNullable(image), x.background(), x.glowing(), x.waxed(), true);
                    }
                    case "uncut" ->{
                        byte[] image;
                        if (x.image().isPresent()) {
                            boolean isEmpty = true;
                            var source = x.image().get();
                            image = new byte[source.length];
                            for (var i = 0; i < source.length; i++) {
                                var c = source[i];
                                if (c == 1) {
                                    c = 0;
                                } else if (c != 0) {
                                    isEmpty = false;
                                }
                                image[i] = c;
                            }

                            if (isEmpty) {
                                image = null;
                            }
                        } else {
                            image = null;
                        }
                        yield new CanvasItem.Data(Optional.ofNullable(image), x.background(), x.glowing(), x.waxed(), false);
                    }
                    default -> x;
                });
                break;
            }
        }
        return stack;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput input) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);

        for(int i = 0; i < defaultedList.size(); ++i) {
            var stack = input.getStackInSlot(i);
            var remainer = stack.getRecipeRemainder();
            if (remainer.isEmpty() && stack.isDamageable()) {
                remainer = stack.copy();
                remainer.setDamage(remainer.getDamage() + 1);
                if (remainer.getDamage() >= remainer.getMaxDamage()) {
                    remainer = ItemStack.EMPTY;
                }
            } else if (stack.isOf(Items.WATER_BUCKET)) {
                remainer = stack.copy();
            }
            defaultedList.set(i, remainer);
        }

        return defaultedList;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DecorationsRecipeSerializers.CANVAS_TRANSFORM;
    }

    @Override
    public @Nullable Recipe<?> getPolymerReplacement(ServerPlayerEntity player) {
        return this.vanilla;
    }
}
