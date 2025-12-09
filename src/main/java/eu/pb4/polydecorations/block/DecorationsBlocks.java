package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.extension.AttachedSignPostBlock;
import eu.pb4.polydecorations.block.extension.WallAttachedLanternBlock;
import eu.pb4.polydecorations.block.extension.WallAttachedOxidizableLanternBlock;
import eu.pb4.polydecorations.block.furniture.*;
import eu.pb4.polydecorations.block.item.*;
import eu.pb4.polydecorations.block.other.GhostLightBlock;
import eu.pb4.polydecorations.block.other.RopeBlock;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperBlocks;
import net.minecraft.world.level.block.WeatheringLanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static eu.pb4.polydecorations.ModInit.id;

public class DecorationsBlocks {
    private static final List<Block> BLOCKS = new ArrayList<>();
    public static final WallAttachedLanternBlock WALL_LANTERN = register("wall_lantern", (LanternBlock) Blocks.LANTERN, WallAttachedLanternBlock::new);
    public static final WallAttachedLanternBlock WALL_SOUL_LANTERN = register("wall_soul_lantern", (LanternBlock) Blocks.SOUL_LANTERN, WallAttachedLanternBlock::new);

    public static WeatheringCopperBlocks WALL_COPPER_LANTERNS = registerRelativeCopper("copper_wall_lantern", Blocks.COPPER_LANTERN,
            (settings, block) -> new WallAttachedLanternBlock(settings, (LanternBlock) block),
            (level, settings, block) -> new WallAttachedOxidizableLanternBlock(settings, (WeatheringLanternBlock) block),
            (level, block) -> BlockBehaviour.Properties.ofFullCopy(block)
    );

    public static final BrazierBlock BRAZIER = register("brazier", Blocks.LANTERN, (settings, ignored) -> new BrazierBlock(settings.noOcclusion().lightLevel(x -> {
                return x.getValue(BrazierBlock.LIT) ? Blocks.CAMPFIRE.defaultBlockState().getLightEmission() : 0;
            }))

    );
    public static final BrazierBlock SOUL_BRAZIER = register("soul_brazier", Blocks.SOUL_LANTERN, (settings, ignored) -> new BrazierBlock(settings.noOcclusion().lightLevel(x -> {
                return x.getValue(BrazierBlock.LIT) ? Blocks.SOUL_CAMPFIRE.defaultBlockState().getLightEmission() : 0;
            }))
    );

    public static final BrazierBlock COPPER_BRAZIER = register("copper_brazier", Blocks.COPPER_LANTERN.unaffected(), (settings, ignored) -> new BrazierBlock(settings.noOcclusion().lightLevel(x -> {
                return x.getValue(BrazierBlock.LIT) ? Blocks.CAMPFIRE.defaultBlockState().getLightEmission() : 0;
            }))
    );

    public static final PolymerCampfireBlock COPPER_CAMPFIRE = register("copper_campfire", Blocks.CAMPFIRE, (settings, block) -> new PolymerCampfireBlock(true, 1, settings));

    public static final GlobeBlock GLOBE = register("globe", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion(), GlobeBlock::new);
    public static final RopeBlock ROPE = register("rope", BlockBehaviour.Properties.of().strength(1f).sound(SoundType.COBWEB).instabreak().noOcclusion(), RopeBlock::new);
    public static final DisplayCaseBlock DISPLAY_CASE = register("display_case", BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion(), DisplayCaseBlock::new);
    public static final WindChimeBlock WIND_CHIME = register("wind_chime", BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion(), WindChimeBlock::new);
    public static final TrashCanBlock TRASHCAN = register("trashcan", settings -> new TrashCanBlock(settings
            .mapColor(MapColor.METAL).strength(3.5F).sound(SoundType.LANTERN).noOcclusion()));

    public static final BasketBlock BASKET = register("basket", settings -> new BasketBlock(settings
            .mapColor(MapColor.WOOD).strength(0.5F)
            .ignitedByLava()
            .sound(SoundType.SCAFFOLDING).noOcclusion()));

