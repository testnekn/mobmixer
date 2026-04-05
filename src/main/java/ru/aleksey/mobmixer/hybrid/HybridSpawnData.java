package ru.aleksey.mobmixer.hybrid;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import ru.aleksey.mobmixer.MobMixerMod;

public final class HybridSpawnData {
    public static final String FIRST_PARENT_KEY = "FirstParent";
    public static final String SECOND_PARENT_KEY = "SecondParent";

    private HybridSpawnData() {
    }

    public static ItemStack createEgg(EntityType<?> firstParent, EntityType<?> secondParent) {
        ItemStack stack = new ItemStack(MobMixerMod.HYBRID_SPAWN_EGG);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            nbt.putString(FIRST_PARENT_KEY, Registries.ENTITY_TYPE.getId(firstParent).toString());
            nbt.putString(SECOND_PARENT_KEY, Registries.ENTITY_TYPE.getId(secondParent).toString());
        });
        return stack;
    }

    public static boolean hasParents(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        return nbt != null && nbt.contains(FIRST_PARENT_KEY) && nbt.contains(SECOND_PARENT_KEY);
    }

    @Nullable
    public static EntityType<?> getFirstParent(ItemStack stack) {
        return getEntityType(stack, FIRST_PARENT_KEY);
    }

    @Nullable
    public static EntityType<?> getSecondParent(ItemStack stack) {
        return getEntityType(stack, SECOND_PARENT_KEY);
    }

    public static Text getFirstParentName(ItemStack stack) {
        return getParentName(stack, FIRST_PARENT_KEY);
    }

    public static Text getSecondParentName(ItemStack stack) {
        return getParentName(stack, SECOND_PARENT_KEY);
    }

    private static Text getParentName(ItemStack stack, String key) {
        EntityType<?> entityType = getEntityType(stack, key);
        return entityType == null ? Text.translatable("item.mobmixer.unknown_parent") : entityType.getName();
    }

    @Nullable
    private static EntityType<?> getEntityType(ItemStack stack, String key) {
        NbtCompound nbt = getCustomData(stack);
        if (nbt == null || !nbt.contains(key)) {
            return null;
        }

        Identifier id = Identifier.tryParse(nbt.getString(key).orElse(""));
        if (id == null || !Registries.ENTITY_TYPE.containsId(id)) {
            return null;
        }

        return Registries.ENTITY_TYPE.get(id);
    }

    @Nullable
    private static NbtCompound getCustomData(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        return customData == null ? null : customData.copyNbt();
    }
}
