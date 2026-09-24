package eu.pb4.polydecorations;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;

// Loaded from the mixin plugin, so it can't touch any Minecraft classes
public final class DecorationsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("PolyDecorations");
    private static final Map<DecorationsFeature, Mode> MODES = load();

    public static Mode mode(DecorationsFeature feature) {
        return MODES.getOrDefault(feature, Mode.ENABLED);
    }

    private static Map<DecorationsFeature, Mode> load() {
        var modes = new EnumMap<DecorationsFeature, Mode>(DecorationsFeature.class);
        if (System.getProperty("fabric-api.datagen") != null) {
            return modes;
        }

        var path = FabricLoader.getInstance().getConfigDir().resolve("polydecorations.json");
        var features = new JsonObject();
        var write = true;
        try {
            if (Files.exists(path)) {
                var json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (json.has("features")) {
                    features = json.getAsJsonObject("features");
                }
                write = false;
            }

            for (var key : features.keySet()) {
                if (DecorationsFeature.byKey(key) == null) {
                    LOGGER.warn("Unknown feature '{}' in config/polydecorations.json", key);
                }
            }

            for (var feature : DecorationsFeature.values()) {
                if (!features.has(feature.key())) {
                    features.addProperty(feature.key(), true);
                    write = true;
                    continue;
                }

                var mode = Mode.of(features.get(feature.key()));
                if (mode == null) {
                    LOGGER.warn("Invalid value for '{}' in config/polydecorations.json, expected true, false or \"hard\"", feature.key());
                } else if (mode != Mode.ENABLED) {
                    modes.put(feature, mode);
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Failed to read config/polydecorations.json, all features will be enabled!", e);
            return new EnumMap<>(DecorationsFeature.class);
        }

        if (write) {
            try {
                var json = new JsonObject();
                json.add("features", features);
                Files.createDirectories(path.getParent());
                Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(json));
            } catch (Throwable e) {
                LOGGER.warn("Failed to write config/polydecorations.json", e);
            }
        }

        if (!modes.isEmpty()) {
            LOGGER.info("Disabled features: {}", modes);
        }
        return modes;
    }

    public enum Mode {
        ENABLED,
        // Content stays registered, but can't be crafted or obtained in survival
        DISABLED,
        // Content isn't registered at all
        HARD;

        @Nullable
        private static Mode of(JsonElement element) {
            if (!(element instanceof JsonPrimitive primitive)) {
                return null;
            }
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean() ? ENABLED : DISABLED;
            }
            return switch (primitive.getAsString()) {
                case "true" -> ENABLED;
                case "false" -> DISABLED;
                case "hard" -> HARD;
                default -> null;
            };
        }
    }
}
