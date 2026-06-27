package com.bonsai.pixelpets.pixelpets.registration;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.data.LeveledAttackData;
import com.bonsai.pixelpets.pixelpets.registration.data.Rarity;
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

// TODO add spawning biome stuff?
// TODO typing (on hold, josh deciding)

// TODO pet sounds could be handled here
// TODO emissive layers too, if necessary
public record PixelPetData(
        ResourceLocation id,
        EntityType<AbstractPixelPetEntity> entityType,
        String genericName,
        Ingredient tameItem,
        int baseHealth,
        ResourceLocation animationId,
        LeveledAttackData attack,
        int tameChance,
        List<HolderSet<EntityType<?>>> scares,
        Rarity rarity
) {

    public static final String DEFAULT_GENERIC_NAME = "Unnamed";
    public static final Ingredient DEFAULT_TAME_ITEM = Ingredient.of(Items.APPLE);
    public static final int DEFAULT_BASE_HEALTH = 20;
    public static final ResourceLocation DEFAULT_ANIMATION_ID = PixelPets.identifier("default_pet");
    public static final LeveledAttackData DEFAULT_ATTACK = new LeveledAttackData(new TreeMap<>());
    public static final int DEFAULT_TAME_CHANCE = 25;
    public static final Rarity DEFAULT_RARITY = Rarity.COMMON;

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

            ExtraCodecs.intRange(1, 100).optionalFieldOf("tame_chance", DEFAULT_TAME_CHANCE)
                    .forGetter(PixelPetData::tameChance),

            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).listOf().optionalFieldOf("scares", List.of())
                    .forGetter(PixelPetData::scares),

            Codec.STRING.optionalFieldOf("rarity")
                    .xmap(s -> s.map(Rarity::byName).orElse(DEFAULT_RARITY),
                            r -> Optional.of(r.name().toLowerCase()))
                    .forGetter(PixelPetData::rarity)

            ).apply(instance, (entityType,genericName, tameItem, baseHealth, animationId, attack, tameChance, scares, rarity) -> {
                return new PixelPetData(
                        null,
                        entityType,
                        genericName,
                        tameItem,
                        baseHealth,
                        animationId,
                        attack,
                        tameChance,
                        scares,
                        rarity
                );
            })
    );


}
