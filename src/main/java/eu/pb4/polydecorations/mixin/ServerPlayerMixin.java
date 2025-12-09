package eu.pb4.polydecorations.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polydecorations.block.DecorationsBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @WrapWithCondition(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V"))
    private boolean dontSetSpawnForSleepingBag(ServerPlayer instance, ServerPlayer.RespawnConfig respawn, boolean sendMessage, @Local(argsOnly = true) BlockPos blockPos) {
        return !instance.level().getBlockState(blockPos).is(DecorationsBlockTags.SLEEPING_BAGS);
    }
}
