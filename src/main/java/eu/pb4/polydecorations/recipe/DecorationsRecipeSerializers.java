package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.recipe.LazyRecipeSerializer;
import eu.pb4.polydecorations.ModInit;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DecorationsRecipeSerializers {
    public static final LazyRecipeSerializer<CanvasTransformRecipe> CANVAS_TRANSFORM = register("crafting/canvas_transform", CanvasTransformRecipe.CODEC);
    public static final LazyRecipeSerializer<CloneCanvasCraftingRecipe> CANVAS_CLONE = register("crafting/canvas_cloning", CloneCanvasCraftingRecipe.CODEC);
    public static final LazyRecipeSerializer<ColorWindChimeRecipe> WIND_CHIME_COLORING = register("crafting/wind_chime_coloring", ColorWindChimeRecipe.CODEC);

    public static void register() {

    }

    public static <T extends RecipeSerializer<?>> T register(String path, T recipeSerializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(ModInit.ID, path), recipeSerializer);
    }

    public static <T extends Recipe<?>> LazyRecipeSerializer<T> register(String path, MapCodec<T> codec) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(ModInit.ID, path), new LazyRecipeSerializer<>(codec));
    }
}
