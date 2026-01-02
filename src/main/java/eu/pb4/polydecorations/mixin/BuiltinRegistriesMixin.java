package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.util.ModdedCompat;
import eu.pb4.polydecorations.util.WoodUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltinRegistriesMixin {
    @Inject(method = "freeze", at = @At("HEAD"))
    private static void searchForModdedWood(CallbackInfo ci) {
        ModdedCompat.detect();
    }
}
