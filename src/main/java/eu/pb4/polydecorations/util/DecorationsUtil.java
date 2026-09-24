package eu.pb4.polydecorations.util;

import com.mojang.authlib.GameProfile;
import eu.pb4.polydecorations.DecorationsFeature;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("UnstableApiUsage")
public class DecorationsUtil {

    public static final List<Direction> REORDERED_DIRECTIONS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final GameProfile GENERIC_PROFILE = new GameProfile(Util.NIL_UUID, "[PolyFactory]");
    public static final Vec3 HALF_BELOW = new Vec3(0, -0.5, 0);

    public static final List<DyeColor> COLORS_CREATIVE = List.of(DyeColor.WHITE,
            DyeColor.LIGHT_GRAY,
            DyeColor.GRAY,
            DyeColor.BLACK,
            DyeColor.BROWN,
            DyeColor.RED,
            DyeColor.ORANGE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.GREEN,
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.BLUE,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.PINK);

    private static final List<Runnable> RUN_NEXT_TICK = new ArrayList<>();

    public static final Map<Direction, BlockState> TRAPDOOR_STATES_REGULAR = DecorationsFeature.anyRegistered(DecorationsFeature.TOOL_RACK, DecorationsFeature.SLEEPING_BAG)
            ? Util.makeEnumMap(Direction.class, x -> PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf("TRAPDOOR_" + switch (x) {
        case UP -> "BOTTOM";
        case DOWN -> "TOP";
        default -> x.getSerializedName().toUpperCase(Locale.ROOT);
    }))) : Map.of();
    public static final Map<Direction, BlockState> TRAPDOOR_STATES_WATERLOGGED = DecorationsFeature.TOOL_RACK.isRegistered()
            ? Util.makeEnumMap(Direction.class, x -> PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf("TRAPDOOR_" + switch (x) {
        case UP -> "BOTTOM";
        case DOWN -> "TOP";
        default -> x.getSerializedName().toUpperCase(Locale.ROOT);
    } + "_WATERLOGGED"))) : Map.of();

    public static final BlockState CAMPFIRE_STATE = DecorationsFeature.anyRegistered(DecorationsFeature.BENCH, DecorationsFeature.BASKET, DecorationsFeature.COPPER_CAMPFIRE, DecorationsFeature.FLOWER_POTS)
            ? PolymerBlockResourceUtils.requestEmpty(BlockModelType.CAMPFIRE) : null;
    public static final BlockState CAMPFIRE_WATERLOGGED_STATE = DecorationsFeature.anyRegistered(DecorationsFeature.BENCH, DecorationsFeature.BASKET, DecorationsFeature.COPPER_CAMPFIRE)
            ? PolymerBlockResourceUtils.requestEmpty(BlockModelType.CAMPFIRE_WATERLOGGED) : null;

    public static void runNextTick(Runnable runnable) {
        RUN_NEXT_TICK.add(runnable);
    }

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(DecorationsUtil::onTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(DecorationsUtil::onServerStopped);
    }

    private static void onServerStopped(MinecraftServer server) {
        RUN_NEXT_TICK.clear();
    }

    private static void onTick(MinecraftServer server) {
        RUN_NEXT_TICK.forEach(Runnable::run);
        RUN_NEXT_TICK.clear();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ModInit.ID, path);
    }

    public static Component someones(@Nullable GameProfile owner, Component thing) {
        if (owner != null && owner.name() != null) {
            return Component.translatable("text.polydecorations.someones", owner.name(), thing);
        }

        return thing;
    }

    public static  <X, Y> void getValues(Map<X, Y> map, List<X> keys, BiConsumer<X, Y> consumer) {
        for (var key : keys) {
            var val = map.get(key);
            if (val != null) {
                consumer.accept(key, val);
            }
        }
    }
}
