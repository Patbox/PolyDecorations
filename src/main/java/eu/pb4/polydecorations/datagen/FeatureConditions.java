package eu.pb4.polydecorations.datagen;

import eu.pb4.polydecorations.DecorationsFeature;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.util.FeatureResourceCondition;
import net.fabricmc.fabric.api.datagen.v1.recipe.FabricRecipeOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// Generated recipes, their advancements and loot tables only load if their feature allows it.
// The feature is found by the name of the entry, the same way as for assets
class FeatureConditions {
    // Recipes using items of other features
    private static final Map<String, List<DecorationsFeature>> EXTRA_FEATURES = Map.of(
            "basket", List.of(DecorationsFeature.ROPE),
            "copper_brazier", List.of(DecorationsFeature.COPPER_CAMPFIRE)
    );

    public static ResourceCondition[] recipe(Identifier id) {
        var name = name(id);
        var list = new ArrayList<ResourceCondition>();
        list.add(owner(name, false));
        for (var feature : EXTRA_FEATURES.getOrDefault(name, List.of())) {
            list.add(new FeatureResourceCondition(feature, true));
        }
        return list.toArray(ResourceCondition[]::new);
    }

    public static ResourceCondition[] lootTable(Identifier id) {
        return new ResourceCondition[] { owner(name(id), true) };
    }

    private static String name(Identifier id) {
        return id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
    }

    private static ResourceCondition owner(String name, boolean registered) {
        var owners = DecorationsFeature.owners(name);
        if (owners.isEmpty()) {
            throw new IllegalStateException("No feature found for '" + name + "', add it to DecorationsFeature");
        }

        var conditions = owners.stream().map(feature -> new FeatureResourceCondition(feature, registered)).toArray(ResourceCondition[]::new);
        return conditions.length == 1 ? conditions[0] : ResourceConditions.or(conditions);
    }

    public static <T> BootstrapContext<T> recipes(BootstrapContext<T> context) {
        return new ConditionalContext<>(context);
    }

    // Entries of this mod might not exist if their feature is hard disabled
    public static <T> TagAppender<T> optionalEntries(TagAppender<T> appender) {
        return new TagAppender<>() {
            @Override
            public TagAppender<T> add(ResourceKey<T> key) {
                if (key.identifier().getNamespace().equals(ModInit.ID)) {
                    appender.addOptional(key);
                } else {
                    appender.add(key);
                }
                return this;
            }

            @Override
            public TagAppender<T> addOptional(ResourceKey<T> key) {
                appender.addOptional(key);
                return this;
            }

            @Override
            public TagAppender<T> addTag(TagKey<T> tag) {
                appender.addTag(tag);
                return this;
            }

            @Override
            public TagAppender<T> addOptionalTag(TagKey<T> tag) {
                appender.addOptionalTag(tag);
                return this;
            }
        };
    }

    // Implements FabricRecipeOutput, as fabric uses it to get recipe ids
    private record ConditionalContext<T>(BootstrapContext<T> context) implements BootstrapContext<T>, FabricRecipeOutput {
        @Override
        public Holder.Reference<T> register(ResourceKey<T> key, T value) {
            FabricDataGenHelper.addConditions(value, FeatureConditions.recipe(key.identifier()));
            return this.context.register(key, value);
        }

        @Override
        public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
            return this.context.lookup(key);
        }

        @Override
        public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
            return this.context.listContextElements(key);
        }

        @Override
        public Identifier getRecipeIdentifier(Identifier recipeId) {
            return this.context instanceof FabricRecipeOutput output ? output.getRecipeIdentifier(recipeId) : recipeId;
        }
    }
}
