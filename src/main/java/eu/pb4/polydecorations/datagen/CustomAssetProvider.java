package eu.pb4.polydecorations.datagen;

import com.google.common.hash.HashCode;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.ui.UiResourceCreator;
import eu.pb4.polydecorations.util.WoodUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
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

    private static final String SHELF_MODEL_JSON = """
            {
              "parent": "polydecorations:block/base_shelf",
              "textures": {
                "planks": "|PLANKS|",
                "logs": "|LOG|"
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
            writer.accept("assets/polydecorations/models/block/" + type.name() + "_shelf.json", SHELF_MODEL_JSON
                    .replace("|PLANKS|", "minecraft:block/" + type.name() + "_planks")
                    .replace("|LOG|", "minecraft:block/" + WoodUtil.getLogName(type))
                    .getBytes(StandardCharsets.UTF_8)
            );

            writer.accept("assets/polydecorations/models/item/" + type.name() + "_shelf.json", ITEM_MODEL_JSON
                    .replace("|I|", type.name() + "_shelf")
                    .getBytes(StandardCharsets.UTF_8)
            );
        });
        System.out.println(t);
    }

    @Override
    public String getName() {
        return "polydecorations:assets";
    }
}
