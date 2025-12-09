package eu.pb4.polydecorations.util;

import static eu.pb4.polydecorations.ModInit.id;

import net.minecraft.sounds.SoundEvent;

public interface DecorationSoundEvents {
    SoundEvent CANVAS_PLACE = of("entity.canvas.place");
    SoundEvent CANVAS_BREAK = of("entity.canvas.break");
    SoundEvent TRASHCAN_OPEN = of("block.trashcan.open");
    SoundEvent TRASHCAN_CLOSE = of("block.trashcan.close");
    SoundEvent TRASHCAN_CLEAR = of("block.trashcan.clear");
    SoundEvent BASKET_OPEN = of("block.basket.open");
    SoundEvent BASKET_CLOSE = of("block.basket.close");

    static SoundEvent of(String string) {
        return SoundEvent.createVariableRangeEvent(id(string));
    }

    static SoundEvent of(String string, float distanceToTravel) {
        return SoundEvent.createFixedRangeEvent(id(string), distanceToTravel);
    }
}
