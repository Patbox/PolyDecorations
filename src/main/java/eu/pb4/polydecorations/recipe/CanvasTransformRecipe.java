package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.polydecorations.canvas.CanvasData;
import eu.pb4.polydecorations.canvas.CanvasPixels;
import eu.pb4.polydecorations.entity.CanvasEntity;
import eu.pb4.polydecorations.item.CanvasItem;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CanvasTransformRecipe extends ShapelessRecipe {
    public static MapCodec<CanvasTransformRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(CanvasTransformRecipe::group),
                        Codec.STRING.optionalFieldOf("action", "wax").forGetter(CanvasTransformRecipe::action),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CanvasTransformRecipe::category),
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

    public CanvasTransformRecipe(String group, String action, CraftingBookCategory category, ItemStack result, Ingredient source, List<Ingredient> ingredients) {
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
    public boolean matches(CraftingInput craftingRecipeInput, Level world) {
        for (var tmp : craftingRecipeInput.items()) {
            if (this.source.test(tmp)) {
                var data = tmp.getOrDefault(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT);

                if (switch (this.action) {
                    case "wax" -> data.waxed();
                    case "glow" -> data.glowing();
                    case "unglow" -> !data.glowing();
                    case "dye" -> {
                        var x = CanvasEntity.getColor(craftingRecipeInput.items().stream().filter(tmp2 -> tmp2.is(ConventionalItemTags.DYES)).findFirst().orElse(ItemStack.EMPTY));
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
    public ItemStack assemble(CraftingInput recipeInputInventory, HolderLookup.Provider wrapperLookup) {
        var stack = super.assemble(recipeInputInventory, wrapperLookup);
        ItemStack dye = recipeInputInventory.items().stream().filter(tmp -> tmp.is(ConventionalItemTags.DYES)).findFirst().orElse(ItemStack.EMPTY);
        for (var tmp : recipeInputInventory.items()) {
            if (this.source.test(tmp)) {
                stack.applyComponents(tmp.getComponents());
                stack.update(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT, (x) -> switch (this.action) {
                    case "wax" -> new CanvasData(x.image(), x.background(), x.glowing(), true, x.cut());
                    case "glow" -> new CanvasData(x.image(), x.background(), true, x.waxed(), x.cut());
                    case "unglow" -> new CanvasData(x.image(), x.background(), false, x.waxed(), x.cut());
                    case "dye" ->
                            new CanvasData(x.image(), CanvasEntity.getColor(dye), x.glowing(), x.waxed(), x.cut());
                    case "cut" -> {
                        byte[] image;
                        if (x.image().isPresent()) {
                            var source = x.image().get();
                            image = new byte[source.data().length];
                            for (var i = 0; i < source.data().length; i++) {
                                var c = source.data()[i];
                                if (c == 0) {
                                    c = 1;
                                }
                                image[i] = c;
                            }
                        } else {
                            image = new byte[16 * 16];
                            Arrays.fill(image, (byte) 1);
                        }
                        yield new CanvasData(Optional.ofNullable(new CanvasPixels(image)), x.background(), x.glowing(), x.waxed(), true);
                    }
                    case "uncut" -> {
                        byte[] image;
                        if (x.image().isPresent()) {
                            boolean isEmpty = true;
                            var source = x.image().get();
                            image = new byte[source.data().length];
                            for (var i = 0; i < source.data().length; i++) {
                                var c = source.data()[i];
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
                        yield new CanvasData(Optional.ofNullable(new CanvasPixels(image)), x.background(), x.glowing(), x.waxed(), false);
                    }
                    default -> x;
                });
                break;
            }
        }
        return stack;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            var stack = input.getItem(i);
            var remainer = stack.getItem().getCraftingRemainder();
            if (remainer.isEmpty() && stack.isDamageableItem()) {
                remainer = stack.copy();
                remainer.setDamageValue(remainer.getDamageValue() + 1);
                if (remainer.getDamageValue() >= remainer.getMaxDamage()) {
                    remainer = ItemStack.EMPTY;
                }
            } else if (stack.is(Items.WATER_BUCKET)) {
                remainer = stack.copy();
            }
            defaultedList.set(i, remainer);
        }

        return defaultedList;
    }

    @Override
    public List<RecipeDisplay> display() {
        var list = new ArrayList<SlotDisplay>();
        list.add(this.source.display());
        for (var ing : this.ingredientsOg) {
            list.add(ing.display());
        }
        var res = this.result.copy();

        var fakeImage = Optional.of(new CanvasPixels(new byte[0]));
        res.update(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT, (x) -> switch (this.action) {
            case "wax" -> new CanvasData(fakeImage, x.background(), x.glowing(), true, x.cut());
            case "glow" -> new CanvasData(fakeImage, x.background(), true, x.waxed(), x.cut());
            case "unglow" -> new CanvasData(fakeImage, x.background(), false, x.waxed(), x.cut());
            case "dye" -> new CanvasData(fakeImage, Optional.of(CanvasColor.OFF_WHITE_NORMAL), x.glowing(), x.waxed(), x.cut());
            case "cut" -> new CanvasData(fakeImage, x.background(), x.glowing(), x.waxed(), true);
            case "uncut" -> new CanvasData(fakeImage, x.background(), x.glowing(), x.waxed(), false);
            default -> x;
        });

        return List.of(new ShapelessCraftingRecipeDisplay(list, new SlotDisplay.ItemStackSlotDisplay(res), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeSerializer getSerializer() {
        return DecorationsRecipeSerializers.CANVAS_TRANSFORM;
    }
}
