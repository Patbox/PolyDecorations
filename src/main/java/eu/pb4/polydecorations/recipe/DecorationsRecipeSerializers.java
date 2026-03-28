package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.MapCodec;
import eu.pb4.polydecorations.ModInit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DecorationsRecipeSerializers {
    public static final RecipeSerializer<CanvasTransformRecipe> CANVAS_TRANSFORM = register("crafting/canvas_transform", CanvasTransformRecipe.CODEC);
    public static final RecipeSerializer<CloneCanvasCraftingRecipe> CANVAS_CLONE = register("crafting/canvas_cloning", CloneCanvasCraftingRecipe.CODEC);
    public static final RecipeSerializer<ComponentApplyCraftingRecipe> COMPONENT_APPLY = register("crafting/components_apply", ComponentApplyCraftingRecipe.CODEC);
    public static final RecipeSerializer<ColorWindChimeRecipe> WIND_CHIME_COLORING = register("crafting/wind_chime_coloring", ColorWindChimeRecipe.CODEC);

    public static void register() {

    }

    public static <T extends RecipeSerializer<?>> T register(String path, T recipeSerializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(ModInit.ID, path), recipeSerializer);
    }

    public static <T extends Recipe<?>> RecipeSerializer<T> register(String path, MapCodec<T> codec) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(ModInit.ID, path), new RecipeSerializer<>(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec())));
    }
}
