package eu.pb4.polydecorations.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.data.loot.BlockLootSubProvider.class)
public interface BlockLootSubProviderAccessor {
    @Accessor
    Map<ResourceKey<LootTable>, LootTable.Builder> getMap();
}
