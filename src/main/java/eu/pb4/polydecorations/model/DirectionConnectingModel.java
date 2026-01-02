package eu.pb4.polydecorations.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.util.ResourceUtils;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionConnectingModel {
    public static final int SIZE = (int) Math.pow(2, 6);
    private final Identifier baseModel;
    private final ItemStack[] models = new ItemStack[SIZE];

    public DirectionConnectingModel(Identifier baseModel) {
        this.baseModel = baseModel;

        for (var i = 0; i < SIZE; i++) {
            this.models[i] = ItemDisplayElementUtil.getSolidModel(baseModel.withSuffix("/" + i));
        }
    }

    public void generateModels(BiConsumer<String, byte[]> dataWriter) {
        var model = ResourceUtils.getElementResolvedModel(this.baseModel);

        for (int i = 0; i < SIZE; i++) {
            int dirCount = 0;
            int axisCount = 0;
            for (var axis : Direction.Axis.values()) {
                var a = hasDirection(i, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
                var b = hasDirection(i, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE));
                if (a && b) {
                    axisCount += 1;
                    dirCount += 2;
                } else if (a || b) {
                    dirCount += 1;
                }
            }

            var base = new JsonObject();
            base.asMap().putAll(model.asMap());
            var elements = new JsonArray();

            for (var element : model.getAsJsonArray("elements")) {
                var name = element.getAsJsonObject().get("name");
                if (name != null && name.getAsString().equals("center_linear")) {
                    if (dirCount != 2 || axisCount != 1) {
                        elements.add(element);
                    }
                } else if (name != null && name.getAsString().equals("center_knot")) {
                    if (dirCount >= 3 || dirCount == 0) {
                        elements.add(element);
                    }
                } else if (name != null && name.getAsString().equals("center_no_knot")) {
                    if (dirCount < 3 || dirCount != 0) {
                        elements.add(element);
                    }
                } else if (name != null && name.getAsString().startsWith("rp_")) {
                    var dir = Direction.byName(name.getAsString().substring("rp_".length()));
                    if (dir == null || dirCount == 0) {
                        elements.add(element);
                    } else if (dirCount == 1 && hasDirection(i, dir.getOpposite())) {
                        elements.add(element);
                    } else if (hasDirection(i, dir)) {
                        elements.add(element);
                    }
                } else if (name != null && name.getAsString().startsWith("tuft_")) {
                    var dir = Direction.byName(name.getAsString().substring("tuft_".length()));
                    if (dir == null || dirCount == 0) {
                        elements.add(element);
                    } else if (dirCount == 1 && hasDirection(i, dir.getOpposite())) {
                        elements.add(element);
                    }
                } else if (name != null && Direction.byName(name.getAsString()) != null) {
                    if (hasDirection(i, Direction.byName(name.getAsString()))) {
                        elements.add(element);
                    }
                } else {
                    elements.add(element);
                }
            }
            base.add("elements", elements);

            dataWriter.accept(AssetPaths.model(this.baseModel.withSuffix("/" + i + ".json")), base.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    public ItemStack get(BlockState state, BiPredicate<BlockState, Direction> directionPredicate) {
        return get(getModelId(state, directionPredicate));
    }

    public ItemStack get(int i) {
        return this.models[i];
    }
    public static boolean hasDirection(int i, Direction direction) {
        if (direction == null) {
            return false;
        }

        return (i & (1 << direction.ordinal())) != 0;
    }

    public static int getModelId(BlockState state, BiPredicate<BlockState, Direction> directionPredicate) {
        int i = 0;

        for(int j = 0; j < Direction.values().length; ++j) {
            var direction = Direction.values()[j];
            if (directionPredicate.test(state, direction)) {
                i |= 1 << j;
            }
        }

        return i;
    }
}
