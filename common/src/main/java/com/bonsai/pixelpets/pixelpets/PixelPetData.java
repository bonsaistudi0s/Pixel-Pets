package com.bonsai.pixelpets.pixelpets;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

// TODO add spawning biome stuff?
// TODO typing (on hold, joosh deciding)
public record PixelPetData(
        ResourceLocation id,
        EntityType<AbstractPixelPetEntity> entityType,
        String genericName,
        Item tameItem,
        int baseHealth,
        ResourceLocation animationId,
        boolean attacking,
        int tameChance,
        List<EntityType<?>> scares, // TODO this will be a bit challenging to implement since scare code is in the scared entity
        PixelPetRarity rarity
) {

    public static final String DEFAULT_GENERIC_NAME = "Unnamed";
    public static final Item DEFAULT_TAME_ITEM = Items.APPLE;
    public static final int DEFAULT_BASE_HEALTH = 20;
    public static final ResourceLocation DEFAULT_ANIMATION_ID = PixelPets.identifier("default_pet");
    public static final boolean DEFAULT_ATTACKING = false;
    public static final int DEFAULT_TAME_CHANCE = 25;
    public static final PixelPetRarity DEFAULT_RARITY = PixelPetRarity.COMMON;

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

            BuiltInRegistries.ITEM.byNameCodec()
                    .optionalFieldOf("tame_item", DEFAULT_TAME_ITEM)
                    .forGetter(PixelPetData::tameItem),

            ExtraCodecs.intRange(1, 100).optionalFieldOf("base_health", DEFAULT_BASE_HEALTH)
                    .forGetter(PixelPetData::baseHealth),

            ResourceLocation.CODEC.optionalFieldOf("animation_id", DEFAULT_ANIMATION_ID)
                    .forGetter(PixelPetData::animationId),

            Codec.BOOL.optionalFieldOf("attacking", DEFAULT_ATTACKING)
                    .forGetter(PixelPetData::attacking),

            ExtraCodecs.intRange(1, 100).optionalFieldOf("tame_chance", DEFAULT_TAME_CHANCE)
                    .forGetter(PixelPetData::tameChance),


            BuiltInRegistries.ENTITY_TYPE.byNameCodec()
                    .listOf()
                    .optionalFieldOf("scares", List.of())
                    .forGetter(PixelPetData::scares),

            Codec.STRING.optionalFieldOf("rarity")
                    .xmap(s -> s.map(PixelPetRarity::byName).orElse(DEFAULT_RARITY),
                            r -> Optional.of(r.name().toLowerCase()))
                    .forGetter(PixelPetData::rarity)

            ).apply(instance, (entityType,genericName, tameItem, baseHealth, animationId, attacking, tameChance, scares, rarity) -> {
                return new PixelPetData(
                        null,
                        entityType,
                        genericName,
                        tameItem,
                        baseHealth,
                        animationId,
                        attacking,
                        tameChance,
                        scares,
                        rarity
                );
            })
    );
}
