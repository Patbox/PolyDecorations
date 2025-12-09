package eu.pb4.polydecorations.polydex;

import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.block.other.GenericSingleItemBlockEntity;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydex.api.v1.hover.HoverDisplayBuilder;
import eu.pb4.polydex.api.v1.recipe.*;

import eu.pb4.factorytools.api.recipe.CountedIngredient;
import eu.pb4.factorytools.api.recipe.OutputStack;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.ui.GuiUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;

public class PolydexCompatImpl {
    public static void register() {
        HoverDisplayBuilder.register(DecorationsBlocks.DISPLAY_CASE, PolydexCompatImpl::replaceName);
    }

    private static void replaceName(HoverDisplayBuilder builder) {
        if (builder.getTarget().blockEntity() instanceof GenericSingleItemBlockEntity be && !be.isEmpty()) {
            builder.setComponent(HoverDisplayBuilder.NAME, Component.empty()
                    .append(be.getTheItem().getHoverName())
                    .append(Component.literal(" (")
                            .append(builder.getComponent(HoverDisplayBuilder.NAME))
                            .append(")").withStyle(ChatFormatting.GRAY))
            );
        }
    }

    public static GuiElement getButton(RecipeType<?> type) {
        var category = PolydexCategory.of(type);
        return GuiTextures.POLYDEX_BUTTON.get()
                .setName(Component.translatable("text.polyfactory.recipes"))
                .setCallback((index, type1, action, gui) -> {
                    PolydexPageUtils.openCategoryUi(gui.getPlayer(), category, gui::open);
                    GuiUtils.playClickSound(gui.getPlayer());
                }).build();
    }

    public static List<PolydexIngredient<?>> createIngredients(CountedIngredient... input) {
        return createIngredients(List.of(input));
    }
    public static List<PolydexIngredient<?>> createIngredients(List<CountedIngredient> input) {
        var list = new ArrayList<PolydexIngredient<?>>(input.size());
        for (var x : input) {
            list.add(PolydexIngredient.of(x.ingredient().orElse(null), Math.max(x.count(), 1)));
        }
        return list;
    }

    public static PolydexStack<?>[] createOutput(List<OutputStack> output) {
        var list = new ArrayList<PolydexStack<?>>(output.size());
        for (var x : output) {
            list.add(PolydexStack.of(x.stack().copyWithCount(x.stack().getCount() * x.roll()), x.chance()));
        }
        return list.toArray(new PolydexStack[0]);
    }
}
