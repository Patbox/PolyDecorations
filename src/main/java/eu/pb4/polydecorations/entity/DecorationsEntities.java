package eu.pb4.polydecorations.entity;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class DecorationsEntities {
    public static final EntityType<CanvasEntity> CANVAS = register("canvas", FabricEntityTypeBuilder
            .create().dimensions(EntityDimensions.changing(0.5f, 0.5f)).entityFactory(CanvasEntity::new));

    public static final EntityType<StatueEntity> STATUE = register("statue", FabricEntityTypeBuilder
            .create().dimensions(EntityType.ARMOR_STAND.getDimensions()).entityFactory(StatueEntity::new));

    public static final EntityType<SeatEntity> SEAT = register("seat", FabricEntityTypeBuilder
            .create().fireImmune().dimensions(EntityDimensions.fixed(0f, 0f)).entityFactory(SeatEntity::new).disableSummon());

    public static void register() {
        StatueEntity.Type.STONE.fireproof();
        FabricDefaultAttributeRegistry.register(STATUE, StatueEntity.createLivingAttributes());
    }

    public static <T extends Entity> EntityType<T> register(String path, FabricEntityTypeBuilder<T> item) {
        var id = Identifier.of(ModInit.ID, path);
        var x = Registry.register(Registries.ENTITY_TYPE, id, item.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id)));
        PolymerEntityUtils.registerType(x);
        return x;
    }
}
