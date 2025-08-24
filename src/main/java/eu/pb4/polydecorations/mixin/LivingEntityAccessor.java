package eu.pb4.polydecorations.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    static TrackedData<Optional<BlockPos>> getSLEEPING_POSITION() {
        throw new UnsupportedOperationException();
    }
}
