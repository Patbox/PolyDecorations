package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.block.DecorationsBlockTags;
import eu.pb4.polydecorations.block.furniture.SleepingBagBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Unique
    private BlockPos realSleepPos = null;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "setSleepingPosition", at = @At("HEAD"), argsOnly = true)
    private BlockPos hackBlockPos(BlockPos value) {
        var state = this.getWorld().getBlockState(value);
        if (!(state.getBlock() instanceof SleepingBagBlock)) {
            this.realSleepPos = null;
            return value;
        }

        realSleepPos = value;
        var fakePos = value.withY(value.getY() > 0 ? this.getWorld().getBottomY() : this.getWorld().getTopYInclusive() - 1);

        for (var player : ((ServerWorld) this.getWorld()).getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(this.getChunkPos())) {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(fakePos, Blocks.BLACK_BED.getDefaultState().with(BedBlock.FACING, state.get(BedBlock.FACING))));
        }

        return fakePos;
    }

    @Inject(method = "getSleepingPosition", at = @At("HEAD"), cancellable = true)
    private void provideRealSleepPos(CallbackInfoReturnable<Optional<BlockPos>> cir) {
        if (realSleepPos != null) {
            cir.setReturnValue(Optional.of(realSleepPos));
        }
    }

    @Inject(method = "clearSleepingPosition", at = @At("HEAD"))
    private void clearFakeSleepPos(CallbackInfo ci) {
        if (realSleepPos == null) {
            return;
        }

        var fakePos = realSleepPos.withY(realSleepPos.getY() > 0 ? this.getWorld().getBottomY() : this.getWorld().getTopYInclusive() - 1);

        for (var player : ((ServerWorld) this.getWorld()).getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(this.getChunkPos())) {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.getWorld(), fakePos));
        }
        realSleepPos = null;
    }
}
