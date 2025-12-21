package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.polydecorations.entity.CanvasEntity;
import eu.pb4.polydecorations.item.CanvasItem;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.item.WindChimeItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ColorWindChimeRecipe extends CustomRecipe {
    public static final MapCodec<ColorWindChimeRecipe> CODEC = CraftingBookCategory.CODEC.fieldOf("category")
            .xmap(ColorWindChimeRecipe::new, ColorWindChimeRecipe::category);
    private static Optional<DataComponentType<Integer>> POLYFACTORY_COLOR;

    public ColorWindChimeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingRecipeInput, Level world) {
        var hasWindChime = false;
        var hasDye = 0;

        for (var stack : craftingRecipeInput.items()) {
            if (stack.is(DecorationsItems.WIND_CHIME)) {
                if (hasWindChime) return false;
                hasWindChime = true;
            } else if (stack.is(ConventionalItemTags.DYES) && getColor(stack) != -1) {
                hasDye++;
            }
        }

        return hasWindChime && hasDye > 0 && hasDye <= 5;
    }

    @Override
    public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider wrapperLookup) {
        var colors = new IntArrayList();
        for (var stack : inventory.items()) {
            if (stack.is(ConventionalItemTags.DYES)) {
                colors.add(getColor(stack));
            }
        }
        var stack = DecorationsItems.WIND_CHIME.getDefaultInstance();
        stack.set(DecorationsDataComponents.WIND_CHIME_COLOR, colors);
        return stack;
    }

    public static int getColor(ItemStack stack) {
        //noinspection OptionalAssignedToNull
        if (POLYFACTORY_COLOR == null) {
            //noinspection unchecked
            POLYFACTORY_COLOR = Optional.ofNullable((DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.parse("polyfactory:color")));
        }
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return dyeItem.getDyeColor().getTextureDiffuseColor();
        } else if (POLYFACTORY_COLOR.isPresent() && stack.has(POLYFACTORY_COLOR.get())) {
            //noinspection DataFlowIssue
            return stack.get(POLYFACTORY_COLOR.get());
        }
        return -1;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeSerializer getSerializer() {
        return DecorationsRecipeSerializers.WIND_CHIME_COLORING;
    }
}
