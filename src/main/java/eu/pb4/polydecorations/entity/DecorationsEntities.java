package eu.pb4.polydecorations.entity;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DecorationsEntities {
    //public static final EntityType<ArtificialWitherSkullEntity> ARTIFICIAL_WITHER_SKULL = register("artificial_wither_skull", FabricEntityTypeBuilder
   //         .create().fireImmune().dimensions(EntityDimensions.fixed(0.5f, 0.5f)).entityFactory(ArtificialWitherSkullEntity::new));

    public static void register() {

    }

    public static <T extends Entity> EntityType<T> register(String path, FabricEntityTypeBuilder<T> item) {
        var x = Registry.register(Registries.ENTITY_TYPE, new Identifier(ModInit.ID, path), item.build());
        PolymerEntityUtils.registerType(x);
        return x;
    }
}