    public static final LargeFlowerPotBlock LARGE_FLOWER_POT = register("large_flower_pot", settings -> new LargeFlowerPotBlock(settings
            .mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.25F).noOcclusion()));

    public static final LongFlowerPotBlock LONG_FLOWER_POT = register("long_flower_pot", BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE).instabreak().noOcclusion(), LongFlowerPotBlock::new);

    public static final GhostLightBlock GHOST_LIGHT = register("ghost_light",
            settings -> new GhostLightBlock(settings.noOcclusion()
                    .noCollision().instabreak().lightLevel(x -> 7), 5, 1, 0.001f, ParticleTypes.SOUL_FIRE_FLAME));

    public static final Map<WoodType, PlainShelfBlock> SHELF = registerWood("shelf", (x, id, settings) -> {
        var planks = Identifier.parse(x.name() + "_planks");
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new PlainShelfBlock(
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never), BuiltInRegistries.BLOCK.getValue(planks), id(x.name() + "_shelf")
            );
        }

        return null;
    });

    public static final Map<WoodType, BenchBlock> BENCH = registerWood("bench", (x, id, settings) -> {
        var planks = Identifier.parse(x.name() + "_planks");
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new BenchBlock(
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    id,
                    BuiltInRegistries.BLOCK.getValue(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, ToolRackBlock> TOOL_RACK = registerWood("tool_rack", (x, id, settings) -> {
        var planks = Identifier.parse(x.name() + "_planks");
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new ToolRackBlock(
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    BuiltInRegistries.BLOCK.getValue(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, TableBlock> TABLE = registerWood("table", (x, id, settings) -> {
        var planks = Identifier.parse(x.name() + "_planks");
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new TableBlock(id,
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    BuiltInRegistries.BLOCK.getValue(planks)
            );
        }

        return null;
    });

    public static final Map<WoodType, StumpBlock> STUMP = registerWood("stump", (x, id, settings) -> {
        var log = Identifier.parse(WoodUtil.getLogName(x));

        if (BuiltInRegistries.BLOCK.containsKey(log)) {
            var logBlock = BuiltInRegistries.BLOCK.getValue(log);

            return new StumpBlock(
                    BlockBehaviour.Properties.ofFullCopy(logBlock).mapColor(logBlock.defaultMapColor()).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    logBlock
            );
        }

        return null;
    });

    public static final Map<WoodType, StumpBlock> STRIPPED_STUMP = registerWood("stripped_", "stump", (x, id, settings) -> {
        var log = Identifier.parse("stripped_" + WoodUtil.getLogName(x));

        if (BuiltInRegistries.BLOCK.containsKey(log)) {
            var logBlock = BuiltInRegistries.BLOCK.getValue(log);

            var b = new StumpBlock(
                    BlockBehaviour.Properties.ofFullCopy(logBlock).mapColor(logBlock.defaultMapColor()).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    logBlock
            );
            //StrippableBlockRegistry.register(STUMP.get(x), b);
            return b;
        }

        return null;
    });

    public static final Map<WoodType, AttachedSignPostBlock> WOOD_SIGN_POST = registerWood("sign_post", (x, id, settings) -> {
        var planks = Identifier.parse(x.name() + "_fence");
        var block = BuiltInRegistries.BLOCK.getValue(planks);
        if (block instanceof FenceBlock) {
            return new AttachedSignPostBlock(BlockBehaviour.Properties.ofFullCopy(block).setId(ResourceKey.create(Registries.BLOCK, id)), block, 4);
        }

        return null;
    });

    public static final Map<DyeColor, SleepingBagBlock> SLEEPING_BAG = registerDye("sleeping_bag", (x, id, settings) -> {
        var bed = Identifier.parse(x.getSerializedName() + "_bed");
        var block = BuiltInRegistries.BLOCK.getValue(bed);
        if (block instanceof BedBlock) {
            return new SleepingBagBlock(x, BlockBehaviour.Properties.ofFullCopy(block).pushReaction(PushReaction.BLOCK).setId(ResourceKey.create(Registries.BLOCK, id)));
        }

        return null;
    });

    public static final Map<Block, AttachedSignPostBlock> WALL_SIGN_POST = Util.make(() -> {
      var map = new HashMap<Block, AttachedSignPostBlock>();
      var l = new ArrayList<Block>();
      for (var b : BuiltInRegistries.BLOCK) {
          if (b instanceof WallBlock && BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
              l.add(b);
          }
      }

      for (var b : l) {
          map.put(b, register(BuiltInRegistries.BLOCK.getKey(b).getPath() + "_sign_post", (s) -> new AttachedSignPostBlock(s, b, 8)));
      }
      return map;
    });

    public static final AttachedSignPostBlock NETHER_BRICK_SIGN_POST = register("nether_brick_sign_post", Blocks.NETHER_BRICK_FENCE,
            (settings, block) -> new AttachedSignPostBlock(settings, block, 4));

    public static final Map<WoodType, MailboxBlock> WOODEN_MAILBOX = registerWood("mailbox", (x, id, settings) -> {
        var planks = Identifier.parse(x.name() + "_planks");
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            var block = BuiltInRegistries.BLOCK.getValue(planks);
            return new MailboxBlock(BlockBehaviour.Properties.ofFullCopy(block).setId(ResourceKey.create(Registries.BLOCK, id)), block);
        }

        return null;
    });

    private static <T extends Block & PolymerBlock> Map<WoodType, T> registerWood(String id, TriFunction<WoodType, Identifier, BlockBehaviour.Properties, T> object) {
        return registerWood("", id, object);
    }
    private static <T extends Block & PolymerBlock> Map<WoodType, T> registerWood(String prefix, String id, TriFunction<WoodType, Identifier, BlockBehaviour.Properties, T> object) {
        var map = new HashMap<WoodType, T>();

        WoodUtil.VANILLA.forEach(x -> {
            var y = register(prefix + x.name() + "_" + id, (s) -> object.apply(x,  id(prefix + x.name() + "_" + id), s));
            if (y != null) {
                map.put(x, y);
            }
        });

        return map;
    }

    private static <T extends Block & PolymerBlock> Map<DyeColor, T> registerDye(String id, TriFunction<DyeColor, Identifier, BlockBehaviour.Properties, T> object) {
        var map = new HashMap<DyeColor, T>();

        for (var x : DyeColor.values()) {
            var y = register( x.getSerializedName() + "_" + id, (s) -> object.apply(x, id(x.getSerializedName() + "_" + id), s));
            if (y != null) {
                map.put(x, y);
            }
        }

        return map;
    }

    public static void register() {
        LongFlowerPotBlock.setupResourcesAndMapping();

        if (ModInit.DEV_MODE) {
            ServerLifecycleEvents.SERVER_STARTED.register((DecorationsBlocks::validateLootTables));
            ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(((server, resourceManager, success) -> {
                validateLootTables(server);
            }));
        }
    }

    public static void forEveryEntry(Consumer<Block> blockConsumer) {
        for (var block : BuiltInRegistries.BLOCK.stream().toList()) {
            blockConsumer.accept(block);
        }
        RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK).register(((rawId, id, object) -> blockConsumer.accept(object)));
    }

    private static void validateLootTables(MinecraftServer server) {
        for (var block : BLOCKS) {
            if (block.getLootTable().isPresent()) {
                var lt = server.reloadableRegistries().getLootTable(block.getLootTable().get());
                if (lt == LootTable.EMPTY) {
                    ModInit.LOGGER.warn("Missing loot table? " + block.getLootTable().get().identifier());
                }
            }
            if (block instanceof EntityBlock provider) {
                var be = provider.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
                assert be == null || be.getType().isValid(block.defaultBlockState());
            }

        }
    }

    public static <T extends Block> T register(String path, Function<BlockBehaviour.Properties, T> function) {
        return register(path, BlockBehaviour.Properties.of(), function);
    }

    public static <T extends Block, Y extends Block> T register(String path, Y copyFrom, BiFunction<BlockBehaviour.Properties, Y, T> function) {
        return register(path, BlockBehaviour.Properties.ofFullCopy(copyFrom), (settings) -> function.apply(settings, copyFrom));
    }

    @NotNull
    public static <T extends Block> T register(String path, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, T> function) {
        var id = Identifier.fromNamespaceAndPath(ModInit.ID, path);
        var item = function.apply(settings.setId(ResourceKey.create(Registries.BLOCK, id)));
        if (item == null) {
            //noinspection DataFlowIssue
            return null;
        }
        BLOCKS.add(item);
        return Registry.register(BuiltInRegistries.BLOCK, id, item);
    }


    public static <Waxed extends Block, Regular extends Block & WeatheringCopper> WeatheringCopperBlocks registerRelativeCopper(String baseId, WeatheringCopperBlocks source,
                                                                                                                  BiFunction<BlockBehaviour.Properties, Block, Waxed> waxedBlockFactory,
                                                                                                                  TriFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, Block, Regular> unwaxedBlockFactory,
                                                                                                                  BiFunction<WeatheringCopper.WeatherState, Block, BlockBehaviour.Properties> settingsFromOxidationLevel) {

        Block unaffected = register(baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.UNAFFECTED, source.unaffected()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.UNAFFECTED, settings, source.unaffected());
        });
        Block exposed = register("exposed_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.EXPOSED, source.exposed()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.EXPOSED, settings, source.exposed());
        });
        Block weathered = register("weathered_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.WEATHERED, source.weathered()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.WEATHERED, settings, source.weathered());
        });
        Block oxidized = register("oxidized_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.OXIDIZED, source.oxidized()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.OXIDIZED, settings, source.oxidized());
        });

        Block unaffectedWaxed = register("waxed_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.UNAFFECTED, source.waxed()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxed());
        });
        Block exposedWaxed = register("waxed_exposed_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.EXPOSED, source.waxedExposed()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxedExposed());
        });
        Block weatheredWaxed = register("waxed_weathered_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.WEATHERED, source.waxedWeathered()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxedWeathered());
        });
        Block oxidizedWaxed = register("waxed_oxidized_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.OXIDIZED, source.waxedOxidized()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxedOxidized());
        });
        return new WeatheringCopperBlocks(unaffected, exposed, weathered, oxidized, unaffectedWaxed, exposedWaxed, weatheredWaxed, oxidizedWaxed);
    }
}
