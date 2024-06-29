package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.ModeledItem;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import java.util.ArrayList;

public class TrowelItem extends ModeledItem {
    public TrowelItem(Settings settings) {
        super(settings);
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() == null) {
            return ActionResult.FAIL;
        }
        if (context.getPlayer().getStackInHand(Hand.OFF_HAND).getItem() instanceof BlockItem) {
            return ActionResult.SUCCESS;
        }

        var stacks = new ArrayList<ItemStack>(9);
        for (var i = 0; i < 9; i++) {
            var x = context.getPlayer().getInventory().getStack(i);
            if (x.getItem() instanceof BlockItem) {
                stacks.add(x);
            }
        }
        if (stacks.isEmpty()) {
            return ActionResult.FAIL;
        }
        var stack = stacks.get(context.getPlayer().getRandom().nextInt(stacks.size()));

        var blockItem = ((BlockItem) stack.getItem());
        var ctx = new ItemPlacementContext(context.getPlayer(), context.getHand(), stack,
                new BlockHitResult(context.getHitPos(), context.getSide(), context.getBlockPos(), context.hitsInsideBlock()));

        var x = blockItem.place(ctx);

        if (x.isAccepted() && ctx.getPlayer() instanceof ServerPlayerEntity player) {
            ctx = blockItem.getPlacementContext(ctx);
            if (ctx != null) {
                BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
                var group = blockState.getSoundGroup();
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(group.getPlaceSound()),
                        SoundCategory.BLOCKS, ctx.getBlockPos().getX() + 0.5, ctx.getBlockPos().getY() + 0.5, ctx.getBlockPos().getZ() + 0.5,
                        (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F, player.getRandom().nextLong()));

                return ActionResult.SUCCESS;
            }
        }

        return x;
    }
}
