package ru.aleksey.mobmixer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aleksey.mobmixer.item.HybridSpawnEggItem;
import ru.aleksey.mobmixer.recipe.HybridSpawnEggRecipe;

public class MobMixerMod implements ModInitializer {
    public static final String MOD_ID = "mobmixer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final HybridSpawnEggItem HYBRID_SPAWN_EGG = Registry.register(
        Registries.ITEM,
        id("hybrid_spawn_egg"),
        new HybridSpawnEggItem()
    );

    public static final RecipeSerializer<HybridSpawnEggRecipe> HYBRID_SPAWN_EGG_RECIPE = Registry.register(
        Registries.RECIPE_SERIALIZER,
        id("hybrid_spawn_egg"),
        new SpecialCraftingRecipe.SpecialRecipeSerializer<>(HybridSpawnEggRecipe::new)
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(HYBRID_SPAWN_EGG));
        LOGGER.info("Mob Mixer loaded");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
