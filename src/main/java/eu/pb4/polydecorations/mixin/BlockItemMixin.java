package eu.pb4.polydecorations.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polydecorations.block.extension.WallAttachedLanternBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @ModifyReturnValue(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("TAIL"))
    private ActionResult swingForLanterns(ActionResult original, @Local(ordinal = 0) BlockState state) {
        return original == ActionResult.SUCCESS && state.getBlock() instanceof WallAttachedLanternBlock ? ActionResult.SUCCESS_SERVER : original;
    }

    @ModifyArg(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private Entity swingForLanterns(Entity player, @Local(ordinal = 0) BlockState state) {
        return state.getBlock() instanceof WallAttachedLanternBlock ? null : player;
    }
}
