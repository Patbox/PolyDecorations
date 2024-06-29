package eu.pb4.polydecorations.datagen;

import com.google.common.hash.HashCode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.block.furniture.BenchBlock;
import eu.pb4.polydecorations.block.furniture.TableBlock;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.ui.UiResourceCreator;
import eu.pb4.polydecorations.util.ResourceUtils;
import eu.pb4.polydecorations.util.WoodUtil;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.WoodType;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

class CustomAssetProvider implements DataProvider {
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
                createWoodPalettes(assetWriter);
                UiResourceCreator.generateAssets(assetWriter);
                writeBlocksAndItems(assetWriter);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }, Util.getMainWorkerExecutor());
    }

    private void createWoodPalettes(BiConsumer<String,byte[]> assetWriter) throws Exception {
        var jar = PolymerCommonUtils.getClientJarRoot();
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
        var b = new ByteArrayOutputStream();

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

    private static final String ITEM_MODEL_JSON = """
            {
              "parent": "polydecorations:block/|I|"
            }
            """;

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

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_shelf.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_shelf")
                    .getBytes(StandardCharsets.UTF_8)
            );
        });

        DecorationsItems.BENCH.forEach((type, item) -> {
            writeBench(type, writer);

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_bench.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_bench")
                    .getBytes(StandardCharsets.UTF_8)
            );
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

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_table.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_table")
                    .getBytes(StandardCharsets.UTF_8)
            );
        });

        DecorationsItems.WOODEN_STATUE.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_sign_post.json", BASE_WOOD_MODEL_JSON
                    .replace("|TYPE|", "sign_post")
                    .replace("|PLANKS|", "polydecorations:block/sign_post_" + type.name())
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_sign_post.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_sign_post")
                    .getBytes(StandardCharsets.UTF_8)
            );

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

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_mailbox.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_mailbox")
                    .getBytes(StandardCharsets.UTF_8)
            );
        });

        DecorationsItems.TOOL_RACK.forEach((type, item) -> {
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_tool_rack.json",
                    BASE_WOOD_MODEL_JSON
                            .replace("|TYPE|", "tool_rack")
                            .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                            .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                            .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_tool_rack.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_tool_rack")
                    .getBytes(StandardCharsets.UTF_8)
            );
        });

        writeBaseTable(writer);


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
            writeStatue(color.getName() + "_terracotta", "block/" + color.getName() + "_terracotta", writer);

            //b.append('"').append("item.polydecorations.").append(color.getName()).append("_terracotta_statue\": \"")
            //        .append(Character.toUpperCase(color.getName().charAt(0))).append(color.getName().substring(1)).append(" Terracotta Statue\",\n");
        }

        //System.out.println(b);
    }

    private void writeBaseTable(BiConsumer<String,byte[]> writer) {
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
    }

    @Override
    public String getName() {
        return "polydecorations:assets";
    }
}
