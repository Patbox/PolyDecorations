package eu.pb4.polydecorations.block;

import eu.pb4.polydecorations.DecorationsFeature;
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
import java.util.Arrays;
import java.util.Objects;

import static eu.pb4.polydecorations.ModInit.id;

public class DecorationsBlockEntities {
    public static final BlockEntityType<?> SHELF = DecorationsFeature.SHELF.isRegistered() ? register("shelf", ShelfBlockEntity::new) : null;
    public static final BlockEntityType<?> SIGN_POST = DecorationsFeature.SIGN_POST.isRegistered() ? register("sign_post", SignPostBlockEntity::new) : null;

    public static final BlockEntityType<?> MAILBOX = DecorationsFeature.MAILBOX.isRegistered() ? register("mailbox", MailboxBlockEntity::new) : null;
    public static final BlockEntityType<?> GLOBE = DecorationsFeature.GLOBE.isRegistered() ? register("globe", GenericSingleItemBlockEntity::globe, DecorationsBlocks.GLOBE) : null;
    public static final BlockEntityType<?> TRASHCAN = DecorationsFeature.TRASHCAN.isRegistered() ? register("trashcan", TrashCanBlockEntity::new, DecorationsBlocks.TRASHCAN) : null;
    public static final BlockEntityType<?> GENERIC_PICKABLE_STORAGE = DecorationsFeature.anyRegistered(DecorationsFeature.BASKET, DecorationsFeature.CARDBOARD_BOX)
            ? register("generic_pickable_storage", PickableItemContainerBlockEntity::new, DecorationsBlocks.BASKET, DecorationsBlocks.CARDBOARD_BOX) : null;
    public static final BlockEntityType<?> WIND_CHIME = DecorationsFeature.WIND_CHIME.isRegistered() ? register("wind_chime", WindChimeBlockEntity::new, DecorationsBlocks.WIND_CHIME) : null;

    public static final BlockEntityType<?> DISPLAY_CASE = DecorationsFeature.DISPLAY_CASE.isRegistered() ? register("display_case",
            GenericSingleItemBlockEntity::displayCase,
            DecorationsBlocks.DISPLAY_CASE
    ) : null;

    public static final BlockEntityType<?> LONG_FLOWER_POT = DecorationsFeature.FLOWER_POTS.isRegistered() ? register("long_flower_pot",
            LongFlowerPotBlockEntity::new,
            DecorationsBlocks.LONG_FLOWER_POT
    ) : null;

    public static final BlockEntityType<?> TOOL_RACK = DecorationsFeature.TOOL_RACK.isRegistered() ? register("tool_rack", ToolRackBlockEntity::new) : null;

    //public static final BlockEntityType<?> BANNER_BED = register("banner_bed",
    //        FabricBlockEntityTypeBuilder.create(BedWithBannerBlockEntity::new)
    //                .addBlocks(DecorationsBlocks.BANNER_BED.values().toArray(new BedWithBannerBlock[0])));
    ;

    public static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Block... blocks) {
        return register(path, FabricBlockEntityTypeBuilder.create(factory, Arrays.stream(blocks).filter(Objects::nonNull).toArray(Block[]::new)));
    }
    public static <T extends BlockEntity> BlockEntityType<T> register(String path, FabricBlockEntityTypeBuilder<T> item) {
        var x = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ModInit.ID, path), item.build());
        PolymerBlockUtils.registerBlockEntity(x);
        return x;
    }

    public static void register() {
        if (GENERIC_PICKABLE_STORAGE != null) {
            BuiltInRegistries.BLOCK_ENTITY_TYPE.addAlias(id("basket"), id("generic_pickable_storage"));
        }
    }
}
