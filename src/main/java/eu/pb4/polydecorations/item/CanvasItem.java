package eu.pb4.polydecorations.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.factorytools.api.item.ModeledItem;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.polydecorations.entity.CanvasEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static eu.pb4.polydecorations.ModInit.id;


public class CanvasItem extends ModeledItem {
    public CanvasItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            var entity = CanvasEntity.create(world, context.getSide(), context.getBlockPos().offset(context.getSide()));

            if (entity.canStayAttached()) {
                if (!world.isClient) {
                    entity.onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, entity.getPos());
                    entity.loadFromStack(context.getStack());
                    world.spawnEntity(entity);
                }

                itemStack.decrement(1);
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.CONSUME;
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image.isPresent()) {
            return super.getTranslationKey(stack) +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).glowing() ? ".glowing" : "") +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).waxed() ? ".waxed" : "");
        }
        return super.getTranslationKey(stack) + ".empty";
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image.isEmpty()) {
                tooltip.add(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                        .append(Text.translatable(super.getTranslationKey() + ".tooltip.1",
                                        Text.translatable("text.polydecorations.tooltip.any_dye").formatted(Formatting.WHITE))
                                .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.add(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable(super.getTranslationKey() + ".tooltip.2",
                                    Text.translatable("text.polydecorations.tooltip.coal_and_bone_meal").formatted(Formatting.WHITE))
                            .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.add(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable(super.getTranslationKey() + ".tooltip.3",
                                    Items.SPONGE.getName().copy().formatted(Formatting.WHITE))
                            .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.add(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable(super.getTranslationKey() + ".tooltip.4",
                                    Items.BRUSH.getName().copy().formatted(Formatting.WHITE))
                            .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            return;
        }
        var datac = stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image();
        if (datac.isPresent() && datac.get().length != 16 * 16) {
            return;
        }
        //noinspection OptionalGetWithoutIsPresent
        var data = datac.get();


        for (var y = 0; y < 16*16; y += 32) {
            var text = Text.empty().setStyle(Style.EMPTY.withFont(id("canvas")));
            var builder = new StringBuilder();
            var color = CanvasColor.CLEAR;

            for (var x = 0; x < 32; x++) {
                if (x == 16) {
                    builder.append("c");
                }
                var cColor = CanvasColor.getFromRaw(data[x + y]);
                if (cColor == CanvasColor.CLEAR) {
                    cColor = CanvasColor.OFF_WHITE_NORMAL;
                }
                if (cColor != color) {
                    text.append(Text.literal(builder.toString()).withColor(color == CanvasColor.CLEAR ? 0xffffff : color.getRgbColor()));
                    builder = new StringBuilder();
                    color = cColor;
                }
                builder.append(cColor == CanvasColor.CLEAR ? "a" : x >= 16 ? "_b" : "-b");
            }
            text.append(Text.literal(builder.toString()).withColor(color == CanvasColor.CLEAR ? 0xffffff : color.getRgbColor()));
            tooltip.add(text);
        }
        tooltip.add(Text.empty());
    }
    public static final ComponentType<Data> DATA_TYPE = ComponentType.<Data>builder().codec(Data.CODEC).cache().build();
    public record Data(Optional<byte[]> image, boolean glowing, boolean waxed) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap).optionalFieldOf("image").forGetter(Data::image),
                Codec.BOOL.optionalFieldOf("glowing", false).forGetter(Data::glowing),
                Codec.BOOL.optionalFieldOf("waxed", false).forGetter(Data::waxed)
        ).apply(instance, Data::new));
        public static final Data DEFAULT = new Data(Optional.empty(), false, false);
    }
}
