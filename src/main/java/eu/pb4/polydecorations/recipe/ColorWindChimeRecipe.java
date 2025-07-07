package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.polydecorations.entity.CanvasEntity;
import eu.pb4.polydecorations.item.CanvasItem;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.item.WindChimeItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.MapColor;
import net.minecraft.component.ComponentType;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ColorWindChimeRecipe extends SpecialCraftingRecipe {
    public static final MapCodec<ColorWindChimeRecipe> CODEC = CraftingRecipeCategory.CODEC.fieldOf("category")
            .xmap(ColorWindChimeRecipe::new, ColorWindChimeRecipe::getCategory);
    private static Optional<ComponentType<Integer>> POLYFACTORY_COLOR;

    public ColorWindChimeRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        var hasWindChime = false;
        var hasDye = 0;

        for (var stack : craftingRecipeInput.getStacks()) {
            if (stack.isOf(DecorationsItems.WIND_CHIME)) {
                if (hasWindChime) return false;
                hasWindChime = true;
            } else if (stack.isIn(ConventionalItemTags.DYES) && getColor(stack) != -1) {
                hasDye++;
            }
        }

        return hasWindChime && hasDye > 0 && hasDye <= 5;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput recipeInputInventory, RegistryWrapper.WrapperLookup wrapperLookup) {
        var colors = new IntArrayList();
        for (var stack : recipeInputInventory.getStacks()) {
            if (stack.isIn(ConventionalItemTags.DYES)) {
                colors.add(getColor(stack));
            }
        }
        var stack = DecorationsItems.WIND_CHIME.getDefaultStack();
        stack.set(WindChimeItem.WIND_CHIME_COLOR, colors);
        return stack;
    }

    public static int getColor(ItemStack stack) {
        //noinspection OptionalAssignedToNull
        if (POLYFACTORY_COLOR == null) {
            //noinspection unchecked
            POLYFACTORY_COLOR = Optional.ofNullable((ComponentType<Integer>) Registries.DATA_COMPONENT_TYPE.get(Identifier.of("polyfactory:color")));
        }
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return dyeItem.getColor().getEntityColor();
        } else if (POLYFACTORY_COLOR.isPresent() && stack.contains(POLYFACTORY_COLOR.get())) {
            //noinspection DataFlowIssue
            return stack.get(POLYFACTORY_COLOR.get());
        }
        return -1;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeSerializer getSerializer() {
        return DecorationsRecipeSerializers.WIND_CHIME_COLORING;
    }
}
