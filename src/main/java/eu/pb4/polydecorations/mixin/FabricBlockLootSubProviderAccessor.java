package eu.pb4.polydecorations.mixin;

import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@org.spongepowered.asm.mixin.Mixin(net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider.class)
public interface FabricBlockLootSubProviderAccessor {
    @Accessor
    CompletableFuture<HolderLookup.Provider> getRegistriesFuture();
}
