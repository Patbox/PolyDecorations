package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.furniture.ShelfBlock;
import eu.pb4.polydecorations.block.furniture.ShelfBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DecorationsBlockEntities {
    public static final BlockEntityType<?> SHELF = register("shelf",
                    FabricBlockEntityTypeBuilder.create(ShelfBlockEntity::new)
                            .addBlocks(DecorationsBlocks.SHELF.values().toArray(new ShelfBlock[0])));
            ;



    public static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder<T> item) {
        var x = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(ModInit.ID, path), item.build());
        PolymerBlockUtils.registerBlockEntity(x);
        return x;
    }

    public static void register() {
    }
}
