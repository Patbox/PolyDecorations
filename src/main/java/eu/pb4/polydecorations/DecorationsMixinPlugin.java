package eu.pb4.polydecorations;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static eu.pb4.polydecorations.DecorationsFeature.*;

public class DecorationsMixinPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "eu.pb4.polydecorations.mixin.";
    // Mixins only needed by some features, skipped if all of them are hard disabled
    private static final Map<String, DecorationsFeature[]> FEATURE_MIXINS = Map.ofEntries(
            Map.entry("BlockItemMixin", new DecorationsFeature[]{ WALL_LANTERNS }),
            Map.entry("LanternBlockMixin", new DecorationsFeature[]{ WALL_LANTERNS, ROPE }),
            Map.entry("CeilingHangingSignBlockMixin", new DecorationsFeature[]{ ROPE }),
            Map.entry("LeadItemMixin", new DecorationsFeature[]{ FENCE_LEADS }),
            Map.entry("FaceAttachedHorizontalDirectionalBlockMixin", new DecorationsFeature[]{ STUMP }),
            Map.entry("ShulkerBoxBlockEntityMixin", new DecorationsFeature[]{ BASKET, TIED_CONTAINERS }),
            Map.entry("ItemContainerContentsMixin", new DecorationsFeature[]{ TIED_CONTAINERS }),
            Map.entry("LivingEntityMixin", new DecorationsFeature[]{ SLEEPING_BAG }),
            Map.entry("ServerPlayerMixin", new DecorationsFeature[]{ SLEEPING_BAG }),
            Map.entry("SignBlockEntityMixin", new DecorationsFeature[]{ SIGN_POST }),
            Map.entry("flowerpot.PlantBlockMixin", new DecorationsFeature[]{ FLOWER_POTS }),
            Map.entry("flowerpot.SaplingBlockMixin", new DecorationsFeature[]{ FLOWER_POTS })
    );

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var features = FEATURE_MIXINS.get(mixinClassName.startsWith(PACKAGE) ? mixinClassName.substring(PACKAGE.length()) : mixinClassName);
        return features == null || DecorationsFeature.anyRegistered(features);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
