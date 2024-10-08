package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.furniture.BenchBlock;
import eu.pb4.polydecorations.block.furniture.LargeFlowerPotBlock;
import eu.pb4.polydecorations.block.furniture.TableBlock;
import eu.pb4.polydecorations.block.item.*;
import eu.pb4.polydecorations.block.furniture.BrazierBlock;
import eu.pb4.polydecorations.block.extension.AttachedSignPostBlock;
import eu.pb4.polydecorations.block.extension.WallAttachedLanternBlock;
import eu.pb4.polydecorations.block.other.GhostLightBlock;
import eu.pb4.polydecorations.block.other.RopeBlock;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.loot.LootTable;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.BiFunction;
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
    public static final RopeBlock ROPE = register("rope", new RopeBlock(AbstractBlock.Settings.create().strength(1f).sounds(BlockSoundGroup.COBWEB).breakInstantly().nonOpaque()));
    public static final DisplayCaseBlock DISPLAY_CASE = register("display_case", new DisplayCaseBlock(AbstractBlock.Settings.copy(Blocks.GLASS).nonOpaque()));
    public static final TrashCanBlock TRASHCAN = register("trashcan", new TrashCanBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.IRON_GRAY).strength(3.5F).sounds(BlockSoundGroup.LANTERN).nonOpaque()));
    public static final LargeFlowerPotBlock LARGE_FLOWER_POT = register("large_flower_pot", new LargeFlowerPotBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.25F).nonOpaque()));

    public static final Map<WoodType, ShelfBlock> SHELF = registerWood("shelf", (x, id) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new ShelfBlock(
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).nonOpaque()
                            .solidBlock(Blocks::never), Registries.BLOCK.get(planks), id(x.name() + "_shelf")
            );
        }

        return null;
    });

    public static final Map<WoodType, BenchBlock> BENCH = registerWood("bench", (x, id) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new BenchBlock(id,
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).nonOpaque()
                            .solidBlock(Blocks::never),
                    Registries.BLOCK.get(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, ToolRackBlock> TOOL_RACK = registerWood("tool_rack", (x, id) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new ToolRackBlock(
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).nonOpaque()
                            .solidBlock(Blocks::never),
                    Registries.BLOCK.get(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, TableBlock> TABLE = registerWood("table", (x, id) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new TableBlock(id,
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).nonOpaque()
                            .solidBlock(Blocks::never),
                    Registries.BLOCK.get(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, AttachedSignPostBlock> WOOD_SIGN_POST = registerWood("sign_post", (x, id) -> {
        var planks = Identifier.of(x.name() + "_fence");
        var block = Registries.BLOCK.get(planks);
        if (block instanceof FenceBlock) {
            return new AttachedSignPostBlock(block, 4);
        }

        return null;
    });

    public static final Map<Block, AttachedSignPostBlock> WALL_SIGN_POST = Util.make(() -> {
      var map = new HashMap<Block, AttachedSignPostBlock>();
      var l = new ArrayList<Block>();
      for (var b : Registries.BLOCK) {
          if (b instanceof WallBlock && Registries.BLOCK.getId(b).getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
              l.add(b);
          }
      }

      for (var b : l) {
          map.put(b, register(Registries.BLOCK.getId(b).getPath() + "_sign_post", new AttachedSignPostBlock(b, 8)));
      }
      return map;
    });

    public static final AttachedSignPostBlock NETHER_BRICK_SIGN_POST = register("nether_brick_sign_post", new AttachedSignPostBlock(Blocks.NETHER_BRICK_FENCE, 4));

    public static final Map<WoodType, MailboxBlock> WOODEN_MAILBOX = registerWood("mailbox", (x, id) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new MailboxBlock(Registries.BLOCK.get(planks));
        }

        return null;
    });
    public static final GhostLightBlock GHOST_LIGHT = register("ghost_light",
            new GhostLightBlock(AbstractBlock.Settings.create().nonOpaque()
                    .noCollision().breakInstantly().luminance(x -> 7), 5, 1, 0.001f, ParticleTypes.SOUL_FIRE_FLAME));



    private static <T extends Block & PolymerBlock> Map<WoodType, T> registerWood(String id, BiFunction<WoodType, Identifier, T> object) {
        var map = new HashMap<WoodType, T>();

        WoodUtil.VANILLA.forEach(x -> {
            var y = object.apply(x, id(x.name() + "_" + id));
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
            var lt = server.getReloadableRegistries().getLootTable(block.getLootTableKey());
            if (lt == LootTable.EMPTY) {
                ModInit.LOGGER.warn("Missing loot table? " + block.getLootTableKey().getValue());
            }
            if (block instanceof BlockEntityProvider provider) {
                var be = provider.createBlockEntity(BlockPos.ORIGIN, block.getDefaultState());
                assert be == null || be.getType().supports(block.getDefaultState());
            }
        }
    }

    public static <T extends Block> T register(String path, T item) {
        BLOCKS.add(item);
        return Registry.register(Registries.BLOCK, Identifier.of(ModInit.ID, path), item);
    }
}
