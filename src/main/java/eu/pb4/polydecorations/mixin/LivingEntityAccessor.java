package eu.pb4.polydecorations.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    static EntityDataAccessor<Optional<BlockPos>> getSLEEPING_POS_ID() {
        throw new UnsupportedOperationException();
    }
}
