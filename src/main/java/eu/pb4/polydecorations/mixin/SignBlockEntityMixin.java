package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.block.extension.SignPostBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {
    @Inject(method = "markUpdated", at = @At("HEAD"), cancellable = true)
    private void cancelForFake(CallbackInfo ci) {
        if (((Object) this) instanceof SignPostBlockEntity.Fake) {
            ci.cancel();
        }
    }
}
