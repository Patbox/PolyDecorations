package eu.pb4.polydecorations.ui;

import eu.pb4.polydecorations.polydex.PolydexTextures;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

import static eu.pb4.polydecorations.ui.UiResourceCreator.*;

public class GuiTextures {
    public static final GuiElement EMPTY = icon16("empty").get().build();
    public static final Function<Component, Component> SHELF = background("shelf");
    public static final Function<Component, Component> TRASHCAN = background("trashcan");
    public static final Function<Component, Component> SHELF_2 = background("shelf_2");
    public static final Function<Component, Component> MAILBOX_SELECT = background("mailbox_select");

    public static final Supplier<GuiElementBuilder> POLYDEX_BUTTON = icon32("polydex");
    public static final Supplier<GuiElementBuilder> NEXT_PAGE_BUTTON = icon16("next_page");
    public static final Supplier<GuiElementBuilder> PREVIOUS_PAGE_BUTTON = icon16("previous_page");
    public static final Supplier<GuiElementBuilder> BACK_BUTTON = icon16("back");

    public static void register() {
        PolydexTextures.register();
    }


    public record Progress(GuiElement[] elements) {

        public GuiElement get(float progress) {
            return elements[Math.min((int) (progress * elements.length), elements.length - 1)];
        }

        public static Progress createVertical(String path, int start, int stop, boolean reverse) {
            var size = stop - start;
            var elements = new GuiElement[size + 1];
            var function = verticalProgress16(path, start, stop, reverse);

            elements[0] = EMPTY;

            for (var i = 1; i <= size; i++) {
                elements[i] = function.apply(i - 1).build();
            }
            return new Progress(elements);
        }

        public static Progress createHorizontal(String path, int start, int stop, boolean reverse) {
            var size = stop - start;
            var elements = new GuiElement[size + 1];
            var function = horizontalProgress16(path, start, stop, reverse);

            elements[0] = EMPTY;

            for (var i = 1; i <= size; i++) {
                elements[i] = function.apply(i - 1).build();
            }
            return new Progress(elements);
        }

        public static Progress createHorizontal32(String path, int start, int stop, boolean reverse) {
            var size = stop - start;
            var elements = new GuiElement[size + 1];
            var function = horizontalProgress32(path, start, stop, reverse);

            elements[0] = EMPTY;

            for (var i = 1; i <= size; i++) {
                elements[i] = function.apply(i - 1).build();
            }
            return new Progress(elements);
        }

        public static Progress createHorizontal32Right(String path, int start, int stop, boolean reverse) {
            var size = stop - start;
            var elements = new GuiElement[size + 1];
            var function = horizontalProgress32Right(path, start, stop, reverse);

            elements[0] = EMPTY;

            for (var i = 1; i <= size; i++) {
                elements[i] = function.apply(i - 1).build();
            }
            return new Progress(elements);
        }
        public static Progress createVertical32Right(String path, int start, int stop, boolean reverse) {
            var size = stop - start;
            var elements = new GuiElement[size + 1];
            var function = verticalProgress32Right(path, start, stop, reverse);

            elements[0] = EMPTY;

            for (var i = 1; i <= size; i++) {
                elements[i] = function.apply(i - 1).build();
            }
            return new Progress(elements);
        }
    }

}
