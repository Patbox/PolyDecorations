package eu.pb4.polydecorations.recipe;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class DecorationsRecipeTypes {


    public static void register() {

    }

    public static <T extends Recipe<?>> RecipeType<T> register(String path) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(ModInit.ID, path), new RecipeType<T>() {
            @Override
            public String toString() {
                return ModInit.ID + ":" + path;
            }
        });
    }
}
