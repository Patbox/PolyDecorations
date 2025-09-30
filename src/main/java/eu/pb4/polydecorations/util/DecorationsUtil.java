package eu.pb4.polydecorations.util;

import com.mojang.authlib.GameProfile;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class DecorationsUtil {

    public static final List<Direction> REORDERED_DIRECTIONS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final GameProfile GENERIC_PROFILE = new GameProfile(Util.NIL_UUID, "[PolyFactory]");
    public static final Vec3d HALF_BELOW = new Vec3d(0, -0.5, 0);

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

    public static final Map<Direction, BlockState> TRAPDOOR_STATES_REGULAR = Util.mapEnum(Direction.class, x -> PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf(switch (x) {
        case UP -> "BOTTOM";
        case DOWN -> "TOP";
        default -> x.asString().toUpperCase(Locale.ROOT);
    } + "_TRAPDOOR")));
    public static final Map<Direction, BlockState> TRAPDOOR_STATES_WATERLOGGED = Util.mapEnum(Direction.class, x -> PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf(switch (x) {
        case UP -> "BOTTOM";
        case DOWN -> "TOP";
        default -> x.asString().toUpperCase(Locale.ROOT);
    } + "_TRAPDOOR_WATERLOGGED")));


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
        return Identifier.of(ModInit.ID, path);
    }

    public static Text someones(@Nullable GameProfile owner, Text thing) {
        if (owner != null && owner.name() != null) {
            return Text.translatable("text.polydecorations.someones", owner.name(), thing);
        }

        return thing;
    }
}
