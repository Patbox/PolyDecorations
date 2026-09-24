package eu.pb4.polydecorations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polydecorations.DecorationsFeature;
import eu.pb4.polydecorations.entity.DecorationsEntities;
import eu.pb4.polydecorations.entity.FirstLeashFenceKnotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(LeadItem.class)
public class LeadItemMixin {
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/LeadItem;bindPlayerMobs(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult handleLeadToFence(Player player, Level level, BlockPos blockPos, Operation<InteractionResult> original,
                                                @Local(argsOnly = true) UseOnContext useOnContext) {
        List<Leashable> list = Leashable.leashableInArea(level, Vec3.atCenterOf(blockPos), (leashablex) -> leashablex.getLeashHolder() == player);
        var state = level.getBlockState(blockPos);

        if (!DecorationsFeature.FENCE_LEADS.isEnabled() || !list.isEmpty() || !state.is(BlockTags.FENCES)) {
            return original.call(player, level, blockPos);
        }

        var knot = new FirstLeashFenceKnotEntity(DecorationsEntities.FIRST_LEASH_FENCE_KNOT, level);
        knot.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        level.addFreshEntity(knot);
        knot.setLeashedTo(player, true);
        useOnContext.getItemInHand().consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
