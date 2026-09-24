package eu.pb4.polydecorations.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.pb4.polydecorations.DecorationsFeature;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.resourcepack.api.PackResource;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class FeatureAssetFilter {
    private static final String ASSETS = "assets/" + ModInit.ID + "/";
    private static final String NAMESPACE = ModInit.ID + ":";
    private static final List<String> TYPE_PREFIXES = List.of("items/-/block/", "items/-/", "items/", "models/block/", "models/item/", "models/",
            "textures/block/", "textures/item/", "textures/");

    public static void register() {
        if (DecorationsFeature.anyHardDisabled()) {
            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(builder -> builder.addResourceConverter(FeatureAssetFilter::convert));
        }
    }

    @Nullable
    private static PackResource convert(String path, PackResource resource) {
        if (path.startsWith("assets/minecraft/atlases/") && path.endsWith(".json")) {
            return filterAtlas(resource);
        } else if (!path.startsWith(ASSETS)) {
            return resource;
        }

        var local = path.substring(ASSETS.length());
        if (local.startsWith("lang/") && local.endsWith(".json")) {
            return filterKeys(resource, FeatureAssetFilter::translationName);
        } else if (local.equals("sounds.json")) {
            return filterKeys(resource, FeatureAssetFilter::soundName);
        } else if (!DecorationsFeature.isUsed(assetName(local))) {
            return null;
        } else if (local.startsWith("font/") && local.endsWith(".json")) {
            return filterFont(resource);
        }
        return resource;
    }

    // models/block/oak_bench_left.json -> oak_bench_left
    public static String assetName(String path) {
        for (var extension : List.of(".png.mcmeta", ".png", ".json")) {
            if (path.endsWith(extension)) {
                path = path.substring(0, path.length() - extension.length());
                break;
            }
        }
        for (var prefix : TYPE_PREFIXES) {
            if (path.startsWith(prefix)) {
                return path.substring(prefix.length());
            }
        }
        return path;
    }

    private static boolean isTextureUsed(String texture) {
        return !texture.startsWith(NAMESPACE) || DecorationsFeature.isUsed(assetName("textures/" + texture.substring(NAMESPACE.length())));
    }

    // block.polydecorations.oak_bench -> oak_bench, subtitles.polydecorations.block.trashcan.open -> trashcan
    @Nullable
    private static String translationName(String key) {
        var parts = key.split("\\.");
        if (parts.length < 3 || !parts[1].equals(ModInit.ID)) {
            return null;
        }
        var index = parts[0].equals("subtitles") ? 3 : 2;
        return index < parts.length ? parts[index] : null;
    }

    // block.trashcan.open -> trashcan
    @Nullable
    private static String soundName(String key) {
        var parts = key.split("\\.");
        return parts.length > 1 ? parts[1] : null;
    }

    private static PackResource filterKeys(PackResource resource, Function<String, @Nullable String> nameGetter) {
        var json = resource.asJson().getAsJsonObject();
        var removed = json.keySet().removeIf(key -> {
            var name = nameGetter.apply(key);
            return name != null && !DecorationsFeature.isUsed(name);
        });
        return removed ? PackResource.fromJson(json) : resource;
    }

    private static PackResource filterFont(PackResource resource) {
        var json = resource.asJson().getAsJsonObject();
        if (!json.has("providers")) {
            return resource;
        }
        var providers = json.getAsJsonArray("providers");
        var removed = providers.asList().removeIf(provider -> provider instanceof JsonObject object
                && object.has("file") && !isTextureUsed(object.get("file").getAsString().replace(".png", "")));
        return removed ? PackResource.fromJson(json) : resource;
    }

    private static PackResource filterAtlas(PackResource resource) {
        var json = resource.asJson().getAsJsonObject();
        if (!json.has("sources")) {
            return resource;
        }
        var changed = false;
        for (var iterator = json.getAsJsonArray("sources").iterator(); iterator.hasNext(); ) {
            if (!(iterator.next() instanceof JsonObject source)) {
                continue;
            }
            if (source.has("resource") && !isTextureUsed(source.get("resource").getAsString())) {
                iterator.remove();
                changed = true;
            } else if (source.get("textures") instanceof JsonArray textures
                    && textures.asList().removeIf(texture -> !isTextureUsed(texture.getAsString()))) {
                changed = true;
                if (textures.isEmpty()) {
                    iterator.remove();
                }
            }
        }
        return changed ? PackResource.fromJson(json) : resource;
    }
}
