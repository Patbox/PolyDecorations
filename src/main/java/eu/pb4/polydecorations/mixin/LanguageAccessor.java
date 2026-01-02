package eu.pb4.polydecorations.mixin;

import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.locale.Language.class)
public interface LanguageAccessor {
    @Invoker
    static Language callLoadDefault() {
        throw new UnsupportedOperationException();
    }
}
