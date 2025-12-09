package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.block.DecorationsBlockTags;
import eu.pb4.polydecorations.block.furniture.SleepingBagBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Unique
    private BlockPos realSleepPos = null;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyVariable(method = "setSleepingPos", at = @At("HEAD"), argsOnly = true)
    private BlockPos hackBlockPos(BlockPos value) {
        var state = this.level().getBlockState(value);
        if (!(state.getBlock() instanceof SleepingBagBlock)) {
            this.realSleepPos = null;
            return value;
        }

        realSleepPos = value;
        var fakePos = value.atY(value.getY() > 0 ? this.level().getMinY() : this.level().getMaxY() - 1);

        for (var player : ((ServerLevel) this.level()).getChunkSource().chunkMap.getPlayersCloseForSpawning(this.chunkPosition())) {
            player.connection.send(new ClientboundBlockUpdatePacket(fakePos, Blocks.BLACK_BED.defaultBlockState().setValue(BedBlock.FACING, state.getValue(BedBlock.FACING))));
        }

        return fakePos;
    }

    @Inject(method = "getSleepingPos", at = @At("HEAD"), cancellable = true)
    private void provideRealSleepPos(CallbackInfoReturnable<Optional<BlockPos>> cir) {
        if (realSleepPos != null) {
            cir.setReturnValue(Optional.of(realSleepPos));
        }
    }

    @Inject(method = "clearSleepingPos", at = @At("HEAD"))
    private void clearFakeSleepPos(CallbackInfo ci) {
        if (realSleepPos == null) {
            return;
        }

        var fakePos = realSleepPos.atY(realSleepPos.getY() > 0 ? this.level().getMinY() : this.level().getMaxY() - 1);

        for (var player : ((ServerLevel) this.level()).getChunkSource().chunkMap.getPlayersCloseForSpawning(this.chunkPosition())) {
            player.connection.send(new ClientboundBlockUpdatePacket(this.level(), fakePos));
        }
        realSleepPos = null;
    }
}
