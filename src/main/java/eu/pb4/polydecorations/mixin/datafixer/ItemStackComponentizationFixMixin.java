package eu.pb4.polydecorations.mixin.datafixer;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
    @Inject(method = "fixStack", at = @At("TAIL"))
    private static void fixCustomStacks(ItemStackComponentizationFix.StackData data, Dynamic dynamic, CallbackInfo ci) {
        if (data.itemEquals("polydecorations:canvas") ) {
            data.setComponent("polydecorations:canvas_data", dynamic.emptyMap()
                    .setFieldIfPresent("image", data.getAndRemove("data").result())
                    .setFieldIfPresent("glowing", data.getAndRemove("glowing").result())
                    .setFieldIfPresent("waxed", data.getAndRemove("waxed").result())
            );
        }
    }
}
