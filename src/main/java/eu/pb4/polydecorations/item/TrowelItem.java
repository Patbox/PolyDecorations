package eu.pb4.polydecorations.item;


import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import java.util.ArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TrowelItem extends SimplePolymerItem {
    public TrowelItem(Properties settings) {
        super(settings);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) {
            return InteractionResult.FAIL;
        }
        if (context.getPlayer().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof BlockItem) {
            return InteractionResult.SUCCESS_SERVER;
        }

        var stacks = new ArrayList<ItemStack>(9);
        for (var i = 0; i < 9; i++) {
            var x = context.getPlayer().getInventory().getItem(i);
            if (x.getItem() instanceof BlockItem) {
                stacks.add(x);
            }
        }
        if (stacks.isEmpty()) {
            return InteractionResult.FAIL;
        }
        var stack = stacks.get(context.getPlayer().getRandom().nextInt(stacks.size()));

        var blockItem = ((BlockItem) stack.getItem());
        var ctx = new BlockPlaceContext(context.getPlayer(), context.getHand(), stack,
                new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()));

        var x = blockItem.place(ctx);

        if (x.consumesAction() && ctx.getPlayer() instanceof ServerPlayer player) {
            ctx = blockItem.updatePlacementContext(ctx);
            if (ctx != null) {
                BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
                var group = blockState.getSoundType();
                player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(group.getPlaceSound()),
                        SoundSource.BLOCKS, ctx.getClickedPos().getX() + 0.5, ctx.getClickedPos().getY() + 0.5, ctx.getClickedPos().getZ() + 0.5,
                        (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F, player.getRandom().nextLong()));

                return InteractionResult.SUCCESS_SERVER;
            }
        }

        return x;
    }
}
