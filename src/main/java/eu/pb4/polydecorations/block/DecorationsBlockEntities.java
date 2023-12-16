package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.furniture.ShelfBlock;
import eu.pb4.polydecorations.block.furniture.ShelfBlockEntity;
import eu.pb4.polydecorations.block.other.GlobeBlockEntity;
import eu.pb4.polydecorations.block.plus.BedWithBannerBlock;
import eu.pb4.polydecorations.block.plus.BedWithBannerBlockEntity;
import eu.pb4.polydecorations.block.plus.SignPostBlock;
import eu.pb4.polydecorations.block.plus.SignPostBlockEntity;
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
                    .addBlocks(DecorationsBlocks.SIGN_POST.values().toArray(new SignPostBlock[0])));
    public static final BlockEntityType<?> GLOBE = register("globe",
            FabricBlockEntityTypeBuilder.create(GlobeBlockEntity::new).addBlock(DecorationsBlocks.GLOBE));
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
