package eu.pb4.polydecorations.mixin.datafixer;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void fixCustomStacks(ItemStackComponentizationFix.ItemStackData data, Dynamic dynamic, CallbackInfo ci) {
        if (data.is("polydecorations:canvas") ) {
            data.setComponent("polydecorations:canvas_data", dynamic.emptyMap()
                    .setFieldIfPresent("image", data.removeTag("data").result())
                    .setFieldIfPresent("glowing", data.removeTag("glowing").result())
                    .setFieldIfPresent("waxed", data.removeTag("waxed").result())
            );
        }
    }
}
