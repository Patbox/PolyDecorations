package eu.pb4.polydecorations.mixin.datafixer;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.schema.Schema100;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Schema100.class)
public interface Schema100Accessor {
    @Invoker
    static TypeTemplate callTargetItems(Schema schema) {
        throw new UnsupportedOperationException();
    }
}
