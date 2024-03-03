package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.item.ShelfBlock;
import eu.pb4.polydecorations.block.item.ShelfBlockEntity;
import eu.pb4.polydecorations.block.other.GenericSingleItemBlockEntity;
import eu.pb4.polydecorations.block.extension.AttachedSignPostBlock;
import eu.pb4.polydecorations.block.extension.SignPostBlockEntity;
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

    public static final BlockEntityType<?> SIGN_POST = register("sign_post",
            FabricBlockEntityTypeBuilder.create(SignPostBlockEntity::new)
                    .addBlocks(DecorationsBlocks.WOOD_SIGN_POST.values().toArray(new AttachedSignPostBlock[0]))
                    .addBlocks(DecorationsBlocks.WALL_SIGN_POST.values().toArray(new AttachedSignPostBlock[0]))
                    .addBlock(DecorationsBlocks.NETHER_BRICK_SIGN_POST)
    );
    public static final BlockEntityType<?> GLOBE = register("globe",
            FabricBlockEntityTypeBuilder.create(GenericSingleItemBlockEntity::globe).addBlock(DecorationsBlocks.GLOBE));
    ;

    public static final BlockEntityType<?> DISPLAY_CASE = register("display_case",
            FabricBlockEntityTypeBuilder.create(GenericSingleItemBlockEntity::displayCase).addBlock(DecorationsBlocks.DISPLAY_CASE));
    ;

    //public static final BlockEntityType<?> BANNER_BED = register("banner_bed",
    //        FabricBlockEntityTypeBuilder.create(BedWithBannerBlockEntity::new)
    //                .addBlocks(DecorationsBlocks.BANNER_BED.values().toArray(new BedWithBannerBlock[0])));
    ;

    public static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder<T> item) {
        var x = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(ModInit.ID, path), item.build());
        PolymerBlockUtils.registerBlockEntity(x);
        return x;
    }

    public static void register() {
    }
}
