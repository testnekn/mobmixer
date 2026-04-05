package ru.aleksey.mobmixer.hybrid;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class HybridMobFactory {
    private static final int BONUS_DURATION = 20 * 60 * 10;
    private static final List<RegistryEntry<EntityAttribute>> BLENDED_ATTRIBUTES = List.of(
        EntityAttributes.MAX_HEALTH,
        EntityAttributes.MOVEMENT_SPEED,
        EntityAttributes.ATTACK_DAMAGE,
        EntityAttributes.ARMOR,
        EntityAttributes.ATTACK_KNOCKBACK,
        EntityAttributes.KNOCKBACK_RESISTANCE,
        EntityAttributes.FOLLOW_RANGE
    );

    private HybridMobFactory() {
    }

    @Nullable
    public static MobEntity createHybrid(ServerWorld world, BlockPos pos, EntityType<?> firstParent, EntityType<?> secondParent) {
        EntityType<?> dominantParent = world.random.nextBoolean() ? firstParent : secondParent;
        Entity entity = dominantParent.create(world, SpawnReason.COMMAND);
        if (!(entity instanceof MobEntity child)) {
            return null;
        }

        child.refreshPositionAndAngles(pos, world.random.nextFloat() * 360.0F, 0.0F);
        child.initialize(world, world.getLocalDifficulty(pos), SpawnReason.COMMAND, null);
        blendTraits(world, child, firstParent, secondParent);
        child.setHealth(child.getMaxHealth());
        child.setPersistent();
        child.setCustomName(Text.translatable("entity.mobmixer.hybrid_name", firstParent.getName(), secondParent.getName()));
        child.setCustomNameVisible(true);
        child.addCommandTag("mobmixer.hybrid");
        return child;
    }

    private static void blendTraits(ServerWorld world, MobEntity child, EntityType<?> firstParent, EntityType<?> secondParent) {
        LivingEntity firstTemplate = createTemplate(world, firstParent);
        LivingEntity secondTemplate = createTemplate(world, secondParent);

        if (firstTemplate != null && secondTemplate != null) {
            for (RegistryEntry<EntityAttribute> attribute : BLENDED_ATTRIBUTES) {
                blendAttribute(child, firstTemplate, secondTemplate, attribute);
            }

            if (firstTemplate.isFireImmune() || secondTemplate.isFireImmune()) {
                child.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, BONUS_DURATION, 0));
            }

            if (isAquatic(firstParent) || isAquatic(secondParent)) {
                child.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, BONUS_DURATION, 0));
            }

            if (hasFlyingParent(firstParent) || hasFlyingParent(secondParent)) {
                child.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, BONUS_DURATION, 0));
            }

            if (hasUndeadParent(firstParent) || hasUndeadParent(secondParent)) {
                child.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, BONUS_DURATION, 0));
            }

            if (hasHostileParent(firstParent) || hasHostileParent(secondParent)) {
                child.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, BONUS_DURATION, 0));
            }

            if (hasPeacefulParents(firstParent, secondParent)) {
                child.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, BONUS_DURATION, 0));
            }
        }
    }

    @Nullable
    private static LivingEntity createTemplate(ServerWorld world, EntityType<?> parentType) {
        Entity entity = parentType.create(world, SpawnReason.COMMAND);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }

        if (livingEntity instanceof MobEntity mobEntity) {
            BlockPos pos = world.getSpawnPos();
            mobEntity.refreshPositionAndAngles(pos, 0.0F, 0.0F);
            mobEntity.initialize(world, world.getLocalDifficulty(pos), SpawnReason.COMMAND, null);
        }

        return livingEntity;
    }

    private static void blendAttribute(
        MobEntity child,
        LivingEntity firstTemplate,
        LivingEntity secondTemplate,
        RegistryEntry<EntityAttribute> attribute
    ) {
        EntityAttributeInstance childAttribute = child.getAttributeInstance(attribute);
        EntityAttributeInstance firstAttribute = firstTemplate.getAttributeInstance(attribute);
        EntityAttributeInstance secondAttribute = secondTemplate.getAttributeInstance(attribute);
        if (childAttribute == null || firstAttribute == null || secondAttribute == null) {
            return;
        }

        double blendedValue = (firstAttribute.getBaseValue() + secondAttribute.getBaseValue()) / 2.0D;
        childAttribute.setBaseValue(Math.max(0.01D, blendedValue));
    }

    private static boolean isAquatic(EntityType<?> entityType) {
        SpawnGroup spawnGroup = entityType.getSpawnGroup();
        return spawnGroup == SpawnGroup.WATER_AMBIENT
            || spawnGroup == SpawnGroup.WATER_CREATURE
            || spawnGroup == SpawnGroup.UNDERGROUND_WATER_CREATURE
            || spawnGroup == SpawnGroup.AXOLOTLS;
    }

    private static boolean hasFlyingParent(EntityType<?> entityType) {
        return entityType == EntityType.ALLAY
            || entityType == EntityType.BAT
            || entityType == EntityType.BEE
            || entityType == EntityType.BLAZE
            || entityType == EntityType.GHAST
            || entityType == EntityType.PARROT
            || entityType == EntityType.PHANTOM
            || entityType == EntityType.VEX
            || entityType == EntityType.WITHER;
    }

    private static boolean hasHostileParent(EntityType<?> entityType) {
        SpawnGroup spawnGroup = entityType.getSpawnGroup();
        return spawnGroup == SpawnGroup.MONSTER;
    }

    private static boolean hasUndeadParent(EntityType<?> entityType) {
        return entityType == EntityType.DROWNED
            || entityType == EntityType.HUSK
            || entityType == EntityType.PHANTOM
            || entityType == EntityType.SKELETON
            || entityType == EntityType.SKELETON_HORSE
            || entityType == EntityType.STRAY
            || entityType == EntityType.WITHER
            || entityType == EntityType.WITHER_SKELETON
            || entityType == EntityType.ZOGLIN
            || entityType == EntityType.ZOMBIE
            || entityType == EntityType.ZOMBIE_HORSE
            || entityType == EntityType.ZOMBIE_VILLAGER
            || entityType == EntityType.ZOMBIFIED_PIGLIN;
    }

    private static boolean hasPeacefulParents(EntityType<?> firstParent, EntityType<?> secondParent) {
        return firstParent.getSpawnGroup() != SpawnGroup.MONSTER && secondParent.getSpawnGroup() != SpawnGroup.MONSTER;
    }
}
