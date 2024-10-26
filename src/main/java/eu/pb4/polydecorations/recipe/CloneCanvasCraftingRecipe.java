package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polydecorations.item.CanvasItem;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import static eu.pb4.polydecorations.ModInit.id;

public record CloneCanvasCraftingRecipe(String group, Item input) implements CraftingRecipe {
    public static final MapCodec<CloneCanvasCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(x -> x.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(CloneCanvasCraftingRecipe::group),
                    Registries.ITEM.getCodec().fieldOf("input").forGetter(CloneCanvasCraftingRecipe::input)
            ).apply(x, CloneCanvasCraftingRecipe::new)
    );


    public static RecipeEntry<CloneCanvasCraftingRecipe> of(String id, Item item) {
        return new RecipeEntry<>(RegistryKey.of(RegistryKeys.RECIPE, id(id)), new CloneCanvasCraftingRecipe("", item));
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        boolean hasNbt = false;
        int count = 0;

        for (var stack : inventory.getStacks()) {
            if (stack.isOf(this.input)) {
                if (hasNbt && stack.getOrDefault(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT).image().isPresent()) {
                    return false;
                }
                hasNbt |= stack.getOrDefault(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT).image().isPresent();
                count++;
            }
        }
        return hasNbt && count > 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup wrapperLookup) {
        CanvasItem.Data nbt = null;
        int count = 0;
        for (var stack : inventory.getStacks()) {
            if (stack.isOf(this.input)) {
                if (stack.getOrDefault(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT).image().isPresent()) {
                    nbt = stack.get(CanvasItem.DATA_TYPE);
                }
                count++;
            }
        }
        var stack = new ItemStack(this.input, count);
        stack.set(CanvasItem.DATA_TYPE, nbt);
        return stack;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeSerializer getSerializer() {
        return DecorationsRecipeSerializers.CANVAS_CLONE;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }
}
