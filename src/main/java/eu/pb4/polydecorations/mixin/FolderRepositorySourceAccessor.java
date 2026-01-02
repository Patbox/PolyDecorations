package eu.pb4.polydecorations.mixin;

import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.server.packs.repository.FolderRepositorySource.class)
public interface FolderRepositorySourceAccessor {
    @Accessor
    PackSource getPackSource();
}
