package eu.pb4.polydecorations.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.polydecorations.entity.CanvasEntity;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;

import static eu.pb4.polydecorations.ModInit.id;


public class CanvasItem extends SimplePolymerItem {
    public CanvasItem(Properties settings) {
        super(settings.component(DATA_TYPE, Data.DEFAULT));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos blockPos2 = blockPos.relative(direction);
        Player playerEntity = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return InteractionResult.FAIL;
        } else {
            Level world = context.getLevel();
            var entity = CanvasEntity.create(world, context.getClickedFace(), context.getClickedPos().relative(context.getClickedFace()), context.getRotation());

            if (entity.survives()) {
                if (!world.isClientSide()) {
                    entity.playPlacementSound();
                    world.gameEvent(playerEntity, GameEvent.ENTITY_PLACE, entity.position());
                    entity.loadFromStack(context.getItemInHand());
                    world.addFreshEntity(entity);
                }

                itemStack.shrink(1);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    public String getTranslationKey(ItemStack stack) {

        if (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image.isPresent()) {
            return super.descriptionId +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).glowing() ? ".glowing" : "") +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).waxed() ? ".waxed" : "") +
                    (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).cut() ? ".cut" : "")
                    ;
        }
        return super.descriptionId + ".empty";
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getTranslationKey(stack));
    }

    protected boolean canPlaceOn(Player player, Direction side, ItemStack stack, BlockPos pos) {
        return player.mayUseItemAt(pos, side, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> tooltip, TooltipFlag type) {
        if (stack.getOrDefault(DATA_TYPE, Data.DEFAULT).image.isEmpty()) {
                tooltip.accept(Component.empty().append(Component.literal("| ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.translatable(super.getDescriptionId() + ".tooltip.1",
                                        Component.translatable("text.polydecorations.tooltip.any_dye").withStyle(ChatFormatting.WHITE))
                                .withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.accept(Component.empty().append(Component.literal("| ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable(super.getDescriptionId() + ".tooltip.2",
                                    Component.translatable("text.polydecorations.tooltip.coal_and_bone_meal").withStyle(ChatFormatting.WHITE))
                            .withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.accept(Component.empty().append(Component.literal("| ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable(super.getDescriptionId() + ".tooltip.3",
                                    Items.SPONGE.getName().copy().withStyle(ChatFormatting.WHITE))
                            .withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
            tooltip.accept(Component.empty().append(Component.literal("| ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable(super.getDescriptionId() + ".tooltip.4",
                                    Items.BRUSH.getName().copy().withStyle(ChatFormatting.WHITE))
                            .withStyle(ChatFormatting.GRAY)).setStyle(Style.EMPTY.withItalic(false)));
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
            var text = Component.empty().setStyle(Style.EMPTY.withFont(new FontDescription.Resource(id("canvas"))));
            var builder = new StringBuilder();
            var color = CanvasColor.CLEAR;

            for (var x = 0; x < 32; x++) {
                if (x == 16) {
                    builder.append("c");
                }
                var cColor = CanvasColor.getFromRaw(data[x + y]);
                if (cColor != color) {
                    text.append(Component.literal(builder.toString()).withColor(color == CanvasColor.CLEAR ? background.getRgbColor() : color.getRgbColor()));
                    builder = new StringBuilder();
                    color = cColor;
                }
                builder.append(cColor == CanvasColor.CLEAR_FORCE ? "a" : x >= 16 ? "_b" : "-b");
            }
            text.append(Component.literal(builder.toString()).withColor(color.getColor() == MapColor.NONE ? background.getRgbColor() : color.getRgbColor()));
            tooltip.accept(text);
        }
        tooltip.accept(Component.empty());
    }
    public static final DataComponentType<Data> DATA_TYPE = DataComponentType.<Data>builder().persistent(Data.CODEC).cacheEncoding().build();
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
