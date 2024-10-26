package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.polydecorations.entity.CanvasEntity;
import eu.pb4.polydecorations.item.CanvasItem;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CanvasTransformRecipe extends ShapelessRecipe {
    public static MapCodec<CanvasTransformRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(CanvasTransformRecipe::getGroup),
                        Codec.STRING.optionalFieldOf("action", "wax").forGetter(CanvasTransformRecipe::action),
                        CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(CanvasTransformRecipe::getCategory),
                        ItemStack.CODEC.fieldOf("result").forGetter(t -> t.result),
                        Ingredient.CODEC.fieldOf("source").forGetter(t -> t.source),
                        Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap((ingredients) -> {
                            return ingredients.size() > 8 ? DataResult.error(() -> {
                                return "Too many ingredients for shapeless recipe";
                            }) : DataResult.success(ingredients);
                        }, DataResult::success).forGetter(t -> t.ingredientsOg))
                .apply(instance, CanvasTransformRecipe::new);
    });
    private final ItemStack result;
    private final String action;
    private final Ingredient source;
    private final List<Ingredient> ingredientsOg;

    public CanvasTransformRecipe(String group, String action, CraftingRecipeCategory category, ItemStack result, Ingredient source, List<Ingredient> ingredients) {
        super(group, category, result, merge(ingredients, source));
        this.ingredientsOg = ingredients;
        this.action = action;
        this.result = result;
        this.source = source;
    }

    private static List<Ingredient> merge(List<Ingredient> ingredients, Ingredient source) {
        var list = new ArrayList<Ingredient>();
        list.add(source);
        list.addAll(ingredients);
        return list;
    }

    private String action() {
        return this.action;
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        for (var tmp : craftingRecipeInput.getStacks()) {
            if (this.source.test(tmp)) {
                var data = tmp.getOrDefault(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT);

                if (switch (this.action) {
                    case "wax" -> data.waxed();
                    case "glow" -> data.glowing();
                    case "unglow" -> !data.glowing();
                    case "dye" -> {
                        var x = CanvasEntity.getColor(craftingRecipeInput.getStacks().stream().filter(tmp2 -> tmp2.isIn(ConventionalItemTags.DYES)).findFirst().orElse(ItemStack.EMPTY));
                        yield x.isEmpty() && data.background().isEmpty() || x.isPresent() && data.background().orElse(null) == x.get();
                    }
                    case "cut" -> data.cut();
                    case "uncut" -> !data.cut();
                    default -> false;
                }) {
                    return false;
                }
            }
        }

        return super.matches(craftingRecipeInput, world);
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
                    case "glow" -> new CanvasItem.Data(x.image(), x.background(), true, x.waxed(), x.cut());
                    case "unglow" -> new CanvasItem.Data(x.image(), x.background(), false, x.waxed(), x.cut());
                    case "dye" ->
                            new CanvasItem.Data(x.image(), CanvasEntity.getColor(dye), x.glowing(), x.waxed(), x.cut());
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
                    case "uncut" -> {
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
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
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
    public List<RecipeDisplay> getDisplays() {
        var list = new ArrayList<SlotDisplay>();
        list.add(this.source.toDisplay());
        for (var ing : this.ingredientsOg) {
            list.add(ing.toDisplay());
        }
        var res = this.result.copy();

        var fakeImage = Optional.of(new byte[0]);
        res.apply(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT, (x) -> switch (this.action) {
            case "wax" -> new CanvasItem.Data(fakeImage, x.background(), x.glowing(), true, x.cut());
            case "glow" -> new CanvasItem.Data(fakeImage, x.background(), true, x.waxed(), x.cut());
            case "unglow" -> new CanvasItem.Data(fakeImage, x.background(), false, x.waxed(), x.cut());
            case "dye" -> new CanvasItem.Data(fakeImage, Optional.of(CanvasColor.OFF_WHITE_NORMAL), x.glowing(), x.waxed(), x.cut());
            case "cut" -> new CanvasItem.Data(fakeImage, x.background(), x.glowing(), x.waxed(), true);
            case "uncut" -> new CanvasItem.Data(fakeImage, x.background(), x.glowing(), x.waxed(), false);
            default -> x;
        });

        return List.of(new ShapelessCraftingRecipeDisplay(list, new SlotDisplay.StackSlotDisplay(res), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeSerializer getSerializer() {
        return DecorationsRecipeSerializers.CANVAS_TRANSFORM;
    }
}
