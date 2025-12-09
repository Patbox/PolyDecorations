package eu.pb4.polydecorations.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polydecorations.item.DecorationsItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin {
    @ModifyReturnValue(method = "canPlaceItemThroughFace", at = @At("RETURN"))
    private boolean patchNesting(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && !stack.is(DecorationsItems.BASKET);
    }
}
