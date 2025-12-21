package eu.pb4.polydecorations.util;

import static eu.pb4.polydecorations.ModInit.id;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public interface DecorationsSoundEvents {
    SoundEvent CANVAS_PLACE = of("entity.canvas.place");
    SoundEvent CANVAS_BREAK = of("entity.canvas.break");
    SoundEvent TRASHCAN_OPEN = of("block.trashcan.open");
    SoundEvent TRASHCAN_CLOSE = of("block.trashcan.close");
    SoundEvent TRASHCAN_CLEAR = of("block.trashcan.clear");
    SoundEvent BASKET_OPEN = of("block.basket.open");
    SoundEvent BASKET_CLOSE = of("block.basket.close");
    SoundEvent CARDBOARD_BOX_OPEN = of("block.cardboard_box.open");
    SoundEvent CARDBOARD_BOX_CLOSE = of("block.cardboard_box.close");
    SoundType CARDBOARD = new SoundType(0.6f, 1.44f, SoundType.WOOD.getBreakSound(), SoundType.WOOD.getStepSound(), SoundType.WOOD.getPlaceSound(), SoundType.WOOD.getHitSound(), SoundType.WOOD.getFallSound());
    static SoundEvent of(String string) {
        return SoundEvent.createVariableRangeEvent(id(string));
    }

    static SoundEvent of(String string, float distanceToTravel) {
        return SoundEvent.createFixedRangeEvent(id(string), distanceToTravel);
    }
}
