package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.AutoModeledPolymerItem;
import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.polydecorations.block.plus.SignPostBlock;
import eu.pb4.polydecorations.block.plus.SignPostBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;

public class SignPostItem extends Item implements AutoModeledPolymerItem {
    private final Item item = BaseItemProvider.requestItem();
    private String translationKey;

    public SignPostItem(Settings settings) {
        super(settings);
    }

    protected String getOrCreateTranslationKey() {
        if (this.translationKey == null) {
            this.translationKey = Util.createTranslationKey("block", Registries.ITEM.getId(this));
        }

        return this.translationKey;
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var upper = (context.getHitPos().getY() - (int) context.getHitPos().getY()) >= 0.5;
        var blockState = context.getWorld().getBlockState(context.getBlockPos());
        if (blockState.getBlock() instanceof SignPostBlock) {
            if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof SignPostBlockEntity be) {
                var text = be.getText(upper);
                if (text.item() == Items.AIR) {
                    be.setText(upper, SignPostBlockEntity.Sign.of(context.getStack().getItem(), 180 + context.getPlayerYaw()));
                    if (context.getPlayer() instanceof ServerPlayerEntity player) {
                        be.openText(upper, player);
                    }
                }
            }
        } else if (SignPostBlock.MAP.containsKey(blockState.getBlock())) {
            context.getWorld().setBlockState(context.getBlockPos(), SignPostBlock.MAP.get(blockState.getBlock()).getDefaultState().with(Properties.WATERLOGGED, blockState.get(Properties.WATERLOGGED)));
            if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof SignPostBlockEntity be) {
                be.setText(upper, SignPostBlockEntity.Sign.of(context.getStack().getItem(), 180 + context.getPlayerYaw()));
                if (context.getPlayer() instanceof ServerPlayerEntity player) {
                    be.openText(upper, player);
                }
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public Item getPolymerItem() {
        return this.item;
    }
}
