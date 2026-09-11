package eu.pb4.polydecorations.util;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.datagen.CustomAssetProvider;
import eu.pb4.polydecorations.datagen.LootTables;
import eu.pb4.polydecorations.datagen.RecipesProvider;
import eu.pb4.polydecorations.mixin.LanguageAccessor;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.recipe.FabricRecipeOutput;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.core.*;
import net.minecraft.core.registries.BootstrapRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.LootTable;
import nl.theepicblock.resourcelocatorapi.ResourceLocatorApi;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModdedCompat {
    private static final PackLocationInfo PACK_LOCATION_INFO = new PackLocationInfo("$polydecorations", Component.literal("PolyDecorations Dynamic Compat Data"), PackSource.BUILT_IN, Optional.empty());
    private static PackResources resources = null;

    public static void detect() {
        WoodType.values().forEach(WoodUtil::addModded);
        ModInit.LATE_INIT.forEach(Runnable::run);
        ModInit.LATE_INIT.clear();

        if (!WoodUtil.MODDED.isEmpty()) {
            PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(ModdedCompat::createDynamicAssets);
            setupData();
        }
    }

    private static void createDynamicAssets(ResourcePackBuilder builder) {
        try (var pack = ResourceLocatorApi.createGlobalAssetContainer()) {

            var blockAtlas = AtlasAsset.builder();
            CustomAssetProvider.createWoodTextures(blockAtlas, builder::addData, x -> {
                var data = builder.getDataOrSource(x);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }

                var parts = x.split("/");

                if (parts.length <= 2) {
                    throw new RuntimeException("Not found! " + x);
                }

                return Objects.requireNonNull(pack.getAsset(parts[1], String.join("/", List.of(parts).subList(2, parts.length))), "Not found! " + x).get();
            }, WoodUtil.MODDED);
            CustomAssetProvider.writeWoodenBlocksAndItems(builder::addData, WoodUtil.MODDED);
            builder.addData("assets/minecraft/atlases/blocks.json", blockAtlas.build());

            createLanguage(builder);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private static void createLanguage(ResourcePackBuilder builder) {
        var defaultLanguage = LanguageAccessor.callLoadDefault();

        var json = new JsonObject();
        for (var wood : WoodUtil.MODDED) {
            var id = WoodUtil.asPath(wood).replace('/', '.');
            var name = Arrays.stream(Identifier.parse(wood.name()).getPath()
                    .split("_"))
                    .map(x -> x.substring(0, 1).toUpperCase(Locale.ROOT) + x.substring(1))
                    .collect(Collectors.joining(" "));
            name = defaultLanguage.getOrDefault("block." + id.replace('/', '.') + "_planks", name).replace(" Planks", "");

            json.addProperty("block.polydecorations." + id + "_shelf", name + " Plain Shelf");
            json.addProperty("block.polydecorations." + id + "_mailbox", name + " Mailbox");
            json.addProperty("block.polydecorations." + id + "_bench", name + " Bench");
            json.addProperty("block.polydecorations." + id + "_table", name + " Table");
            json.addProperty("block.polydecorations." + id + "_tool_rack", name + " Tool Rack");
            json.addProperty("block.polydecorations." + id + "_stump", name + " Stump");
            json.addProperty("block.polydecorations.stripped_" + id + "_stump", "Stripped " + name + " Stump");
            json.addProperty("block.polydecorations." + id + "_sign_post", name + " Sign Post");
            json.addProperty("item.polydecorations." + id + "_statue", name + " Statue");
        }

        builder.addData("assets/polydecorations/lang/en_us.json", json.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void setupData() {
        var map = new HashMap<String, byte[]>();
        var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);

        FabricBootstrapContext<Recipe<?>> recipes = new RecipeBootstrapContext(registryAccess, identifier -> Identifier.fromNamespaceAndPath(ModInit.ID, identifier.getPath()));
        FabricBootstrapContext<Advancement> advancements = new FabricBootstrapContext<>(registryAccess, Registries.ADVANCEMENT);

        RecipesProvider.woodRecipeProvider(WoodUtil.MODDED, recipes, advancements).buildRecipes();
        new LootTables.WoodLootTables(WoodUtil.MODDED, new LootTableSubProvider.Context() {
            @Override
            public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder value) {
                map.put("data/" + key.identifier().getNamespace() + "/loot_table/" + key.identifier().getPath() + ".json",
                        LootTable.DIRECT_CODEC.encodeStart(ops, value.build()).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));

                return null;
            }

            @Override
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                return registryAccess.lookupOrThrow(key);
            }

            @Override
            public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
                return registryAccess.lookupOrThrow(key).listElements();
            }
        }).run();

        recipes.entries().forEach((key, val) -> {
            map.put("data/" + key.identifier().getNamespace() + "/recipe/" + key.identifier().getPath() + ".json", Recipe.DIRECT_CODEC.encodeStart(ops, val).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));
        });

        advancements.entries().forEach((key, val) -> {
            map.put("data/" + key.identifier().getNamespace() + "/advancement/" + key.identifier().getPath() + ".json", Advancement.CODEC.encodeStart(ops, val).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));
        });

        var list = new ArrayList<TagEntry>();
        for (var x : List.of(
                Maps.filterKeys(DecorationsBlocks.SHELF, WoodUtil.MODDED::contains).values(),
                Maps.filterKeys(DecorationsBlocks.WOOD_SIGN_POST, WoodUtil.MODDED::contains).values(),
                Maps.filterKeys(DecorationsBlocks.WOODEN_MAILBOX, WoodUtil.MODDED::contains).values(),
                Maps.filterKeys(DecorationsBlocks.BENCH, WoodUtil.MODDED::contains).values(),
                Maps.filterKeys(DecorationsBlocks.TABLE, WoodUtil.MODDED::contains).values(),
                Maps.filterKeys(DecorationsBlocks.TOOL_RACK, WoodUtil.MODDED::contains).values(),
                Maps.filterKeys(DecorationsBlocks.STUMP, WoodUtil.MODDED::contains).values(),
                Maps.filterKeys(DecorationsBlocks.STRIPPED_STUMP, WoodUtil.MODDED::contains).values())) {
            for (var y : x) {
                list.add(TagEntry.element(BuiltInRegistries.BLOCK.getKey(y)));
            }
        }
        var tagId = BlockTags.MINEABLE_WITH_AXE.location();
        map.put("data/" + tagId.getNamespace() + "/tags/block/" + tagId.getPath() + ".json", TagFile.CODEC.encodeStart(ops, new TagFile(list, false)).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));

        /*for (var path : map.keySet()) {
            System.out.println(path);
        }*/


        resources = new VirtualPackResources(map, new HashMap<>());
    }

    private record VirtualPackResources(Map<String, byte[]> data, Map<String, List<Pair<Identifier, IoSupplier<InputStream>>>> cache) implements PackResources {
        @Override
        public @Nullable IoSupplier<InputStream> getRootResource(String... parts) {
            var path = String.join("/", parts);
            var data = this.data.get(path);
            return data != null ? () -> new ByteArrayInputStream(data) : null;
        }

        @Override
        public @Nullable IoSupplier<InputStream> getResource(PackType packType, Identifier id) {
            return getRootResource(packType.getDirectory(), id.getNamespace(), id.getPath());
        }

        @Override
        public void listResources(PackType packType, String namespace, String directory, ResourceOutput output) {
            var path = packType.getDirectory() + "/" + namespace + "/" + directory + "/";
            var list = cache.get(path);
            if (list != null) {
                list.forEach(x -> output.accept(x.getFirst(), x.getSecond()));
                return;
            }
            list = new ArrayList<>();
            var prefix = (packType.getDirectory() + "/" + namespace + "/").length();

            var finalList = list;
            this.data.forEach((x, val) -> {
                if (!x.startsWith(path)) {
                    return;
                }

                var identifier = Identifier.fromNamespaceAndPath(namespace, x.substring(prefix));
                IoSupplier<InputStream> data = () -> new ByteArrayInputStream(val);
                output.accept(identifier, data);
                finalList.add(new Pair<>(identifier, data));
            });

            this.cache.put(path, list);
        }

        @Override
        public Set<String> getNamespaces(PackType packType) {
            return Set.of("polydecorations", "minecraft");
        }

        @Override
        public @Nullable <T> T getMetadataSection(MetadataSectionType<T> metadataSectionType) throws IOException {
            return null;
        }

        @Override
        public PackLocationInfo location() {
            return PACK_LOCATION_INFO;
        }

        @Override
        public void close() {
        }
    }

    public record VirtualPackSource(PackType packType) implements RepositorySource {
        @Override
        public void loadPacks(Consumer<Pack> consumer) {
            if (resources != null) {
                consumer.accept(new Pack(
                        PACK_LOCATION_INFO,
                        new Pack.ResourcesSupplier() {
                            @Override
                            public PackMetadataResources openMetadata(PackLocationInfo location) {
                                return resources;
                            }

                            @Override
                            public Stream<PackResources> openResources(PackLocationInfo location, Pack.Metadata metadata) {
                                return Stream.of(resources);
                            }
                        },
                        new Pack.Metadata(Component.literal("PolyDecorations Dynamic Compat Data"), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of()),
                        new PackSelectionConfig(true, Pack.Position.BOTTOM, true)
                ));
            }
        }
    }


    private static class FabricBootstrapContext<T> implements BootstrapContext<T> {
        private final HolderLookup.Provider registries;
        private final ResourceKey<? extends Registry<T>> registryKey;
        private final Map<ResourceKey<T>, T> entries = new LinkedHashMap<>();
        private final BootstrapRegistry<T> entryLookup;

        private FabricBootstrapContext(HolderLookup.Provider registries, ResourceKey<? extends Registry<T>> registryKey) {
            this.registries = registries;
            this.registryKey = registryKey;
            this.entryLookup = new BootstrapRegistry<>(registryKey, Lifecycle.stable());
        }

        @Override
        public Holder.Reference<T> register(ResourceKey<T> key, T value) {
            if (entries.putIfAbsent(key, value) != null) {
                throw new IllegalStateException("Duplicate registration for " + key);
            }

            return entryLookup.getOrThrow(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
            if (key.equals(registryKey)) {
                return (HolderGetter<S>) entryLookup;
            }

            return registries.lookupOrThrow(key);
        }

        @Override
        public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
            return registries.lookupOrThrow(key).listElements();
        }

        public Map<ResourceKey<T>, T> entries() {
            return entries;
        }
    }

    private static final class RecipeBootstrapContext extends FabricBootstrapContext<Recipe<?>> implements FabricRecipeOutput {
        private final Function<Identifier, Identifier> recipeIdentifier;

        private RecipeBootstrapContext(HolderLookup.Provider registries, Function<Identifier, Identifier> recipeIdentifier) {
            super(registries, Registries.RECIPE);
            this.recipeIdentifier = recipeIdentifier;
        }

        @Override
        public Identifier getRecipeIdentifier(Identifier recipeId) {
            return recipeIdentifier.apply(recipeId);
        }
    }
}
