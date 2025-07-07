package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WindChimeItem extends FactoryBlockItem {
	private static final Int2ObjectOpenHashMap<DyeColor> BY_COLOR = new Int2ObjectOpenHashMap<>(Arrays.stream(DyeColor.values())
			.collect(Collectors.toMap(DyeColor::getEntityColor, Function.identity())));

	public <T extends Block & PolymerBlock> WindChimeItem(T block, Settings settings) {
		super(block, settings.component(WIND_CHIME_COLOR, IntList.of()));
	}

	@Override
	public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
		super.modifyBasePolymerItemStack(out, stack, context);
		var color = stack.get(WIND_CHIME_COLOR);
		if (color == null || color.isEmpty()) {
			return;
		}

		var colors = new IntArrayList(5);

		for (int i = 0; i < 5; i++) {
			colors.add(color.getInt(i % color.size()));
		}

		out.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of(), colors));
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
		super.appendTooltip(stack, context, displayComponent, textConsumer, type);
		var color = stack.get(WIND_CHIME_COLOR);
		if (color == null || color.isEmpty()) {
			return;
		}

		textConsumer.accept(appendColorsTooltipText(Text.empty().formatted(Formatting.GRAY), color));
	}


	private static Text appendColorsTooltipText(MutableText text, IntList colors) {
		for(int i = 0; i < colors.size(); ++i) {
			if (i > 0) {
				text.append(", ");
			}

			text.append(getColorText(colors.getInt(i)));
		}

		return text;
	}

	private static Text getColorText(int color) {
		DyeColor dyeColor = BY_COLOR.get(color);
		return (Text)(dyeColor == null ? Text.translatable("item.minecraft.firework_star.custom_color") : Text.translatable("item.minecraft.firework_star." + dyeColor.getId()));
	}
	public static final ComponentType<IntList> WIND_CHIME_COLOR = ComponentType.<IntList>builder().codec(Codecs.RGB.listOf().xmap(IntArrayList::new, List::copyOf)).cache().build();
}
