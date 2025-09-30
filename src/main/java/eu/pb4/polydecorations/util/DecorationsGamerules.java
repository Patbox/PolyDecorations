package eu.pb4.polydecorations.util;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public interface DecorationsGamerules {
    static GameRules.Key<GameRules.IntRule> SEAT_USE_COOLDOWN =
            GameRuleRegistry.register("polydecorations:seat_use_cooldown", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(20, 0, 200));


    static void register() {}
}
