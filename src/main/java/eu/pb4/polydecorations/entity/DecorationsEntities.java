package eu.pb4.polydecorations.entity;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

public class DecorationsEntities {
    public static final EntityType<CanvasEntity> CANVAS = register("canvas", EntityType.Builder
            .of(CanvasEntity::new, MobCategory.MISC).sized(0.5f, 0.5f));

    public static final EntityType<StatueEntity> STATUE = register("statue", EntityType.Builder
            .of(StatueEntity::new, MobCategory.MISC).sized(EntityTypes.ARMOR_STAND.getDimensions().width(), EntityTypes.ARMOR_STAND.getDimensions().height()));

    public static final EntityType<SeatEntity> SEAT = register("seat", EntityType.Builder
            .of(SeatEntity::new, MobCategory.MISC).fireImmune().sized(0f, 0f).noSummon());

    public static final EntityType<FirstLeashFenceKnotEntity> FIRST_LEASH_FENCE_KNOT = register("first_leash_fence_knot",
             EntityType.Builder.of(FirstLeashFenceKnotEntity::new, MobCategory.MISC).noLootTable().sized(0.375F, 0.5F).eyeHeight(0.0625F).clientTrackingRange(10)
                    .updateInterval(10));

    public static void register() {
        StatueEntity.Type.STONE.fireproof();
        FabricDefaultAttributeRegistry.register(STATUE, StatueEntity.createLivingAttributes());
    }

    public static <T extends Entity> EntityType<T> register(String path, EntityType.Builder<T> item) {
        var id = Identifier.fromNamespaceAndPath(ModInit.ID, path);
        var x = Registry.register(BuiltInRegistries.ENTITY_TYPE, id, item.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
        PolymerEntityUtils.registerType(x);
        return x;
    }
}
