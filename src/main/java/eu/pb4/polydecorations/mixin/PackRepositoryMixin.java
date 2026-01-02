package eu.pb4.polydecorations.mixin;

import eu.pb4.polydecorations.util.ModdedCompat;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = PackRepository.class, priority = 1050)
public abstract class PackRepositoryMixin {
	@Shadow
	@Final
	private Set<RepositorySource> sources;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void construct(RepositorySource[] resourcePackProviders, CallbackInfo info) {
		boolean isServer = false;

		for (RepositorySource provider : this.sources) {
			if (provider instanceof FolderRepositorySourceAccessor folderRepositorySource
					&& (folderRepositorySource.getPackSource() == PackSource.WORLD
					|| folderRepositorySource.getPackSource() == PackSource.SERVER)) {
				isServer = true;
				break;
			}
		}

		// On server, add the mod resource pack provider.
		if (isServer) {
			this.sources.add(new ModdedCompat.VirtualPackSource(PackType.SERVER_DATA));
		}
	}
}