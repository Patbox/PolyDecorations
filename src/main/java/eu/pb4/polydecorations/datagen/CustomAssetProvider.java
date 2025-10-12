package eu.pb4.polydecorations.datagen;

import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import eu.pb4.polydecorations.block.furniture.TableBlock;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.model.DecorationsModels;
import eu.pb4.polydecorations.ui.UiResourceCreator;
import eu.pb4.polydecorations.util.ResourceUtils;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.ConstantTintSource;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.CustomModelDataTintSource;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelElement;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.WoodType;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static eu.pb4.polydecorations.ModInit.id;

class CustomAssetProvider implements DataProvider {
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

    private final DataOutput output;

    public CustomAssetProvider(FabricDataOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        BiConsumer<String, byte[]> assetWriter = (path, data) -> {
            try {
                writer.write(this.output.getPath().resolve(path), data, HashCode.fromBytes(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return CompletableFuture.runAsync(() -> {
            try {
                createWoodTextures(assetWriter);
                createBedPalette(assetWriter);
                UiResourceCreator.generateAssets(assetWriter);
                writeBlocksAndItems(assetWriter);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }, Util.getMainWorkerExecutor());
    }

    private void writeBlocksAndItems(BiConsumer<String, byte[]> writer) {
        var t = new StringBuilder();
        DecorationsItems.SHELF.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_shelf.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "shelf")
                    .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/block/" + type.name() + "_shelf_top.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "shelf_top")
                    .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/block/" + type.name() + "_shelf_double.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "shelf_double")
                    .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name() + "_shelf")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name() + "_shelf")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        DecorationsItems.STUMP.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_stump.json", BASE_STUMP_MODEL_JSON
                    .replace("|TYPE|", "stump")
                    .replace("|TOP|", "polydecorations:block/" + WoodUtil.getLogName(type) + "_stump_top")
                    .replace("|SIDE|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name() + "_stump")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name() + "_stump")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        DecorationsItems.STRIPPED_STUMP.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/stripped_" + type.name() + "_stump.json", BASE_STUMP_MODEL_JSON
                    .replace("|TYPE|", "stump")
                    .replace("|TOP|", "polydecorations:block/stripped_" + WoodUtil.getLogName(type) + "_stump_top")
                    .replace("|SIDE|", "minecraft:block/stripped_" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id("stripped_" + type.name() + "_stump")),
                    new ItemAsset(new BasicItemModel(id("block/stripped_" + type.name() + "_stump")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        DecorationsItems.BENCH.forEach((type, item) -> {
            writeBench(type, writer);

            writer.accept(AssetPaths.itemAsset(id(type.name() + "_bench")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name() + "_bench")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });


        DecorationsItems.TABLE.forEach((type, block) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_table" + ".json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "table")
                    .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            for (int i = 1; i < TableBlock.TableModel.COUNT; i++) {
                writer.accept("assets/polydecorations/models/block/" + type.name() + "_table_" + i + ".json", BASE_WOOD_MODEL_JSON
                        .replace("|TYPE|", "table_" + i)
                        .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                        .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                        .getBytes(StandardCharsets.UTF_8)
                );
            }

            writer.accept(AssetPaths.itemAsset(id(type.name() + "_table")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name() + "_table")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        DecorationsItems.WOODEN_STATUE.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_sign_post.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "sign_post")
                    .replace("|PLANKS|", "polydecorations:block/sign_post_" + type.name())
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name() + "_sign_post")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name() + "_sign_post")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));

            writeStatue(type.name(), "block/" + type.name() + "_planks", writer);
        });

        DecorationsItems.WOODEN_MAILBOX.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_mailbox.json",
                    (type != WoodType.BAMBOO ? MAILBOX_MODEL_JSON : MAILBOX_BAMBOO_MODEL_JSON)
                            .replace("|TYPE|", "mailbox")
                            .replace("|FRONT|", "polydecorations:block/mailbox_front_" + type.name())
                            .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                            .replace("|STRIPPED_LOG|", "minecraft:block/stripped_" + WoodUtil.getLogName(type))
                            .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name() + "_mailbox")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name() + "_mailbox")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        DecorationsItems.TOOL_RACK.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_tool_rack.json",
                    BASE_WOOD_MODEL_JSON
                            .replace("|TYPE|", "tool_rack")
                            .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                            .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                            .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.name() + "_tool_rack")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.name() + "_tool_rack")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        });

        DecorationsItems.SLEEPING_BAG.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.asString() + "_sleeping_bag.json",
                    BASE_SLEEPING_BAG_JSON
                            .replace("|TYPE|", "base_sleeping_bag")
                            .replace("|TXT|", "polydecorations:block/sleeping_bag_" + type.asString())
                            .replace("|PARTICLE|", "minecraft:block/" + type.asString() + "_wool")
                            .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept(AssetPaths.itemAsset(id(type.asString() + "_sleeping_bag")),
                    new ItemAsset(new BasicItemModel(id("block/" + type.asString() + "_sleeping_bag")), ItemAsset.Properties.DEFAULT)
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
            writeStatue(color.asString() + "_terracotta", "block/" + color.asString() + "_terracotta", writer);
            writeStatue(color.asString() + "_wool", "block/" + color.asString() + "_wool", writer);

            //b.append('"').append("item.polydecorations.").append(color.getName()).append("_terracotta_statue\": \"")
            //        .append(Character.toUpperCase(color.getName().charAt(0))).append(color.getName().substring(1)).append(" Terracotta Statue\",\n");
        }

        //System.out.println(b);

        DecorationsModels.ROPE.generateModels(writer);

        generateWindChimeModels(writer);

        for (var item : List.of(DecorationsItems.CANVAS, DecorationsItems.ROPE, DecorationsItems.GLOBE, DecorationsItems.GHOST_LIGHT,
                DecorationsItems.TRASHCAN, DecorationsItems.HAMMER, DecorationsItems.TROWEL)) {
            var id = Registries.ITEM.getId(item);
            writer.accept(AssetPaths.itemAsset(id),
                    new ItemAsset(new BasicItemModel(id.withPrefixedPath("item/")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        }

        for (var item : List.of(DecorationsItems.DISPLAY_CASE, DecorationsItems.BRAZIER, DecorationsItems.SOUL_BRAZIER, DecorationsItems.LARGE_FLOWER_POT, DecorationsItems.LONG_FLOWER_POT)) {
            var id = Registries.ITEM.getId(item);
            writer.accept(AssetPaths.itemAsset(id),
                    new ItemAsset(new BasicItemModel(id.withPrefixedPath("block/")), ItemAsset.Properties.DEFAULT)
                            .toJson().getBytes(StandardCharsets.UTF_8));
        }

        writer.accept(AssetPaths.itemAsset(id("basket")),
                new ItemAsset(new BasicItemModel(id("block/basket_open")), ItemAsset.Properties.DEFAULT)
                        .toJson().getBytes(StandardCharsets.UTF_8));
    }

    private void createWoodTextures(BiConsumer<String, byte[]> assetWriter) throws Exception {
        var jar = PolymerCommonUtils.getClientJarRoot();
        var b = new ByteArrayOutputStream();

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

            int newWidth = (positions.size() + 7) & ~7;
            var palette = new BufferedImage(newWidth, 8, BufferedImage.TYPE_INT_RGB);

            for (var wood : WoodUtil.VANILLA) {
                var input = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/block/" + wood.name() + "_planks.png")));
                for (int i = 0; i < positions.size(); i++) {
                    var pos = positions.get(i);
                    palette.setRGB(i, 0, input.getRGB(pos[0], pos[1]));
                }
                ImageIO.write(palette, "png", b);
                assetWriter.accept("assets/polydecorations/textures/palette/wood/" + wood.name() + ".png", b.toByteArray());
                b.reset();
            }
        }

        // Stump top
        for (var prefix :List.of("", "stripped_")) {
            for (var wood : WoodUtil.VANILLA) {
                var source = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/block/" + prefix + WoodUtil.getLogName(wood) + "_top.png")));
                var texture = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
                var frames = source.getHeight() / source.getWidth();

                for (int i = 0; i < frames; i++) {
                    var offset = i * source.getWidth();
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            texture.setRGB(x + 3, y + 3 + offset, source.getRGB(x, y + offset));
                            texture.setRGB(x + 3 + 5, y + 3 + offset, source.getRGB(11 + x, y + offset));
                            texture.setRGB(x + 3, y + 3 + 5 + offset, source.getRGB(x, 11 + y + offset));
                            texture.setRGB(x + 3 + 5, y + 3 + 5 + offset, source.getRGB(11 + x, 11 + y + offset));
                        }
                    }
                }

                ImageIO.write(texture, "png", b);
                assetWriter.accept("assets/polydecorations/textures/block/" + prefix + WoodUtil.getLogName(wood) + "_stump_top.png", b.toByteArray());

                var mcMeta = jar.resolve("assets/minecraft/textures/block/" + prefix + WoodUtil.getLogName(wood) + "_top.png.mcmeta");
                if (Files.exists(mcMeta)) {
                    assetWriter.accept("assets/polydecorations/textures/block/" + prefix + WoodUtil.getLogName(wood) + "_stump_top.png.mcmeta",
                            Files.readAllBytes(mcMeta));
                }
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

            int newWidth = (positions.size() + 7) & ~7;
            var palette = new BufferedImage(newWidth, 8, BufferedImage.TYPE_INT_RGB);

            for (var color : DyeColor.values()) {
                var input = ImageIO.read(Files.newInputStream(jar.resolve("assets/minecraft/textures/entity/bed/" + color.asString() + ".png")));
                for (int i = 0; i < positions.size(); i++) {
                    var pos = positions.get(i);
                    palette.setRGB(i, 0, input.getRGB(pos[0], pos[1]));
                }
                ImageIO.write(palette, "png", b);
                assetWriter.accept("assets/polydecorations/textures/palette/bed_color/" + color.asString() + ".png", b.toByteArray());
                b.reset();
            }
        }
    }

    private void generateWindChimeModels(BiConsumer<String,byte[]> writer) {
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
        var chimeOffset = new ArrayList<Vec3d>();

        for (int i = 0; i < 5; i++) {
            chimeOffset.add(chimesElement[i].getFirst().rotation().orElseThrow().origin());
            for (var e : chimesElement[i]) {
                var offset = e.rotation().orElseThrow().origin().negate().add(8, 8, 8);
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

        writer.accept("wind_chime_offsets.json", Vec3d.CODEC.listOf().encodeStart(JsonOps.INSTANCE, chimeOffset).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));

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

    private void writeBench(WoodType name, BiConsumer<String, byte[]> writer) {
        writeBenchSet(name, "", writer);
        writeBenchSet(name, "_norest", writer);
    }

    private void writeBenchSet(WoodType type, String suffix, BiConsumer<String, byte[]> writer) {
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + ".json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix)
                .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + "_left.json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_left")
                .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + "_right.json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_right")
                .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + "_middle.json", BASE_WOOD_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_middle")
                .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    private void writeStatue(String type, String texture, BiConsumer<String, byte[]> writer) {
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
    public String getName() {
        return "polydecorations:assets";
    }
}
