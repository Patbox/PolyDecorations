package eu.pb4.polydecorations.datagen;

import com.google.common.hash.HashCode;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.ui.UiResourceCreator;
import eu.pb4.polydecorations.util.WoodUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.WoodType;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
            UiResourceCreator.generateAssets(assetWriter);

            writeBlocksAndItems(assetWriter);
        }, Util.getMainWorkerExecutor());
    }

    private static final String BASE_MODEL_JSON = """
            {
              "parent": "polydecorations:block/base_|TYPE|",
              "textures": {
                "planks": "|PLANKS|",
                "logs": "|LOG|"
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
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_shelf.json", BASE_MODEL_JSON
                    .replace("|TYPE|", "shelf")
                    .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/block/" + type.name() + "_shelf_top.json", BASE_MODEL_JSON
                    .replace("|TYPE|", "shelf_top")
                    .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/block/" + type.name() + "_shelf_double.json", BASE_MODEL_JSON
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

            writer.accept("assets/polydecorations/models/block/" + type.name() + "_sign_post.json", BASE_MODEL_JSON
                    .replace("|TYPE|", "sign_post")
                    .replace("|PLANKS|", "minecraft:entity/signs/" + type.name())
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_sign_post.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_sign_post")
                    .getBytes(StandardCharsets.UTF_8)
            );

            writeStatue(type.name(), "block/" + type.name() + "_planks", writer);

        });


        writeStatue("deepslate", "block/deepslate_top", writer);
        writeStatue("blackstone", "block/blackstone", writer);
        writeStatue("prismarine", "block/prismarine", writer);
        writeStatue("sandstone", "block/sandstone_top", writer);
        writeStatue("red_sandstone", "block/red_sandstone_top", writer);
        writeStatue("quartz", "block/quartz_block_bottom", writer);
    }

    private void writeBench(WoodType name, BiConsumer<String, byte[]> writer) {
        writeBenchSet(name, "", writer);
        writeBenchSet(name, "_norest", writer);
    }

    private void writeBenchSet(WoodType type, String suffix, BiConsumer<String, byte[]> writer) {
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + ".json", BASE_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix)
                .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + "_left.json", BASE_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_left")
                .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + "_right.json", BASE_MODEL_JSON
                .replace("|TYPE|", "bench" + suffix + "_right")
                .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                .getBytes(StandardCharsets.UTF_8)
        );
        writer.accept("assets/polydecorations/models/block/" + type.name() + "_bench" + suffix + "_middle.json", BASE_MODEL_JSON
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
