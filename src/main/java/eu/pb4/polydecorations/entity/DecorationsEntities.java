package eu.pb4.polydecorations.entity;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;

public class DecorationsEntities {
    public static final EntityType<CanvasEntity> CANVAS = register("canvas", FabricEntityTypeBuilder
            .create().dimensions(EntityDimensions.scalable(0.5f, 0.5f)).entityFactory(CanvasEntity::new));

    public static final EntityType<StatueEntity> STATUE = register("statue", FabricEntityTypeBuilder
            .create().dimensions(EntityType.ARMOR_STAND.getDimensions()).entityFactory(StatueEntity::new));

    public static final EntityType<SeatEntity> SEAT = register("seat", FabricEntityTypeBuilder
            .create().fireImmune().dimensions(EntityDimensions.fixed(0f, 0f)).entityFactory(SeatEntity::new).disableSummon());

    public static void register() {
        StatueEntity.Type.STONE.fireproof();
        FabricDefaultAttributeRegistry.register(STATUE, StatueEntity.createLivingAttributes());
    }

    public static <T extends Entity> EntityType<T> register(String path, FabricEntityTypeBuilder<T> item) {
        var id = Identifier.fromNamespaceAndPath(ModInit.ID, path);
        var x = Registry.register(BuiltInRegistries.ENTITY_TYPE, id, item.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
        PolymerEntityUtils.registerType(x);
        return x;
    }
}
