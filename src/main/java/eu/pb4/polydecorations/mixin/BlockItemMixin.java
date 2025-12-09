package eu.pb4.polydecorations.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polydecorations.block.extension.WallAttachedLanternBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @ModifyReturnValue(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("TAIL"))
    private InteractionResult swingForLanterns(InteractionResult original, @Local(ordinal = 0) BlockState state) {
        return original == InteractionResult.SUCCESS && state.getBlock() instanceof WallAttachedLanternBlock ? InteractionResult.SUCCESS_SERVER : original;
    }

    @ModifyArg(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private Entity swingForLanterns(Entity player, @Local(ordinal = 0) BlockState state) {
        return state.getBlock() instanceof WallAttachedLanternBlock ? null : player;
    }
}
