package eu.pb4.polydecorations.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


class AdvancementsProvider extends FabricAdvancementProvider {


    protected AdvancementsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    //@Override
    public void generateAdvancement(Consumer<AdvancementEntry> exporter) {
        /*var root = Advancement.Builder.create()
                .display(
                        FactoryItems.WINDMILL_SAIL,
                        Text.translatable("advancements.polyfactory.root.title"),
                        Text.translatable("advancements.polyfactory.root.description"),
                        id("textures/advancements/background.png"),
                        AdvancementFrame.TASK,
                        false,
                        false,
                        false
                )
                .criterion("any_item", InventoryChangedCriterion.Conditions.items(
                        ItemPredicate.Builder.create().tag(FactoryItemTags.ROOT_ADVANCEMENT)
                ))
                .build(exporter, "polyfactory:main/root");
*/
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup registryLookup, Consumer<AdvancementEntry> consumer) {

    }
}
