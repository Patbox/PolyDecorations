package eu.pb4.polydecorations.model;

import static eu.pb4.polydecorations.ModInit.id;

public interface DecorationsModels {
    DirectionConnectingModel ROPE = new DirectionConnectingModel(id("block/rope"));
    static void register() {

    }
}
