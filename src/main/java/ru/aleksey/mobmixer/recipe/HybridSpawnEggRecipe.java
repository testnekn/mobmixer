package ru.aleksey.mobmixer.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import ru.aleksey.mobmixer.MobMixerMod;
import ru.aleksey.mobmixer.hybrid.HybridSpawnData;

public class HybridSpawnEggRecipe extends SpecialCraftingRecipe {
    public HybridSpawnEggRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput inventory, World world) {
        return !getCraftingResult(inventory).isEmpty();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, RegistryWrapper.WrapperLookup registryLookup) {
        return getCraftingResult(inventory);
    }

    @Override
    public RecipeSerializer<HybridSpawnEggRecipe> getSerializer() {
        return MobMixerMod.HYBRID_SPAWN_EGG_RECIPE;
    }

    private ItemStack getCraftingResult(CraftingRecipeInput inventory) {
        ItemStack firstEgg = ItemStack.EMPTY;
        ItemStack secondEgg = ItemStack.EMPTY;
        boolean hasCatalyst = false;

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.isOf(Items.SLIME_BALL)) {
                if (hasCatalyst) {
                    return ItemStack.EMPTY;
                }
                hasCatalyst = true;
                continue;
            }

            if (!(stack.getItem() instanceof SpawnEggItem)) {
                return ItemStack.EMPTY;
            }

            if (firstEgg.isEmpty()) {
                firstEgg = stack;
            } else if (secondEgg.isEmpty()) {
                secondEgg = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (!hasCatalyst || firstEgg.isEmpty() || secondEgg.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!(firstEgg.getItem() instanceof SpawnEggItem firstSpawnEgg) || !(secondEgg.getItem() instanceof SpawnEggItem secondSpawnEgg)) {
            return ItemStack.EMPTY;
        }

        if (firstSpawnEgg.getEntityType(null) == secondSpawnEgg.getEntityType(null)) {
            return ItemStack.EMPTY;
        }

        return HybridSpawnData.createEgg(
            firstSpawnEgg.getEntityType(null),
            secondSpawnEgg.getEntityType(null)
        );
    }
}
