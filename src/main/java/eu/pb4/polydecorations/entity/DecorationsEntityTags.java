package eu.pb4.polydecorations.entity;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class DecorationsEntityTags {

    private static TagKey<EntityType<?>> of(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ModInit.id(path));
    }}
