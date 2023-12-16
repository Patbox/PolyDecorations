package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.furniture.BenchBlock;
import eu.pb4.polydecorations.block.furniture.ShelfBlock;
import eu.pb4.polydecorations.block.other.BrazierBlock;
import eu.pb4.polydecorations.block.other.GlobeBlock;
import eu.pb4.polydecorations.block.plus.SignPostBlock;
import eu.pb4.polydecorations.block.plus.WallAttachedLanternBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.*;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static eu.pb4.polydecorations.ModInit.id;

public class DecorationsBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>();
    public static final WallAttachedLanternBlock WALL_LANTERN = register("wall_lantern",
            new WallAttachedLanternBlock((LanternBlock) Blocks.LANTERN));
    public static final WallAttachedLanternBlock WALL_SOUL_LANTERN = register("wall_soul_lantern",
            new WallAttachedLanternBlock((LanternBlock) Blocks.SOUL_LANTERN));

    public static final BrazierBlock BRAZIER = register("brazier", new BrazierBlock(AbstractBlock.Settings.copy(Blocks.LANTERN).nonOpaque().luminance(x -> {
                return x.get(BrazierBlock.LIT) ? Blocks.CAMPFIRE.getDefaultState().getLuminance() : 0;
            }))

    );
    public static final BrazierBlock SOUL_BRAZIER = register("soul_brazier", new BrazierBlock(AbstractBlock.Settings.copy(Blocks.SOUL_LANTERN).nonOpaque().luminance(x -> {
                return x.get(BrazierBlock.LIT) ? Blocks.SOUL_CAMPFIRE.getDefaultState().getLuminance() : 0;
            }))
    );

    public static final GlobeBlock GLOBE = register("globe", new GlobeBlock(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).nonOpaque()));

    public static final Map<WoodType, ShelfBlock> SHELF = registerWood("shelf", (x) -> {
        var planks = new Identifier(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new ShelfBlock(
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).nonOpaque()
                            .solidBlock(Blocks::never)
            );
        }

        return null;
    });

    public static final Map<WoodType, BenchBlock> BENCH = registerWood("bench", (x) -> {
        var planks = new Identifier(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new BenchBlock(id(x.name() + "_bench"),
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).nonOpaque()
            );
        }

        return null;
    });

    public static final Map<WoodType, SignPostBlock> SIGN_POST = registerWood("sign_post", (x) -> {
        var planks = new Identifier(x.name() + "_fence");
        if (Registries.BLOCK.get(planks) instanceof FenceBlock fenceBlock) {
            return new SignPostBlock(fenceBlock);
        }

        return null;
    });

    //public static final Map<DyeColor, BedWithBannerBlock> BANNER_BED = registerDye("banner_bed", (x) -> {
    //    return new BedWithBannerBlock((BedBlock) Registries.BLOCK.get(new Identifier(x.name().toLowerCase(Locale.ROOT) + "_bed")));
    //});

    private static <T extends Block & PolymerBlock> Map<WoodType, T> registerWood(String id, Function<WoodType, T> object) {
        var map = new HashMap<WoodType, T>();

        WoodType.stream().forEach(x -> {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name() + "_" + id, y));
            }
        });

        return map;
    }

    private static <T extends Block & PolymerBlock> Map<DyeColor, T> registerDye(String id, Function<DyeColor, T> object) {
        var map = new HashMap<DyeColor, T>();

        for (var x : DyeColor.values()) {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name().toLowerCase(Locale.ROOT) + "_" + id, y));
            }
        }

        return map;
    }

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
