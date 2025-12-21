package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polydecorations.canvas.CanvasData;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static eu.pb4.polydecorations.ModInit.id;

public record ComponentApplyCraftingRecipe(String group, CraftingBookCategory category, Ingredient input, List<Ingredient> extra, DataComponentPatch componentPatch) implements CraftingRecipe {
    public static final MapCodec<ComponentApplyCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(x -> x.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(ComponentApplyCraftingRecipe::group),
                    CraftingBookCategory.CODEC.fieldOf("category").forGetter(ComponentApplyCraftingRecipe::category),
                    Ingredient.CODEC.fieldOf("input").forGetter(ComponentApplyCraftingRecipe::input),
                    ExtraCodecs.compactListCodec(Ingredient.CODEC).optionalFieldOf("extra", List.of()).forGetter(ComponentApplyCraftingRecipe::extra),
                DataComponentPatch.CODEC.fieldOf("components").forGetter(ComponentApplyCraftingRecipe::componentPatch)
            ).apply(x, ComponentApplyCraftingRecipe::new)
    );

    @Override
    public boolean matches(CraftingInput inventory, Level world) {
        var ing = new ArrayList<>(extra);
        ing.add(input);
        ItemStack input = null;
        for (var stack : inventory.items()) {
            for (int a = 0; a < ing.size(); a++) {
                var ingr = ing.get(a);
                if (ingr.test(stack)) {
                    if (ingr == this.input) {
                        input = stack;
                    }
                    ing.remove(a);
                    break;
                }
            }
        }
        if (!ing.isEmpty() || input == null) {
            return false;
        }

        for (var comp : this.componentPatch.entrySet()) {
            if (!Objects.equals(input.get(comp.getKey()), comp.getValue().orElse(null))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider wrapperLookup) {
        for (var stack : inventory.items()) {
            if (this.input.test(stack)) {
                stack = stack.copyWithCount(1);
                stack.applyComponents(this.componentPatch);
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public RecipeSerializer getSerializer() {
        return DecorationsRecipeSerializers.COMPONENT_APPLY;
    }

    @Override
    public PlacementInfo placementInfo() {
        var ing = new ArrayList<>(extra);
        ing.add(input);
        return PlacementInfo.create(ing);
    }

    @Override
    public List<RecipeDisplay> display() {
        var ing = new ArrayList<>(extra);
        ing.add(input);
        return List.of(new ShapelessCraftingRecipeDisplay(
                ing.stream().map(Ingredient::display).toList(),
                new SlotDisplay.Composite(this.input().items().map(x -> {
                    var y = x.value().getDefaultInstance();
                    y.applyComponents(this.componentPatch);
                    return (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(y);
                }).toList()), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }
}
