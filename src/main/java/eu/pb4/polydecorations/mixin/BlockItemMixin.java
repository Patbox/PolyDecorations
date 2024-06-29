package eu.pb4.polydecorations.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polydecorations.block.extension.WallAttachedLanternBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @ModifyArg(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;"))
    private boolean swingForLanterns(boolean swingHand, @Local(ordinal = 0) BlockState state) {
        return swingHand || state.getBlock() instanceof WallAttachedLanternBlock;
    }

    @ModifyArg(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private PlayerEntity swingForLanterns(PlayerEntity player, @Local(ordinal = 0) BlockState state) {
        return state.getBlock() instanceof WallAttachedLanternBlock ? null : player;
    }
}
