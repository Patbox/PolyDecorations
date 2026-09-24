package eu.pb4.polydecorations.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polydecorations.DecorationsFeature;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

import static eu.pb4.polydecorations.ModInit.id;

// {"condition": "polydecorations:feature", "feature": "bench"} passes if the feature is enabled,
// with "registered": true it passes as long as the feature isn't hard disabled
public record FeatureResourceCondition(DecorationsFeature feature, boolean registered) implements ResourceCondition {
    private static final Codec<DecorationsFeature> FEATURE_CODEC = Codec.STRING.comapFlatMap(key -> {
        var feature = DecorationsFeature.byKey(key);
        return feature != null ? DataResult.success(feature) : DataResult.error(() -> "Unknown feature '" + key + "'");
    }, DecorationsFeature::key);

    public static final MapCodec<FeatureResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FEATURE_CODEC.fieldOf("feature").forGetter(FeatureResourceCondition::feature),
            Codec.BOOL.optionalFieldOf("registered", false).forGetter(FeatureResourceCondition::registered)
    ).apply(instance, FeatureResourceCondition::new));

    public static final ResourceConditionType<FeatureResourceCondition> TYPE = ResourceConditionType.create(id("feature"), CODEC);

    public static void register() {
        ResourceConditions.register(TYPE);
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryInfo) {
        return this.registered ? this.feature.isRegistered() : this.feature.isEnabled();
    }
}
