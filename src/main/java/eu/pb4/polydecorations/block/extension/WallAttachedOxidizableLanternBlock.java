package eu.pb4.polydecorations.block.extension;

import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringLanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;
import java.util.Map;

import static eu.pb4.polydecorations.ModInit.id;

public class WallAttachedOxidizableLanternBlock extends WallAttachedLanternBlock implements WeatheringCopper {
    private final WeatheringLanternBlock lantern;

    public WallAttachedOxidizableLanternBlock(Properties settings, WeatheringLanternBlock block) {
        super(settings, block);
        this.lantern = block;
    }

    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        this.changeOverTime(state, world, pos, random);
    }

    protected boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

    public WeatheringCopper.WeatherState getAge() {
        return this.lantern.getAge();
    }
}
