package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.furniture.WindChimeBlockEntity;
import eu.pb4.polydecorations.block.item.*;
import eu.pb4.polydecorations.block.other.GenericSingleItemBlockEntity;
import eu.pb4.polydecorations.block.extension.SignPostBlockEntity;
import eu.pb4.polydecorations.block.furniture.LongFlowerPotBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.ArrayList;

public class DecorationsBlockEntities {
    public static final BlockEntityType<?> SHELF = register("shelf", ShelfBlockEntity::new, DecorationsBlocks.SHELF.values().toArray(new PlainShelfBlock[0]));
    public static final BlockEntityType<?> SIGN_POST = register("sign_post", SignPostBlockEntity::new,
            Util.make(new ArrayList<Block>(), (l) -> {
                l.addAll(DecorationsBlocks.WOOD_SIGN_POST.values());
                l.addAll(DecorationsBlocks.WALL_SIGN_POST.values());
                l.add(DecorationsBlocks.NETHER_BRICK_SIGN_POST);
            }).toArray(new Block[0])
    );

    public static final BlockEntityType<?> MAILBOX = register("mailbox", MailboxBlockEntity::new,
            DecorationsBlocks.WOODEN_MAILBOX.values().toArray(new Block[0]));
    public static final BlockEntityType<?> GLOBE = register("globe", GenericSingleItemBlockEntity::globe, DecorationsBlocks.GLOBE);
    public static final BlockEntityType<?> TRASHCAN = register("trashcan", TrashCanBlockEntity::new, DecorationsBlocks.TRASHCAN);
    public static final BlockEntityType<?> BASKET = register("basket", BasketBlockEntity::new, DecorationsBlocks.BASKET);
    public static final BlockEntityType<?> WIND_CHIME = register("wind_chime", WindChimeBlockEntity::new, DecorationsBlocks.WIND_CHIME);

    public static final BlockEntityType<?> DISPLAY_CASE = register("display_case",
            GenericSingleItemBlockEntity::displayCase,
            DecorationsBlocks.DISPLAY_CASE
    );

    public static final BlockEntityType<?> LONG_FLOWER_POT = register("long_flower_pot",
            LongFlowerPotBlockEntity::new,
            DecorationsBlocks.LONG_FLOWER_POT
    );

    public static final BlockEntityType<?> TOOL_RACK = register("tool_rack", ToolRackBlockEntity::new,
            DecorationsBlocks.TOOL_RACK.values().toArray(new Block[0]));

    //public static final BlockEntityType<?> BANNER_BED = register("banner_bed",
    //        FabricBlockEntityTypeBuilder.create(BedWithBannerBlockEntity::new)
    //                .addBlocks(DecorationsBlocks.BANNER_BED.values().toArray(new BedWithBannerBlock[0])));
    ;

    public static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Block... blocks) {
        return register(path, FabricBlockEntityTypeBuilder.create(factory, blocks));
    }
    public static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder<T> item) {
        var x = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ModInit.ID, path), item.build());
        PolymerBlockUtils.registerBlockEntity(x);
        return x;
    }

    public static void register() {
    }
}
