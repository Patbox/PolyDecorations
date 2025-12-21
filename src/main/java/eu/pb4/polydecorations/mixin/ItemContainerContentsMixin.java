package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.item.DecorationsDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemContainerContents.class)
public class ItemContainerContentsMixin {
    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private void cancelIfTied(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter, CallbackInfo ci) {
        if (dataComponentGetter.get(DecorationsDataComponents.TIED) != null) {
            consumer.accept(Component.translatable("text.polydecorations.tied").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            ci.cancel();
        }
    }
}
