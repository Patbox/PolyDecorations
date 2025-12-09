package eu.pb4.polydecorations.util;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public interface DecorationsGamerules {
    static GameRule<Integer> SEAT_USE_COOLDOWN = GameRuleBuilder.forInteger(20).range(0, 200)
            .category(GameRuleCategory.PLAYER).buildAndRegister(Identifier.parse("polydecorations:seat_use_cooldown"));


    static void register() {}
}
