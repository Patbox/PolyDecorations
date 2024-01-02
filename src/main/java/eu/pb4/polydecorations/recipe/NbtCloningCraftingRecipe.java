package eu.pb4.polydecorations.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;

import static eu.pb4.polydecorations.ModInit.id;

public record NbtCloningCraftingRecipe(String group, Item input) implements CraftingRecipe {
    public static final Codec<NbtCloningCraftingRecipe> CODEC = RecordCodecBuilder.create(x -> x.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(NbtCloningCraftingRecipe::group),
                    Registries.ITEM.getCodec().fieldOf("input").forGetter(NbtCloningCraftingRecipe::input)
            ).apply(x, NbtCloningCraftingRecipe::new)
    );


    public static RecipeEntry<NbtCloningCraftingRecipe> of(String id, Item item) {
        return new RecipeEntry<>(id(id), new NbtCloningCraftingRecipe("", item));
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return CraftingRecipeCategory.MISC;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        boolean hasNbt = false;
        int count = 0;

        for (var stack : inventory.getHeldStacks()) {
            if (stack.isOf(this.input)) {
                if (hasNbt && stack.hasNbt()) {
                    return false;
                }
                hasNbt |= stack.hasNbt();
                count++;
            }
        }
        return hasNbt && count > 1;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        NbtCompound nbt = null;
        int count = 0;
        for (var stack : inventory.getHeldStacks()) {
            if (stack.isOf(this.input)) {
                if (stack.hasNbt()) {
                    nbt = stack.getNbt();
                }
                count++;
            }
        }
        var stack = new ItemStack(this.input, count);
        stack.setNbt(nbt);
        return stack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height > 1;
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registryManager) {
        return input.getDefaultStack();
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return DecorationsRecipeSerializers.NBT_CLONING;
    }
}
