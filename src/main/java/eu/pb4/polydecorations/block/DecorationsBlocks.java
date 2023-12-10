package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.furniture.ShelfBlock;
import eu.pb4.polydecorations.block.plus.WallAttachedLanternBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DecorationsBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>();
    public static final WallAttachedLanternBlock WALL_LANTERN = register("wall_lantern",
            new WallAttachedLanternBlock((LanternBlock) Blocks.LANTERN));
    public static final WallAttachedLanternBlock WALL_SOUL_LANTERN = register("wall_soul_lantern",
            new WallAttachedLanternBlock((LanternBlock) Blocks.SOUL_LANTERN));

    public static final Map<WoodType, ShelfBlock> SHELF = Util.make(() -> {
        var map = new HashMap<WoodType, ShelfBlock>();

        WoodType.stream().forEach(x -> {
            var planks = new Identifier(x.name() + "_planks");
            if (Registries.BLOCK.containsId(planks)) {
                map.put(x, register(x.name() + "_" + "shelf", new ShelfBlock(
                        AbstractBlock.Settings.copy(Registries.BLOCK.get(planks))
                )));
            }
        });

        return map;
    });

    public static void register() {
        if (ModInit.DEV_MODE) {
            ServerLifecycleEvents.SERVER_STARTED.register((DecorationsBlocks::validateLootTables));
            ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, success) -> {
                validateLootTables(server);
            }));
        }
    }

    public static void forEveryEntry(Consumer<Block> blockConsumer) {
        for (var block : Registries.BLOCK.stream().toList()) {
            blockConsumer.accept(block);
        }
        RegistryEntryAddedCallback.event(Registries.BLOCK).register(((rawId, id, object) -> blockConsumer.accept(object)));
    }

    private static void validateLootTables(MinecraftServer server) {
        for (var block : BLOCKS) {
            var lt = server.getLootManager().getLootTable(block.getLootTableId());
            if (lt == LootTable.EMPTY) {
                ModInit.LOGGER.warn("Missing loot table? " + block.getLootTableId());
            }
        }
    }

    public static <T extends Block> T register(String path, T item) {
        BLOCKS.add(item);
        return Registry.register(Registries.BLOCK, new Identifier(ModInit.ID, path), item);
    }
}
