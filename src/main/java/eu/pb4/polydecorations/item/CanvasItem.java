package eu.pb4.polydecorations.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.polydecorations.entity.CanvasEntity;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.block.MapColor;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.TooltipDisplayComponent;
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
import java.util.*;
import java.util.function.Consumer;

import static eu.pb4.polydecorations.ModInit.id;


public class CanvasItem extends SimplePolymerItem {
    public CanvasItem(Settings settings) {
        super(settings.component(DATA_TYPE, Data.DEFAULT));
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
            var entity = CanvasEntity.create(world, context.getSide(), context.getBlockPos().offset(context.getSide()), context.getPlayerYaw());

            if (entity.canStayAttached()) {
                if (!world.isClient) {
                    entity.onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, entity.getPos());
                    entity.loadFromStack(context.getStack());
                    world.spawnEntity(entity);
                }

                itemStack.decrement(1);
                return ActionResult.SUCCESS_SERVER;
            } else {
                return ActionResult.CONSUME;
            }
        }
    }

    public String getTranslationKey(ItemStack stack) {

        if (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image.isPresent()) {
            return super.translationKey +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).glowing() ? ".glowing" : "") +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).waxed() ? ".waxed" : "") +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).cut() ? ".cut" : "")
                    ;
        }
        return super.translationKey + ".empty";
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(getTranslationKey(stack));
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return player.canPlaceOn(pos, side, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> tooltip, TooltipType type) {
        if (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image.isEmpty()) {
                tooltip.accept(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                        .append(Text.translatable(super.getTranslationKey() + ".tooltip.1",
                                        Text.translatable("text.polydecorations.tooltip.any_dye").formatted(Formatting.WHITE))
                                .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.accept(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable(super.getTranslationKey() + ".tooltip.2",
                                    Text.translatable("text.polydecorations.tooltip.coal_and_bone_meal").formatted(Formatting.WHITE))
                            .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.accept(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable(super.getTranslationKey() + ".tooltip.3",
                                    Items.SPONGE.getName().copy().formatted(Formatting.WHITE))
                            .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.accept(Text.empty().append(Text.literal("| ").formatted(Formatting.DARK_GRAY))
                    .append(Text.translatable(super.getTranslationKey() + ".tooltip.4",
                                    Items.BRUSH.getName().copy().formatted(Formatting.WHITE))
                            .formatted(Formatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            return;
        }
        var background = stack.getOrDefault(DATA_TYPE, Data.DEFAULT).background().orElse(CanvasColor.OFF_WHITE_NORMAL);
        var datac = stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image();
        if (datac.isPresent() && datac.get().length != 16 * 16) {
            return;
        }
        //noinspection OptionalGetWithoutIsPresent
        var data = datac.get();


        for (var y = 0; y < 16 * 16; y += 32) {
            var text = Text.empty().setStyle(Style.EMPTY.withFont(id("canvas")));
            var builder = new StringBuilder();
            var color = CanvasColor.CLEAR;

            for (var x = 0; x < 32; x++) {
                if (x == 16) {
                    builder.append("c");
                }
                var cColor = CanvasColor.getFromRaw(data[x + y]);
                if (cColor != color) {
                    text.append(Text.literal(builder.toString()).withColor(color == CanvasColor.CLEAR ? background.getRgbColor() : color.getRgbColor()));
                    builder = new StringBuilder();
                    color = cColor;
                }
                builder.append(cColor == CanvasColor.CLEAR_FORCE ? "a" : x >= 16 ? "_b" : "-b");
            }
            text.append(Text.literal(builder.toString()).withColor(color.getColor() == MapColor.CLEAR ? background.getRgbColor() : color.getRgbColor()));
            tooltip.accept(text);
        }
        tooltip.accept(Text.empty());
    }
    public static final ComponentType<Data> DATA_TYPE = ComponentType.<Data>builder().codec(Data.CODEC).cache().build();
    public record Data(Optional<byte[]> image, Optional<CanvasColor> background, boolean glowing, boolean waxed, boolean cut) {
        private static final byte[] EMPTY_IMAGE = new byte[0];
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap).optionalFieldOf("image").forGetter(Data::image),
                Codec.BYTE.xmap(CanvasColor::getFromRaw, CanvasColor::getRenderColor).optionalFieldOf("background").forGetter(Data::background),
                Codec.BOOL.optionalFieldOf("glowing", false).forGetter(Data::glowing),
                Codec.BOOL.optionalFieldOf("waxed", false).forGetter(Data::waxed),
                Codec.BOOL.optionalFieldOf("cut", false).forGetter(Data::cut)
        ).apply(instance, Data::new));

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Data data = (Data) object;


            return glowing == data.glowing && waxed == data.waxed && cut == data.cut
                    && Arrays.equals(image.orElse(EMPTY_IMAGE), data.image.orElse(EMPTY_IMAGE))
                    && Objects.equals(background, data.background);
        }

        @Override
        public int hashCode() {
            return Objects.hash(image, background, glowing, waxed, cut);
        }

        public static final Data DEFAULT = new Data(Optional.empty(),  Optional.empty(), false, false, false);
    }
}
