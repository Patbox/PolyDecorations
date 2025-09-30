package eu.pb4.polydecorations.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polydecorations.block.DecorationsBlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @WrapWithCondition(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/server/network/ServerPlayerEntity$Respawn;Z)V"))
    private boolean dontSetSpawnForSleepingBag(ServerPlayerEntity instance, ServerPlayerEntity.Respawn respawn, boolean sendMessage, @Local(argsOnly = true) BlockPos blockPos) {
        return !instance.getEntityWorld().getBlockState(blockPos).isIn(DecorationsBlockTags.SLEEPING_BAGS);
    }
}
