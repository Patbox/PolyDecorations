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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static eu.pb4.polydecorations.ModInit.id;

public class DecorationsBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>();
    public static final WallAttachedLanternBlock WALL_LANTERN = register("wall_lantern", (LanternBlock) Blocks.LANTERN, WallAttachedLanternBlock::new);
    public static final WallAttachedLanternBlock WALL_SOUL_LANTERN = register("wall_soul_lantern", (LanternBlock) Blocks.SOUL_LANTERN, WallAttachedLanternBlock::new);

    public static final BrazierBlock BRAZIER = register("brazier", Blocks.LANTERN, (settings, ignored) -> new BrazierBlock(settings.nonOpaque().luminance(x -> {
                return x.get(BrazierBlock.LIT) ? Blocks.CAMPFIRE.getDefaultState().getLuminance() : 0;
            }))

    );
    public static final BrazierBlock SOUL_BRAZIER = register("soul_brazier", Blocks.SOUL_LANTERN, (settings, ignored) -> new BrazierBlock(settings.nonOpaque().luminance(x -> {
                return x.get(BrazierBlock.LIT) ? Blocks.SOUL_CAMPFIRE.getDefaultState().getLuminance() : 0;
            }))
    );
    public static final GlobeBlock GLOBE = register("globe", AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).nonOpaque(), GlobeBlock::new);
    public static final RopeBlock ROPE = register("rope", AbstractBlock.Settings.create().strength(1f).sounds(BlockSoundGroup.COBWEB).breakInstantly().nonOpaque(), RopeBlock::new);
    public static final DisplayCaseBlock DISPLAY_CASE = register("display_case", AbstractBlock.Settings.copy(Blocks.GLASS).nonOpaque(), DisplayCaseBlock::new);
    public static final TrashCanBlock TRASHCAN = register("trashcan", settings -> new TrashCanBlock(settings
            .mapColor(MapColor.IRON_GRAY).strength(3.5F).sounds(BlockSoundGroup.LANTERN).nonOpaque()));
    public static final LargeFlowerPotBlock LARGE_FLOWER_POT = register("large_flower_pot", settings ->new LargeFlowerPotBlock(settings
            .mapColor(MapColor.ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.25F).nonOpaque()));

    public static final Map<WoodType, ShelfBlock> SHELF = registerWood("shelf", (x, id, settings) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new ShelfBlock(
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)).nonOpaque()
                            .solidBlock(Blocks::never), Registries.BLOCK.get(planks), id(x.name() + "_shelf")
            );
        }

        return null;
    });

    public static final Map<WoodType, BenchBlock> BENCH = registerWood("bench", (x, id, settings) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new BenchBlock(
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)).nonOpaque()
                            .solidBlock(Blocks::never),
                    id,
                    Registries.BLOCK.get(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, ToolRackBlock> TOOL_RACK = registerWood("tool_rack", (x, id, settings) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new ToolRackBlock(
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)).nonOpaque()
                            .solidBlock(Blocks::never),
                    Registries.BLOCK.get(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, TableBlock> TABLE = registerWood("table", (x, id, settings) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            return new TableBlock(id,
                    AbstractBlock.Settings.copy(Registries.BLOCK.get(planks)).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)).nonOpaque()
                            .solidBlock(Blocks::never),
                    Registries.BLOCK.get(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, AttachedSignPostBlock> WOOD_SIGN_POST = registerWood("sign_post", (x, id, settings) -> {
        var planks = Identifier.of(x.name() + "_fence");
        var block = Registries.BLOCK.get(planks);
        if (block instanceof FenceBlock) {
            return new AttachedSignPostBlock(AbstractBlock.Settings.copy(block).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)), block, 4);
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
          map.put(b, register(Registries.BLOCK.getId(b).getPath() + "_sign_post", (s) -> new AttachedSignPostBlock(s, b, 8)));
      }
      return map;
    });

    public static final AttachedSignPostBlock NETHER_BRICK_SIGN_POST = register("nether_brick_sign_post", Blocks.NETHER_BRICK_FENCE,
            (settings, block) -> new AttachedSignPostBlock(settings, block, 4));

    public static final Map<WoodType, MailboxBlock> WOODEN_MAILBOX = registerWood("mailbox", (x, id, settings) -> {
        var planks = Identifier.of(x.name() + "_planks");
        if (Registries.BLOCK.containsId(planks)) {
            var block = Registries.BLOCK.get(planks);
            return new MailboxBlock(AbstractBlock.Settings.copy(block).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)), block);
        }

        return null;
    });
    public static final GhostLightBlock GHOST_LIGHT = register("ghost_light",
            settings -> new GhostLightBlock(settings.nonOpaque()
                    .noCollision().breakInstantly().luminance(x -> 7), 5, 1, 0.001f, ParticleTypes.SOUL_FIRE_FLAME));



    private static <T extends Block & PolymerBlock> Map<WoodType, T> registerWood(String id, TriFunction<WoodType, Identifier, AbstractBlock.Settings, T> object) {
        var map = new HashMap<WoodType, T>();

        WoodUtil.VANILLA.forEach(x -> {
            var y = register(x.name() + "_" + id, (s) -> object.apply(x, id(x.name() + "_" + id), s));
            if (y != null) {
                map.put(x, y);
            }
        });

        return map;
    }

    private static <T extends Block & PolymerBlock> Map<DyeColor, T> registerDye(String id, Function<DyeColor, T> object) {
        var map = new HashMap<DyeColor, T>();

        for (var x : DyeColor.values()) {
            var y = object.apply(x);
            if (y != null) {
                map.put(x, register(x.name().toLowerCase(Locale.ROOT) + "_" + id, (s) -> y));
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
            if (block.getLootTableKey().isPresent()) {
                var lt = server.getReloadableRegistries().getLootTable(block.getLootTableKey().get());
                if (lt == LootTable.EMPTY) {
                    ModInit.LOGGER.warn("Missing loot table? " + block.getLootTableKey().get().getValue());
                }
            }
            if (block instanceof BlockEntityProvider provider) {
                var be = provider.createBlockEntity(BlockPos.ORIGIN, block.getDefaultState());
                assert be == null || be.getType().supports(block.getDefaultState());
            }

        }
    }

    public static <T extends Block> T register(String path, Function<AbstractBlock.Settings, T> function) {
        return register(path, AbstractBlock.Settings.create(), function);
    }

    public static <T extends Block, Y extends Block> T register(String path, Y copyFrom, BiFunction<AbstractBlock.Settings, Y, T> function) {
        return register(path, AbstractBlock.Settings.copy(copyFrom), (settings) -> function.apply(settings, copyFrom));
    }

    @NotNull
    public static <T extends Block> T register(String path, AbstractBlock.Settings settings, Function<AbstractBlock.Settings, T> function) {
        var id = Identifier.of(ModInit.ID, path);
        var item = function.apply(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)));
        if (item == null) {
            //noinspection DataFlowIssue
            return null;
        }
        BLOCKS.add(item);
        return Registry.register(Registries.BLOCK, id, item);
    }


}
