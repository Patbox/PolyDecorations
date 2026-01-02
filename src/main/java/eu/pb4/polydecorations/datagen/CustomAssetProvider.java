package eu.pb4.polydecorations.datagen;

import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.furniture.TableBlock;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.model.DecorationsModels;
import eu.pb4.polydecorations.ui.UiResourceCreator;
import eu.pb4.polydecorations.util.ResourceUtils;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.ConstantTintSource;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.CustomModelDataTintSource;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelElement;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static eu.pb4.polydecorations.ModInit.id;
import static eu.pb4.polydecorations.util.DecorationsUtil.getValues;

public class CustomAssetProvider implements DataProvider {
    private static final String BASE_WOOD_MODEL_JSON = """
            {
              "parent": "polydecorations:block/base_|TYPE|",
              "textures": {
                "planks": "|PLANKS|",
                "logs": "|LOG|"
              }
            }
            """;
    private static final String MAILBOX_MODEL_JSON = """
            {
              "parent": "polydecorations:block/base_|TYPE|",
              "textures": {
                "front": "|FRONT|",
                "stripped_log": "|STRIPPED_LOG|",
                "stripped_log_top": "|STRIPPED_LOG|_top",
                "log": "|LOG|",
                "log_top": "|LOG|_top"
              }
            }
            """;
    private static final String MAILBOX_BAMBOO_MODEL_JSON = """
            {
              "parent": "polydecorations:block/base_|TYPE|",
              "textures": {
                "front": "|FRONT|",
                "stripped_log": "|STRIPPED_LOG|",
                "stripped_log_top": "|STRIPPED_LOG|",
                "log": "|LOG|",
                "log_top": "|LOG|_top"
              }
            }
            """;
    private static final String STATUE_MODEL_JSON = """
            {
              "parent": "polydecorations:block/statue/stone/|TYPE|",
              "textures": {
                "0": "|TXT|"
              }
            }
            """;
    private static final String STATUE_ITEM_JSON = """
            {
              "parent": "polydecorations:item/stone_statue",
              "textures": {
                "0": "|TXT|"
              }
            }
            """;
    private static final String BASE_STUMP_MODEL_JSON = """
            {
              "parent": "polydecorations:block/base_|TYPE|",
              "textures": {
                "top": "|TOP|",
                "side": "|SIDE|"
              }
            }
            """;

    private static final String BASE_SLEEPING_BAG_JSON = """
            {
              "parent": "polydecorations:block/|TYPE|",
              "textures": {
                "texture": "|TXT|",
                "particle": "|PARTICLE|"
              }
            }
            """;

    private static final List<Map.Entry<String, String>> COPPER_PREFIXES = List.of(
            Map.entry("", "unaffected"),
            Map.entry("exposed_", "exposed"),
            Map.entry("weathered_", "weathered"),
            Map.entry("oxidized_", "oxidized"));

    private final PackOutput output;

