package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.DecorationsFeature;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.extension.AttachedSignPostBlock;
import eu.pb4.polydecorations.block.extension.WallAttachedLanternBlock;
import eu.pb4.polydecorations.block.extension.WallAttachedOxidizableLanternBlock;
import eu.pb4.polydecorations.block.furniture.*;
import eu.pb4.polydecorations.block.item.*;
import eu.pb4.polydecorations.block.other.GhostLightBlock;
import eu.pb4.polydecorations.block.other.RopeBlock;
import eu.pb4.polydecorations.util.DecorationsSoundEvents;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.commons.lang3.function.TriFunction;

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
    public static final WallAttachedLanternBlock WALL_LANTERN = DecorationsFeature.WALL_LANTERNS.isRegistered() ? register("wall_lantern", (LanternBlock) Blocks.LANTERN, WallAttachedLanternBlock::new) : null;
    public static final WallAttachedLanternBlock WALL_SOUL_LANTERN = DecorationsFeature.WALL_LANTERNS.isRegistered() ? register("wall_soul_lantern", (LanternBlock) Blocks.SOUL_LANTERN, WallAttachedLanternBlock::new) : null;

    public static WeatheringCopperCollection<Block> WALL_COPPER_LANTERNS = DecorationsFeature.WALL_LANTERNS.isRegistered() ? registerRelativeCopper("copper_wall_lantern", Blocks.COPPER_LANTERN,
            (settings, block) -> new WallAttachedLanternBlock(settings, (LanternBlock) block),
            (level, settings, block) -> new WallAttachedOxidizableLanternBlock(settings, (WeatheringLanternBlock) block),
            (level, block) -> BlockBehaviour.Properties.ofFullCopy(block)
    ) : null;

    public static final BrazierBlock BRAZIER = DecorationsFeature.BRAZIER.isRegistered() ? register("brazier", Blocks.LANTERN, (settings, ignored) -> new BrazierBlock(settings.noOcclusion().lightLevel(x -> {
                return x.getValue(BrazierBlock.LIT) ? Blocks.CAMPFIRE.defaultBlockState().getLightEmission() : 0;
            }))

    ) : null;
    public static final BrazierBlock SOUL_BRAZIER = DecorationsFeature.BRAZIER.isRegistered() ? register("soul_brazier", Blocks.SOUL_LANTERN, (settings, ignored) -> new BrazierBlock(settings.noOcclusion().lightLevel(x -> {
                return x.getValue(BrazierBlock.LIT) ? Blocks.SOUL_CAMPFIRE.defaultBlockState().getLightEmission() : 0;
            }))
    ) : null;

    public static final BrazierBlock COPPER_BRAZIER = DecorationsFeature.BRAZIER.isRegistered() ? register("copper_brazier", Blocks.COPPER_LANTERN.weathering().unaffected(), (settings, ignored) -> new BrazierBlock(settings.noOcclusion().lightLevel(x -> {
                return x.getValue(BrazierBlock.LIT) ? Blocks.CAMPFIRE.defaultBlockState().getLightEmission() : 0;
            }))
    ) : null;

    public static final PolymerCampfireBlock COPPER_CAMPFIRE = DecorationsFeature.COPPER_CAMPFIRE.isRegistered() ? register("copper_campfire", Blocks.CAMPFIRE, (settings, block) -> new PolymerCampfireBlock(true, 1, settings)) : null;

    public static final GlobeBlock GLOBE = DecorationsFeature.GLOBE.isRegistered() ? register("globe", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion(), GlobeBlock::new) : null;
    public static final RopeBlock ROPE = DecorationsFeature.ROPE.isRegistered() ? register("rope", BlockBehaviour.Properties.of().strength(1f).sound(SoundType.COBWEB).instabreak().noOcclusion(), RopeBlock::new) : null;
    public static final DisplayCaseBlock DISPLAY_CASE = DecorationsFeature.DISPLAY_CASE.isRegistered() ? register("display_case", BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion(), DisplayCaseBlock::new) : null;
    public static final WindChimeBlock WIND_CHIME = DecorationsFeature.WIND_CHIME.isRegistered() ? register("wind_chime", BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion(), WindChimeBlock::new) : null;
    public static final TrashCanBlock TRASHCAN = DecorationsFeature.TRASHCAN.isRegistered() ? register("trashcan", settings -> new TrashCanBlock(settings
            .mapColor(MapColor.METAL).strength(3.5F).sound(SoundType.LANTERN).noOcclusion())) : null;

    public static final PickableItemContainerBlock BASKET = DecorationsFeature.BASKET.isRegistered() ? register("basket", settings -> new BasketBlock(settings
            .mapColor(MapColor.WOOD).strength(0.5F)
            .ignitedByLava()
            .sound(SoundType.SCAFFOLDING).noOcclusion())) : null;

    public static final PickableItemContainerBlock CARDBOARD_BOX = DecorationsFeature.CARDBOARD_BOX.isRegistered() ? register("cardboard_box", settings -> new PickableItemContainerBlock(settings
            .mapColor(MapColor.WOOD).strength(0.4F)
            .ignitedByLava()
            .sound(DecorationsSoundEvents.CARDBOARD).noOcclusion(), DecorationsSoundEvents.CARDBOARD_BOX_OPEN, DecorationsSoundEvents.CARDBOARD_BOX_CLOSE)) : null;

    public static final LargeFlowerPotBlock LARGE_FLOWER_POT = DecorationsFeature.FLOWER_POTS.isRegistered() ? register("large_flower_pot", settings -> new LargeFlowerPotBlock(settings
            .mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.25F).noOcclusion())) : null;

    public static final LongFlowerPotBlock LONG_FLOWER_POT = DecorationsFeature.FLOWER_POTS.isRegistered() ? register("long_flower_pot", BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE).instabreak().noOcclusion(), LongFlowerPotBlock::new) : null;

    public static final GhostLightBlock GHOST_LIGHT = DecorationsFeature.GHOST_LIGHTS.isRegistered() ? register("ghost_light",
            settings -> new GhostLightBlock(settings.noOcclusion()
                    .noCollision().instabreak().lightLevel(x -> 7), 5, 1, 0.001f, ParticleTypes.SOUL_FIRE_FLAME)) : null;

    public static final GhostLightBlock BURNING_GHOST_LIGHT = DecorationsFeature.GHOST_LIGHTS.isRegistered() ? register("burning_ghost_light", BlockBehaviour.Properties.ofFullCopy(GHOST_LIGHT),
            settings -> new GhostLightBlock(settings.lightLevel(x -> 9), 5, 1, 0.001f, ParticleTypes.FLAME)) : null;

    public static final GhostLightBlock COPPER_GHOST_LIGHT = DecorationsFeature.GHOST_LIGHTS.isRegistered() ? register("copper_ghost_light", BlockBehaviour.Properties.ofFullCopy(GHOST_LIGHT),
            settings -> new GhostLightBlock(settings.lightLevel(x -> 9), 5, 1, 0.001f, ParticleTypes.COPPER_FIRE_FLAME)) : null;

    public static final Map<WoodType, PlainShelfBlock> SHELF = DecorationsFeature.SHELF.isRegistered() ? registerWood("shelf", (x, id, settings) -> {
        var planks = WoodUtil.getPlanksId(x);
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new PlainShelfBlock(
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never), BuiltInRegistries.BLOCK.getValue(planks), id
            );
        }

        return null;
    }) : Map.of();

    public static final Map<WoodType, BenchBlock> BENCH = DecorationsFeature.BENCH.isRegistered() ? registerWood("bench", (x, id, settings) -> {
        var planks = WoodUtil.getPlanksId(x);
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new BenchBlock(
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    id,
                    BuiltInRegistries.BLOCK.getValue(planks)
            );
        }

        return null;
    }) : Map.of();

    public static final Map<WoodType, ToolRackBlock> TOOL_RACK = DecorationsFeature.TOOL_RACK.isRegistered() ? registerWood("tool_rack", (x, id, settings) -> {
        var planks = WoodUtil.getPlanksId(x);
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new ToolRackBlock(
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    BuiltInRegistries.BLOCK.getValue(planks)
            );
        }

        return null;
    }) : Map.of();

    public static final Map<WoodType, TableBlock> TABLE = DecorationsFeature.TABLE.isRegistered() ? registerWood("table", (x, id, settings) -> {
        var planks = WoodUtil.getPlanksId(x);
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            return new TableBlock(id,
                    BlockBehaviour.Properties.ofFullCopy(BuiltInRegistries.BLOCK.getValue(planks)).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    BuiltInRegistries.BLOCK.getValue(planks)
            );
        }

        return null;
    }) : Map.of();

    public static final Map<WoodType, StumpBlock> STUMP = DecorationsFeature.STUMP.isRegistered() ? registerWood("stump", (x, id, settings) -> {
        var log = WoodUtil.getLogId(x);

        if (WoodUtil.hasLog(x) && BuiltInRegistries.BLOCK.containsKey(log)) {
            var logBlock = BuiltInRegistries.BLOCK.getValue(log);

            return new StumpBlock(
                    BlockBehaviour.Properties.ofFullCopy(logBlock).mapColor(logBlock.defaultMapColor()).setId(ResourceKey.create(Registries.BLOCK, id)).noOcclusion()
                            .isRedstoneConductor(Blocks::never),
                    logBlock
            );
        }

        return null;
    }) : Map.of();

    public static final Map<WoodType, StumpBlock> STRIPPED_STUMP = DecorationsFeature.STUMP.isRegistered() ? registerWood("stripped_", "stump", (x, id, settings) -> {
        var log = WoodUtil.getStrippedLogId(x);

        if (!log.equals(WoodUtil.getLogId(x)) && WoodUtil.hasLog(x) && BuiltInRegistries.BLOCK.containsKey(log)) {
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
    }) : Map.of();

    public static final Map<WoodType, AttachedSignPostBlock> WOOD_SIGN_POST = DecorationsFeature.SIGN_POST.isRegistered() ? registerWood("sign_post", (x, id, settings) -> {
        var planks = WoodUtil.getFenceId(x);
        var block = BuiltInRegistries.BLOCK.getValue(planks);
        if (block instanceof FenceBlock) {
            return new AttachedSignPostBlock(BlockBehaviour.Properties.ofFullCopy(block).setId(ResourceKey.create(Registries.BLOCK, id)), block, 4);
        }

        return null;
    }) : Map.of();

    public static final Map<DyeColor, SleepingBagBlock> SLEEPING_BAG = DecorationsFeature.SLEEPING_BAG.isRegistered() ? registerDye("sleeping_bag", (x, id, settings) -> {
        var bed = Identifier.parse(x.getSerializedName() + "_bed");
        var block = BuiltInRegistries.BLOCK.getValue(bed);
        if (block instanceof BedBlock) {
            return new SleepingBagBlock(x, BlockBehaviour.Properties.ofFullCopy(block).pushReaction(PushReaction.IMMOVEABLE).setId(ResourceKey.create(Registries.BLOCK, id)));
        }

        return null;
    }) : Map.of();

    public static final Map<Block, AttachedSignPostBlock> WALL_SIGN_POST = Util.make(() -> {
      var map = new HashMap<Block, AttachedSignPostBlock>();
      if (!DecorationsFeature.SIGN_POST.isRegistered()) {
          return map;
      }
      var l = new ArrayList<Block>();
      for (var b : BuiltInRegistries.BLOCK) {
          if (b instanceof WallBlock && BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
              l.add(b);
          }
      }

      for (var b : l) {
           map.put(b, register(BuiltInRegistries.BLOCK.getKey(b).getPath() + "_sign_post", b,
                   (settings, block) -> new AttachedSignPostBlock(settings, block, 8)));
      }
      return map;
    });

    public static final AttachedSignPostBlock NETHER_BRICK_SIGN_POST = DecorationsFeature.SIGN_POST.isRegistered() ? register("nether_brick_sign_post", Blocks.NETHER_BRICK_FENCE,
            (settings, block) -> new AttachedSignPostBlock(settings, block, 4)) : null;

    public static final Map<WoodType, MailboxBlock> WOODEN_MAILBOX = DecorationsFeature.MAILBOX.isRegistered() ? registerWood("mailbox", (x, id, settings) -> {
        var planks = WoodUtil.getPlanksId(x);
        if (BuiltInRegistries.BLOCK.containsKey(planks)) {
            var block = BuiltInRegistries.BLOCK.getValue(planks);
            return new MailboxBlock(BlockBehaviour.Properties.ofFullCopy(block).setId(ResourceKey.create(Registries.BLOCK, id)), block);
        }

        return null;
    }) : Map.of();

    private static <T extends Block & PolymerBlock> Map<WoodType, T> registerWood(String id, TriFunction<WoodType, Identifier, BlockBehaviour.Properties, T> object) {
        return registerWood("", id, object);
    }
    private static <T extends Block & PolymerBlock> Map<WoodType, T> registerWood(String prefix, String id, TriFunction<WoodType, Identifier, BlockBehaviour.Properties, T> object) {
        var map = new HashMap<WoodType, T>();

        WoodUtil.registerVanillaAndWaitForModded(x -> {
            var y = register(prefix + x.name().replace(':', '/') + "_" + id, (s) -> object.apply(x,  id(prefix + x.name().replace(':', '/') + "_" + id), s));
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
        if (DecorationsFeature.FLOWER_POTS.isRegistered()) {
            LongFlowerPotBlock.setupResourcesAndMapping();
        }

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


    public static <Waxed extends Block, Regular extends Block & WeatheringCopper> WeatheringCopperCollection<Block> registerRelativeCopper(String baseId, WeatheringCopperCollection<Block> source,
                                                                                                                  BiFunction<BlockBehaviour.Properties, Block, Waxed> waxedBlockFactory,
                                                                                                                  TriFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, Block, Regular> unwaxedBlockFactory,
                                                                                                                  BiFunction<WeatheringCopper.WeatherState, Block, BlockBehaviour.Properties> settingsFromOxidationLevel) {

        Block unaffected = register(baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.UNAFFECTED, source.weathering().unaffected()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.UNAFFECTED, settings, source.weathering().unaffected());
        });
        Block exposed = register("exposed_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.EXPOSED, source.weathering().exposed()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.EXPOSED, settings, source.weathering().exposed());
        });
        Block weathered = register("weathered_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.WEATHERED, source.weathering().weathered()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.WEATHERED, settings, source.weathering().weathered());
        });
        Block oxidized = register("oxidized_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.OXIDIZED, source.weathering().oxidized()), (settings) -> {
            return unwaxedBlockFactory.apply(WeatheringCopper.WeatherState.OXIDIZED, settings, source.weathering().oxidized());
        });

        Block unaffectedWaxed = register("waxed_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.UNAFFECTED, source.waxed().unaffected()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxed().unaffected());
        });
        Block exposedWaxed = register("waxed_exposed_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.EXPOSED, source.waxed().exposed()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxed().exposed());
        });
        Block weatheredWaxed = register("waxed_weathered_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.WEATHERED, source.waxed().weathered()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxed().weathered());
        });
        Block oxidizedWaxed = register("waxed_oxidized_" + baseId, settingsFromOxidationLevel.apply(WeatheringCopper.WeatherState.OXIDIZED, source.waxed().oxidized()), (settings) -> {
            return waxedBlockFactory.apply(settings, source.waxed().oxidized());
        });
        return new WeatheringCopperCollection<>(new WeatheringCopperCollection.ByState<>(unaffected, exposed, weathered, oxidized),
                new WeatheringCopperCollection.ByState<>(unaffectedWaxed, exposedWaxed, weatheredWaxed, oxidizedWaxed));
    }
}
