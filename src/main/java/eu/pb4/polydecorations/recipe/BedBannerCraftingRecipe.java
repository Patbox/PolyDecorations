package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polydecorations.block.plus.BedWithBannerBlock;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

import java.util.Optional;

public record BedBannerCraftingRecipe(Item input, Item output) implements CraftingRecipe {
    public static Codec<BedBannerCraftingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Registries.ITEM.getCodec().fieldOf("input").forGetter(BedBannerCraftingRecipe::input),
        Registries.ITEM.getCodec().fieldOf("output").forGetter(BedBannerCraftingRecipe::output)
    ).apply(instance, BedBannerCraftingRecipe::new));
    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.BUILDING;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        int beds = 0;
        int banners = 0;

        for (var stack : inventory.getHeldStacks()) {
            if (stack.isOf(input)) {
                beds++;
            }
            if (stack.getItem() instanceof BannerItem) {
                banners++;
            }
        }
        return beds == 1 && banners == 1;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        var found = ItemStack.EMPTY;
        for (var x : inventory.getHeldStacks()) {
            if (x.getItem() instanceof BannerItem) {
                found = x;
                break;
            }
        }
        var stack = this.output.getDefaultStack();
        BedWithBannerBlock.withBanner(stack, found);
        return stack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width*height >= 2;
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registryManager) {
        return output.getDefaultStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DecorationsRecipeSerializers.BED_BANNER;
    }
}