    public CustomAssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    public static void writeWoodenBlocksAndItems(BiConsumer<String, byte[]> writer, List<WoodType> types) {
        getValues(DecorationsItems.SHELF, types, (type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_shelf.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "shelf")
                    .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                    .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_shelf_top.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "shelf_top")
                    .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                    .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_shelf_double.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "shelf_double")
                    .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                    .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name().replace(':', '/') + "_shelf")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name().replace(':', '/') + "_shelf")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        getValues(DecorationsItems.STUMP, types, (type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_stump.json", BASE_STUMP_MODEL_JSON
                    .replace("|TYPE|", "stump")
                    .replace("|TOP|", "polydecorations:block/" + WoodUtil.asPath(type) + "_" + WoodUtil.getLogSuffix(type) + "_stump_top")
                    .replace("|SIDE|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name().replace(':', '/') + "_stump")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name().replace(':', '/') + "_stump")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        getValues(DecorationsItems.STRIPPED_STUMP, types, (type, item) -> {
            writer.accept("assets/polydecorations/models/block/stripped_" + type.name().replace(':', '/') + "_stump.json", BASE_STUMP_MODEL_JSON
                    .replace("|TYPE|", "stump")
                    .replace("|TOP|", "polydecorations:block/stripped_" + WoodUtil.asPath(type) + "_" + WoodUtil.getLogSuffix(type) + "_stump_top")
                    .replace("|SIDE|", WoodUtil.getLogName(type).withPrefix("block/stripped_").toString())
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id("stripped_" + type.name().replace(':', '/') + "_stump")),
                    new ItemAsset(new BasicItemModel(id("block/stripped_" + type.name().replace(':', '/') + "_stump")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        getValues(DecorationsItems.BENCH, types, (type, item) -> {
            writeBench(type, writer);

            writer.accept(AssetPaths.itemAsset(id(type.name().replace(':', '/') + "_bench")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name().replace(':', '/') + "_bench")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });


        getValues(DecorationsItems.TABLE, types, (type, block) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_table" + ".json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "table")
                    .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                    .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                    .getBytes(StandardCharsets.UTF_8)
            );

            for (int i = 1; i < TableBlock.TableModel.COUNT; i++) {
                writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_table_" + i + ".json", BASE_WOOD_MODEL_JSON
                        .replace("|TYPE|", "table_" + i)
                        .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                        .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                        .getBytes(StandardCharsets.UTF_8)
                );
            }

            writer.accept(AssetPaths.itemAsset(id(type.name().replace(':', '/') + "_table")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name().replace(':', '/') + "_table")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        getValues(DecorationsItems.WOODEN_STATUE, types, (type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_sign_post.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "sign_post")
                    .replace("|PLANKS|", "polydecorations:block/sign_post_" + type.name().replace(':', '/'))
                    .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name().replace(':', '/') + "_sign_post")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name().replace(':', '/') + "_sign_post")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));

            writeStatue(type.name().replace(':', '/'), Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString(), writer);
        });

        getValues(DecorationsItems.WOODEN_MAILBOX, types, (type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_mailbox.json",
                    (type != WoodType.BAMBOO ? MAILBOX_MODEL_JSON : MAILBOX_BAMBOO_MODEL_JSON)
                            .replace("|TYPE|", "mailbox")
                            .replace("|FRONT|", "polydecorations:block/mailbox_front_" + WoodUtil.asPath(type))
                            .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                            .replace("|STRIPPED_LOG|", WoodUtil.getLogName(type).withPrefix("block/stripped_").toString())
                            .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name().replace(':', '/') + "_mailbox")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name().replace(':', '/') + "_mailbox")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        getValues(DecorationsItems.TOOL_RACK, types, (type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_tool_rack.json",
                    BASE_WOOD_MODEL_JSON
                            .replace("|TYPE|", "tool_rack")
                            .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                            .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                            .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name().replace(':', '/') + "_tool_rack")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name().replace(':', '/') + "_tool_rack")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });
    }

    public static void createWoodTextures(AtlasAsset.Builder blockAtlas, BiConsumer<String, byte[]> assetWriter, FileReader fileReader, List<WoodType> types) throws Exception {
        var jar = PolymerCommonUtils.getClientJarRoot();
        var b = new ByteArrayOutputStream();

        blockAtlas.palettedPermutations(id("palette/wood/oak"), c -> {
            c.texture(id("block/sign_post"));
            c.texture(id("block/mailbox_front"));
            for (var type : types) {
                if (type == WoodType.BAMBOO) continue;

                c.permutation(type.name().replace(':', '/'), id("palette/wood/" + type.name().replace(':', '/')));
            }
        });


        { // Palette
            var oakPlanks = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/block/oak_planks.png")));
            var positions = new ArrayList<int[]>();
            {
                var existingColors = new IntOpenHashSet();
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        var rgb = oakPlanks.getRGB(x, y);
                        if (existingColors.add(rgb)) {
                            positions.add(new int[]{x, y});
                        }
                    }
                }
            }

            var palette = new BufferedImage(positions.size(), 1, BufferedImage.TYPE_INT_RGB);

            for (var wood : types) {
                try {
                    var id = Identifier.parse(wood.name());

                    var input = ImageIO.read(fileReader.apply("assets/" + id.getNamespace() + "/textures/block/" + id.getPath() + "_planks.png"));
                    for (int i = 0; i < positions.size(); i++) {
                        var pos = positions.get(i);
                        palette.setRGB(i, 0, input.getRGB(pos[0], pos[1]));
                    }
                    ImageIO.write(palette, "png", b);
                    assetWriter.accept("assets/polydecorations/textures/palette/wood/" + wood.name().replace(':', '/') + ".png", b.toByteArray());
                    b.reset();
                } catch (Throwable e) {
                    ModInit.LOGGER.warn("Failed to find planks texture for '" + wood.name() + "'", e);
                }

            }
        }

        // Stump top
        for (var prefix : List.of("", "stripped_")) {
            for (var wood : types) {
                try {
                    var id = Identifier.parse(wood.name());
                    var source = ImageIO.read(fileReader.apply("assets/" + id.getNamespace() + "/textures/block/" + prefix + WoodUtil.getLogName(wood).getPath() + "_top.png"));
                    var texture = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
                    var frames = source.getHeight() / source.getWidth();

                    var scale = source.getWidth() / 16f;

                    var three = (int) (3 * scale);
                    var threePlusFive = (int) ((3 + 5) * scale);
                    var eleven = (int) (11 * scale);
                    var five = Mth.ceil(5 * scale);

                    for (int i = 0; i < frames; i++) {
                        var offset = i * source.getWidth();
                        for (int x = 0; x < five; x++) {
                            for (int y = 0; y < five; y++) {
                                texture.setRGB(x + three, y + three + offset, source.getRGB(x, y + offset));
                                texture.setRGB(x + threePlusFive, y + three + offset, source.getRGB(eleven + x, y + offset));
                                texture.setRGB(x + three, y + threePlusFive + offset, source.getRGB(x, eleven + y + offset));
                                texture.setRGB(x + threePlusFive, y + threePlusFive + offset, source.getRGB(eleven + x, eleven + y + offset));
                            }
                        }
                    }

                    ImageIO.write(texture, "png", b);
                    assetWriter.accept("assets/polydecorations/textures/block/" + prefix + WoodUtil.asPath(wood) + "_" + WoodUtil.getLogSuffix(wood) + "_stump_top.png", b.toByteArray());

                    var mcMeta = jar.resolve("assets/" + id.getNamespace() + "/textures/block/" + prefix + WoodUtil.getLogName(wood).getPath() + "_top.png.mcmeta");
                    if (Files.exists(mcMeta)) {
                        assetWriter.accept("assets/polydecorations/textures/block/" + prefix + WoodUtil.asPath(wood) + "_" + WoodUtil.getLogSuffix(wood) + "_stump_top.png.mcmeta",
                                Files.readAllBytes(mcMeta));
                    }
                    b.reset();
                } catch (Throwable e) {
                    ModInit.LOGGER.warn("Failed to find file for '" + wood.name() + "'", e);
                }
            }
        }
    }

    private static void writeBench(WoodType name, BiConsumer<String, byte[]> writer) {
        writeBenchSet(name, "", writer);
        writeBenchSet(name, "_norest", writer);
    }

    private static void writeBenchSet(WoodType type, String suffix, BiConsumer<String, byte[]> writer) {
        writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_bench" + suffix + ".json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix)
                .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_bench" + suffix + "_left.json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_left")
                .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_bench" + suffix + "_right.json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_right")
                .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name().replace(':', '/') + "_bench" + suffix + "_middle.json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_middle")
                .replace("|PLANKS|", Identifier.parse(type.name()).withPrefix("block/").withSuffix("_planks").toString())
                .replace("|LOG|", WoodUtil.getLogName(type).withPrefix("block/").toString())
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    private static void writeStatue(String type, String texture, BiConsumer<String, byte[]> writer) {
        for (var x : List.of("head", "body", "left_leg", "right_leg", "left_arm", "right_arm")) {
            writer.accept("assets/polydecorations/models/block/statue/" + type + "/" + x + ".json", STATUE_MODEL_JSON
                    .replace("|TYPE|", x)
                    .replace("|TXT|", texture)
                    .getBytes(StandardCharsets.UTF_8)
            );
        }

        writer.accept("assets/polydecorations/models/item/" + type + "_statue.json", STATUE_ITEM_JSON
                .replace("|TXT|", texture)
                .getBytes(StandardCharsets.UTF_8)
        );

        writer.accept(AssetPaths.itemAsset(id(type + "_statue")),
                new ItemAsset(new BasicItemModel(id("item/" + type + "_statue")), ItemAsset.Properties.DEFAULT)
                        .toJson().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        BiConsumer<String, byte[]> assetWriter = (path, data) -> {
            try {
                writer.writeIfNeeded(this.output.getOutputFolder().resolve(path), data, HashCode.fromBytes(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return CompletableFuture.runAsync(() -> {
            try {
                var blockAtlas = AtlasAsset.builder();
                createWoodTextures(blockAtlas, assetWriter, ResourceUtils::getJarStream, WoodUtil.VANILLA);
                createBedPalette(assetWriter);
                createCopperBarPalette(assetWriter);
                UiResourceCreator.generateAssets(assetWriter);
                writeBlocksAndItems(assetWriter);
                assetWriter.accept("assets/minecraft/atlases/blocks.json", blockAtlas.build().toBytes());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }, Util.backgroundExecutor());
    }

    private void writeBlocksAndItems(BiConsumer<String, byte[]> writer) {
        writeWoodenBlocksAndItems(writer, WoodUtil.VANILLA);

        DecorationsItems.SLEEPING_BAG.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.getSerializedName() + "_sleeping_bag.json",
                    BASE_SLEEPING_BAG_JSON
                            .replace("|TYPE|", "base_sleeping_bag")
                            .replace("|TXT|", "polydecorations:block/sleeping_bag_" + type.getSerializedName())
                            .replace("|PARTICLE|", "minecraft:block/" + type.getSerializedName() + "_wool")
                            .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.getSerializedName() + "_sleeping_bag")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.getSerializedName() + "_sleeping_bag")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        try {
            writeBaseTable(writer);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        writer.accept(AssetPaths.itemAsset(id("stone_statue")),
                new ItemAsset(new BasicItemModel(id("item/stone_statue")), ItemAsset.Properties.DEFAULT)
                        .toJson().getBytes(StandardCharsets.UTF_8));


        writeStatue("deepslate", "block/deepslate_top", writer);
        writeStatue("blackstone", "block/blackstone", writer);
        writeStatue("prismarine", "block/prismarine", writer);
        writeStatue("sandstone", "block/sandstone_top", writer);
        writeStatue("red_sandstone", "block/red_sandstone_top", writer);
        writeStatue("quartz", "block/quartz_block_bottom", writer);
        writeStatue("tuff", "block/tuff", writer);
        writeStatue("stone_bricks", "block/stone_bricks", writer);
        writeStatue("tuff_bricks", "block/tuff_bricks", writer);
        writeStatue("packed_mud", "block/packed_mud", writer);
        writeStatue("granite", "block/granite", writer);
        writeStatue("andesite", "block/andesite", writer);
        writeStatue("diorite", "block/diorite", writer);
        writeStatue("terracotta", "block/terracotta", writer);

        //var b = new StringBuilder();

        for (var color : DyeColor.values()) {
            writeStatue(color.getSerializedName() + "_terracotta", "block/" + color.getSerializedName() + "_terracotta", writer);
            writeStatue(color.getSerializedName() + "_wool", "block/" + color.getSerializedName() + "_wool", writer);

            //b.append('"').append("item.polydecorations.").append(color.getName()).append("_terracotta_statue\": \"")
            //        .append(Character.toUpperCase(color.getName().charAt(0))).append(color.getName().substring(1)).append(" Terracotta Statue\",\n");
        }

        //System.out.println(b);

        DecorationsModels.ROPE.generateModels(writer);

        generateWindChimeModels(writer);

        for (var item : List.of(DecorationsItems.CANVAS, DecorationsItems.ROPE, DecorationsItems.GLOBE, DecorationsItems.GHOST_LIGHT,
                DecorationsItems.BURNING_GHOST_LIGHT, DecorationsItems.COPPER_GHOST_LIGHT,
                DecorationsItems.TRASHCAN, DecorationsItems.HAMMER, DecorationsItems.TROWEL, DecorationsItems.COPPER_CAMPFIRE)) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            writer.accept(AssetPaths.itemAsset(id),
                    new ItemAsset(new BasicItemModel(id.withPrefix("item/")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        }

        for (var item : List.of(DecorationsItems.DISPLAY_CASE, DecorationsItems.BRAZIER, DecorationsItems.SOUL_BRAZIER, DecorationsItems.COPPER_BRAZIER, DecorationsItems.LARGE_FLOWER_POT, DecorationsItems.LONG_FLOWER_POT)) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            writer.accept(AssetPaths.itemAsset(id),
                    new ItemAsset(new BasicItemModel(id.withPrefix("block/")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        }

        writer.accept(AssetPaths.itemAsset(id("basket")),
                new ItemAsset(new BasicItemModel(id("block/basket_open")), ItemAsset.Properties.DEFAULT)
                        .toJson().getBytes(StandardCharsets.UTF_8));
        writer.accept(AssetPaths.itemAsset(id("cardboard_box")),
                new ItemAsset(new BasicItemModel(id("block/cardboard_box_closed")), ItemAsset.Properties.DEFAULT)
                        .toJson().getBytes(StandardCharsets.UTF_8));
    }

    private void createCopperBarPalette(BiConsumer<String, byte[]> assetWriter) throws Exception {
        var jar = PolymerCommonUtils.getClientJarRoot();
        var b = new ByteArrayOutputStream();

        { // Palette

            var positions = new ArrayList<int[]>();
            {
                var ironBars = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/block/iron_bars.png")));
                var existingColors = new IntOpenHashSet();
                for (int x = 0; x < ironBars.getWidth(); x++) {
                    for (int y = 0; y < ironBars.getHeight(); y++) {
                        var rgb = ironBars.getRGB(x, y);
                        if (existingColors.add(rgb)) {
                            positions.add(new int[]{x, y});
                        }
                    }
                }
            }

            var palette = new BufferedImage(positions.size(), 1, BufferedImage.TYPE_INT_RGB);

            for (var type : COPPER_PREFIXES) {
                var input = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/block/" + type.getKey() + "copper_bars.png")));
                for (int i = 0; i < positions.size(); i++) {
                    var pos = positions.get(i);
                    palette.setRGB(i, 0, input.getRGB(pos[0], pos[1]));
                }
                ImageIO.write(palette, "png", b);
                assetWriter.accept("assets/polydecorations/textures/palette/copper_bars/" + type.getValue() + ".png", b.toByteArray());
                b.reset();
            }
        }
    }

    private void createBedPalette(BiConsumer<String, byte[]> assetWriter) throws Exception {
        var jar = PolymerCommonUtils.getClientJarRoot();
        var b = new ByteArrayOutputStream();

        { // Palette

            var positions = new ArrayList<int[]>();
            {
                var base = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/entity/bed/red.png")));
                var existingColors = new IntOpenHashSet();
                var oakPlanks = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/block/oak_planks.png")));

                for (int x = 0; x < oakPlanks.getWidth(); x++) {
                    for (int y = 0; y < oakPlanks.getHeight(); y++) {
                        existingColors.add(oakPlanks.getRGB(x, y));
                    }
                }
                for (int x = 0; x < base.getWidth(); x++) {
                    for (int y = 0; y < base.getHeight(); y++) {
                        var rgb = base.getRGB(x, y);
                        if (existingColors.add(rgb)) {
                            positions.add(new int[]{x, y});
                        }
                    }
                }
            }

            var palette = new BufferedImage(positions.size(), 1, BufferedImage.TYPE_INT_RGB);

            for (var color : DyeColor.values()) {
                var input = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/entity/bed/" + color.getSerializedName() + ".png")));
                for (int i = 0; i < positions.size(); i++) {
                    var pos = positions.get(i);
                    palette.setRGB(i, 0, input.getRGB(pos[0], pos[1]));
                }
                ImageIO.write(palette, "png", b);
                assetWriter.accept("assets/polydecorations/textures/palette/bed_color/" + color.getSerializedName() + ".png", b.toByteArray());
                b.reset();
            }
        }
    }

    private void generateWindChimeModels(BiConsumer<String, byte[]> writer) {
        writer.accept(AssetPaths.itemAsset(id("wind_chime")),
                new ItemAsset(new BasicItemModel(id("block/wind_chime"), List.of(
                        new ConstantTintSource(0xFFFFFF),
                        new CustomModelDataTintSource(0, 0xFFFFFF),
                        new CustomModelDataTintSource(1, 0xFFFFFF),
                        new CustomModelDataTintSource(2, 0xFFFFFF),
                        new CustomModelDataTintSource(3, 0xFFFFFF),
                        new CustomModelDataTintSource(4, 0xFFFFFF)
                )), ItemAsset.Properties.DEFAULT)
                        .toJson().getBytes(StandardCharsets.UTF_8));

        var model = ResourceUtils.getModel(id("block/wind_chime"));
        var decoded = ModelAsset.CODEC.decode(JsonOps.INSTANCE, model).getOrThrow().getFirst();
        var groups = BlockbenchGroup.CODEC.decode(JsonOps.INSTANCE, model).getOrThrow().getFirst();

        var base = ModelAsset.builder();
        var chimes = new ModelAsset.Builder[5];
        //noinspection unchecked
        List<ModelElement>[] chimesElement = new List[5];
        for (int i = 0; i < 5; i++) {
            chimes[i] = ModelAsset.builder();
            chimesElement[i] = new ArrayList<>();
            decoded.textures().forEach(chimes[i]::texture);
        }

        decoded.textures().forEach(base::texture);

        var el = decoded.elements().orElseThrow();

        outerLoop:
        for (var e = 0; e < el.size(); e++) {
            for (var g : groups) {
                if (g.children().contains(e)) {
                    chimesElement[Integer.parseInt(g.name().substring("chime_".length()))].add(el.get(e));
                    continue outerLoop;
                }
            }
            base.element(el.get(e));
        }
        var chimeOffset = new ArrayList<Vec3>();

        for (int i = 0; i < 5; i++) {
            chimeOffset.add(chimesElement[i].getFirst().rotation().orElseThrow().origin());
            for (var e : chimesElement[i]) {
                var offset = e.rotation().orElseThrow().origin().reverse().add(8, 8, 8);
                var map = new EnumMap<Direction, ModelElement.Face>(Direction.class);

                for (var face : e.faces().entrySet()) {
                    map.put(face.getKey(), face.getValue().tintIndex() > -1
                            ? new ModelElement.Face(face.getValue().uv(), face.getValue().texture(), face.getValue().cullface(), face.getValue().rotation(), 0)
                            : face.getValue());
                }

                chimes[i].element(new ModelElement(e.from().add(offset), e.to().add(offset), map));
            }
            writer.accept(AssetPaths.blockModel(id("wind_chime/chime_" + i)), chimes[i].build().toBytes());

            writer.accept(AssetPaths.itemAsset(id("-/block/wind_chime/chime_" + i)),
                    new ItemAsset(new BasicItemModel(id("block/wind_chime/chime_" + i), List.of(
                            new CustomModelDataTintSource(0, 0xFFFFFF)
                    )), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));

        }

        writer.accept("wind_chime_offsets.json", Vec3.CODEC.listOf().encodeStart(JsonOps.INSTANCE, chimeOffset).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));

        writer.accept(AssetPaths.blockModel(id("wind_chime/base")), base.build().toBytes());

        writer.accept(AssetPaths.itemAsset(id("wind_chime")),
                new ItemAsset(new BasicItemModel(id("block/wind_chime"), List.of(
                        new ConstantTintSource(0xFFFFFF),
                        new CustomModelDataTintSource(0, 0xFFFFFF),
                        new CustomModelDataTintSource(1, 0xFFFFFF),
                        new CustomModelDataTintSource(2, 0xFFFFFF),
                        new CustomModelDataTintSource(3, 0xFFFFFF),
                        new CustomModelDataTintSource(4, 0xFFFFFF)
                )), ItemAsset.Properties.DEFAULT)
                        .toJson().getBytes(StandardCharsets.UTF_8));


    }

    private void writeBaseTable(BiConsumer<String, byte[]> writer) throws IOException {
        var json = JsonParser.parseString(new String(Objects.requireNonNull(ResourceUtils.getJarData("assets/polydecorations/models/block/base_table.json"))));

        for (int i = 1; i < TableBlock.TableModel.COUNT; i++) {
            var corners = TableBlock.TableModel.toCornerNames(i);
            var obj = json.deepCopy().getAsJsonObject();
            var newElements = new JsonArray();
            var elements = obj.getAsJsonArray("elements");
            for (var el : elements) {
                if (!el.getAsJsonObject().has("name") || corners.contains(el.getAsJsonObject().get("name").getAsString())) {
                    newElements.add(el);
                }
            }
            obj.add("elements", newElements);

            writer.accept("assets/polydecorations/models/block/base_table_" + i + ".json", obj.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public String getName() {
        return "polydecorations:assets";
    }

    @FunctionalInterface
    public interface FileReader {
        InputStream apply(String s) throws Exception;
    }
}
