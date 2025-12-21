package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class WindChimeItem extends FactoryBlockItem {
	private static final Int2ObjectOpenHashMap<DyeColor> BY_COLOR = new Int2ObjectOpenHashMap<>(Arrays.stream(DyeColor.values())
			.collect(Collectors.toMap(DyeColor::getTextureDiffuseColor, Function.identity())));

	public <T extends Block & PolymerBlock> WindChimeItem(T block, Properties settings) {
		super(block, settings.component(DecorationsDataComponents.WIND_CHIME_COLOR, IntList.of()));
	}

	@Override
	public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
		super.modifyBasePolymerItemStack(out, stack, context);
		var color = stack.get(DecorationsDataComponents.WIND_CHIME_COLOR);
		if (color == null || color.isEmpty()) {
			return;
		}

		var colors = new IntArrayList(5);

		for (int i = 0; i < 5; i++) {
			colors.add(color.getInt(i % color.size()));
		}

		out.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), colors));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
		super.appendHoverText(stack, context, displayComponent, textConsumer, type);
		var color = stack.get(DecorationsDataComponents.WIND_CHIME_COLOR);
		if (color == null || color.isEmpty()) {
			return;
		}

		textConsumer.accept(appendColorsTooltipText(Component.empty().withStyle(ChatFormatting.GRAY), color));
	}


	private static Component appendColorsTooltipText(MutableComponent text, IntList colors) {
		for(int i = 0; i < colors.size(); ++i) {
			if (i > 0) {
				text.append(", ");
			}

			text.append(getColorText(colors.getInt(i)));
		}

		return text;
	}

	private static Component getColorText(int color) {
		DyeColor dyeColor = BY_COLOR.get(color);
		return (Component)(dyeColor == null ? Component.translatable("item.minecraft.firework_star.custom_color") : Component.translatable("item.minecraft.firework_star." + dyeColor.getName()));
	}
}
