package com.bonsai.pixelpets.pixelpets.registration;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.LeveledAttackData;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.LeveledBuffData;
import com.bonsai.pixelpets.pixelpets.registration.data.PixelPetType;
import com.bonsai.pixelpets.pixelpets.registration.data.cosmetic.ProjectileRenderData;
import com.bonsai.pixelpets.pixelpets.registration.data.PixelPetRarity;
import com.bonsai.pixelpets.pixelpets.registration.data.spawning.SpawnData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

// TODO pet sounds could be handled here
// TODO emissive layers too, if necessary
// TODO immunities?
public record PixelPetData(
        EntityType<AbstractPixelPetEntity> entityType,
        String genericName,
        Ingredient tameItem,
        int baseHealth,
        ResourceLocation animationId,
        LeveledAttackData attack,
        LeveledBuffData buff,
        int tameChance,
        List<HolderSet<EntityType<?>>> scares,
        PixelPetRarity rarity,
        List<PixelPetType> typings,
        SpawnData spawnData,
        ProjectileRenderData projectileRenderData
) {

    public static final String DEFAULT_GENERIC_NAME = "Unnamed";
    public static final Ingredient DEFAULT_TAME_ITEM = Ingredient.of(Items.APPLE);
    public static final int DEFAULT_BASE_HEALTH = 20;
    public static final ResourceLocation DEFAULT_ANIMATION_ID = PixelPets.id("default_pet");
    public static final LeveledAttackData DEFAULT_ATTACK = new LeveledAttackData(new TreeMap<>());
    public static final LeveledBuffData DEFAULT_BUFF = new LeveledBuffData(new TreeMap<>());
    public static final int DEFAULT_TAME_CHANCE = 25;
    public static final PixelPetRarity DEFAULT_RARITY = PixelPetRarity.COMMON;
    public static final List<PixelPetType> DEFAULT_PET_TYPES = List.of();
    public static final SpawnData DEFAULT_SPAWN_DATA = new SpawnData();
    public static final ProjectileRenderData DEFAULT_PROJECTILE_RENDER_DATA = new ProjectileRenderData.ItemBased(() -> Items.SNOWBALL, 1.0f, false);

    public static final Codec<PixelPetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC
                    .flatXmap(
                            rl -> Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.get(rl))
                                    .map(t -> (EntityType<AbstractPixelPetEntity>) t)
                                    .map(DataResult::success)
                                    .orElseGet(() -> DataResult.error(() -> "Unknown entity type: " + rl)),
                            type -> DataResult.success(BuiltInRegistries.ENTITY_TYPE.getKey(type))
                    )
                    .fieldOf("entity_type")
                    .forGetter(PixelPetData::entityType),

            Codec.STRING.optionalFieldOf("generic_name", DEFAULT_GENERIC_NAME)
                    .forGetter(PixelPetData::genericName),

            Ingredient.CODEC.optionalFieldOf("tame_item", DEFAULT_TAME_ITEM)
                    .forGetter(PixelPetData::tameItem),

            ExtraCodecs.intRange(1, 100).optionalFieldOf("base_health", DEFAULT_BASE_HEALTH)
                    .forGetter(PixelPetData::baseHealth),

            ResourceLocation.CODEC.optionalFieldOf("animation_id", DEFAULT_ANIMATION_ID)
                    .forGetter(PixelPetData::animationId),

            LeveledAttackData.CODEC.optionalFieldOf("attack", DEFAULT_ATTACK)
                    .forGetter(PixelPetData::attack),

            LeveledBuffData.CODEC.optionalFieldOf("buff", DEFAULT_BUFF)
                    .forGetter(PixelPetData::buff),

            ExtraCodecs.intRange(1, 100).optionalFieldOf("tame_chance", DEFAULT_TAME_CHANCE)
                    .forGetter(PixelPetData::tameChance),

            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).listOf().optionalFieldOf("scares", List.of())
                    .forGetter(PixelPetData::scares),

            Codec.STRING.optionalFieldOf("rarities")
                    .xmap(s -> s.map(PixelPetRarity::byName).orElse(DEFAULT_RARITY),
                            r -> Optional.of(r.name().toLowerCase()))
                    .forGetter(PixelPetData::rarity),

            PixelPetType.CODEC.listOf().optionalFieldOf("typings", DEFAULT_PET_TYPES)
                            .forGetter(PixelPetData::typings),

            SpawnData.CODEC.optionalFieldOf("spawn_data", DEFAULT_SPAWN_DATA)
                    .forGetter(PixelPetData::spawnData),

            ProjectileRenderData.CODEC.optionalFieldOf("projectile_render", DEFAULT_PROJECTILE_RENDER_DATA)
                    .forGetter(PixelPetData::projectileRenderData)

            ).apply(instance, PixelPetData::new));

    // Was different to CODEC, but it's just so nice having all the data available for both
    public static final Codec<PixelPetData> CLIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC
                    .flatXmap(
                            rl -> Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.get(rl))
                                    .map(t -> (EntityType<AbstractPixelPetEntity>) t)
                                    .map(DataResult::success)
                                    .orElseGet(() -> DataResult.error(() -> "Unknown entity type: " + rl)),
                            type -> DataResult.success(BuiltInRegistries.ENTITY_TYPE.getKey(type))
                    )
                    .fieldOf("entity_type")
                    .forGetter(PixelPetData::entityType),

            Codec.STRING.optionalFieldOf("generic_name", DEFAULT_GENERIC_NAME)
                    .forGetter(PixelPetData::genericName),

            Ingredient.CODEC.optionalFieldOf("tame_item", DEFAULT_TAME_ITEM)
                    .forGetter(PixelPetData::tameItem),

            ExtraCodecs.intRange(1, 100).optionalFieldOf("base_health", DEFAULT_BASE_HEALTH)
                    .forGetter(PixelPetData::baseHealth),

            ResourceLocation.CODEC.optionalFieldOf("animation_id", DEFAULT_ANIMATION_ID)
                    .forGetter(PixelPetData::animationId),

            LeveledAttackData.CODEC.optionalFieldOf("attack", DEFAULT_ATTACK)
                    .forGetter(PixelPetData::attack),

            LeveledBuffData.CODEC.optionalFieldOf("buff", DEFAULT_BUFF)
                    .forGetter(PixelPetData::buff),

            ExtraCodecs.intRange(1, 100).optionalFieldOf("tame_chance", DEFAULT_TAME_CHANCE)
                    .forGetter(PixelPetData::tameChance),

            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).listOf().optionalFieldOf("scares", List.of())
                    .forGetter(PixelPetData::scares),

            Codec.STRING.optionalFieldOf("rarities")
                    .xmap(s -> s.map(PixelPetRarity::byName).orElse(DEFAULT_RARITY),
                            r -> Optional.of(r.name().toLowerCase()))
                    .forGetter(PixelPetData::rarity),

            PixelPetType.CODEC.listOf().optionalFieldOf("typings", DEFAULT_PET_TYPES)
                    .forGetter(PixelPetData::typings),

            SpawnData.CODEC.optionalFieldOf("spawn_data", DEFAULT_SPAWN_DATA)
                    .forGetter(PixelPetData::spawnData),

            ProjectileRenderData.CODEC.optionalFieldOf("projectile_render", DEFAULT_PROJECTILE_RENDER_DATA)
                    .forGetter(PixelPetData::projectileRenderData)

    ).apply(instance, PixelPetData::new));


}
