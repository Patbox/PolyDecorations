package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polydecorations.canvas.CanvasData;
import eu.pb4.polydecorations.item.CanvasItem;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import static eu.pb4.polydecorations.ModInit.id;

public record CloneCanvasCraftingRecipe(String group, Item input) implements CraftingRecipe {
    public static final MapCodec<CloneCanvasCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(x -> x.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(CloneCanvasCraftingRecipe::group),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("input").forGetter(CloneCanvasCraftingRecipe::input)
            ).apply(x, CloneCanvasCraftingRecipe::new)
    );


    public static RecipeHolder<CloneCanvasCraftingRecipe> of(String id, Item item) {
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id(id)), new CloneCanvasCraftingRecipe("", item));
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level world) {
        boolean hasNbt = false;
        int count = 0;

        for (var stack : inventory.items()) {
            if (stack.is(this.input)) {
                if (hasNbt && stack.getOrDefault(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT).image().isPresent()) {
                    return false;
                }
                hasNbt |= stack.getOrDefault(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT).image().isPresent();
                count++;
            }
        }
        return hasNbt && count > 1;
    }

    @Override
    public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider wrapperLookup) {
        CanvasData nbt = null;
        int count = 0;
        for (var stack : inventory.items()) {
            if (stack.is(this.input)) {
                if (stack.getOrDefault(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT).image().isPresent()) {
                    nbt = stack.get(DecorationsDataComponents.CANVAS_DATA);
                }
                count++;
            }
        }
        var stack = new ItemStack(this.input, count);
        stack.set(DecorationsDataComponents.CANVAS_DATA, nbt);
        return stack;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeSerializer getSerializer() {
        return DecorationsRecipeSerializers.CANVAS_CLONE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }
}
