package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions.*;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;

public class ModTargetConditions {

    public static final ResourcefulRegistry<TargetConditionType<?>> TYPES =
            ResourcefulRegistries.create(PixelPets.TARGET_CONDITION_TYPES, PixelPets.MOD_ID);

    public static final RegistryEntry<TargetConditionType<InWaterCondition>> IN_WATER =
            TYPES.register("in_water", () -> new TargetConditionType<>(InWaterCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<InLavaCondition>> IN_LAVA =
            TYPES.register("in_lava", () -> new TargetConditionType<>(InLavaCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsWetCondition>> IS_WET =
            TYPES.register("is_wet", () -> new TargetConditionType<>(IsWetCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsRainingCondition>> IS_RAINING =
            TYPES.register("is_raining", () -> new TargetConditionType<>(IsRainingCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsThunderingCondition>> IS_THUNDERING =
            TYPES.register("is_thundering", () -> new TargetConditionType<>(IsThunderingCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsDayCondition>> IS_DAY =
            TYPES.register("is_day", () -> new TargetConditionType<>(IsDayCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsNightCondition>> IS_NIGHT =
            TYPES.register("is_night", () -> new TargetConditionType<>(IsNightCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsTimeCondition>> IS_TIME =
            TYPES.register("is_time", () -> new TargetConditionType<>(IsTimeCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsHeightCondition>> IS_HEIGHT =
            TYPES.register("is_height", () -> new TargetConditionType<>(IsHeightCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<InBiomeCondition>> IN_BIOME =
            TYPES.register("in_biome", () -> new TargetConditionType<>(InBiomeCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<InHotBiomeCondition>> IN_HOT_BIOME =
            TYPES.register("in_hot_biome", () -> new TargetConditionType<>(InHotBiomeCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<InColdBiomeCondition>> IN_COLD_BIOME =
            TYPES.register("in_cold_biome", () -> new TargetConditionType<>(InColdBiomeCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<OnBlockCondition>> ON_BLOCK =
            TYPES.register("on_block", () -> new TargetConditionType<>(OnBlockCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsSprintingCondition>> IS_SPRINTING =
            TYPES.register("is_sprinting", () -> new TargetConditionType<>(IsSprintingCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsCrouchingCondition>> IS_CROUCHING =
            TYPES.register("is_crouching", () -> new TargetConditionType<>(IsCrouchingCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsSwimmingCondition>> IS_SWIMMING =
            TYPES.register("is_swimming", () -> new TargetConditionType<>(IsSwimmingCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<OnFireCondition>> ON_FIRE =
            TYPES.register("on_fire", () -> new TargetConditionType<>(OnFireCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<AlwaysTrueCondition>> ALWAYS_TRUE =
            TYPES.register("always_true", () -> new TargetConditionType<>(AlwaysTrueCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<HealthPercentCondition>> HEALTH_PERCENT =
            TYPES.register("health_percent", () -> new TargetConditionType<>(HealthPercentCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<HasStatusEffectCondition>> HAS_STATUS_EFFECT =
            TYPES.register("has_status_effect", () -> new TargetConditionType<>(HasStatusEffectCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<HasNegativeEffectCondition>> HAS_NEGATIVE_EFFECT =
            TYPES.register("has_negative_effect", () -> new TargetConditionType<>(HasNegativeEffectCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsPetRangedCondition>> IS_PET_RANGED =
            TYPES.register("is_pet_ranged", () -> new TargetConditionType<>(IsPetRangedCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsPetMeleeCondition>> IS_PET_MELEE =
            TYPES.register("is_pet_melee", () -> new TargetConditionType<>(IsPetMeleeCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsPetRarityCondition>> IS_PET_RARITY =
            TYPES.register("is_pet_rarity", () -> new TargetConditionType<>(IsPetRarityCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<IsPetTypeCondition>> IS_PET_TYPE =
            TYPES.register("is_pet_type", () -> new TargetConditionType<>(IsPetTypeCondition.CODEC));

    public static final RegistryEntry<TargetConditionType<PlayerPetsEquippedCondition>> PLAYER_PETS_EQUIPPED =
            TYPES.register("player_pets_equipped", () -> new TargetConditionType<>(PlayerPetsEquippedCondition.CODEC));


}
