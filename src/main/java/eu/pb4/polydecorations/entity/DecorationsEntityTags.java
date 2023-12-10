package eu.pb4.polydecorations.entity;

import eu.pb4.polydecorations.ModInit;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class DecorationsEntityTags {

    private static TagKey<EntityType<?>> of(String path) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, ModInit.id(path));
    }}
