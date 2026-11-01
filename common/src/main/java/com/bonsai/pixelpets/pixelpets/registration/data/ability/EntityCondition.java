package com.bonsai.pixelpets.pixelpets.registration.data.ability;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.pixelpets.registration.data.PixelPetRarity;
import com.bonsai.pixelpets.pixelpets.registration.data.PixelPetType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// Specify the data you need, ignore the rest
public record EntityCondition(Type type,
                              boolean invert,
                              double min,
                              double max,
                              Optional<HolderSet<MobEffect>> statusEffects,
                              Optional<HolderSet<Biome>> biomes,
                              Optional<HolderSet<Block>> blocks,
                              Set<PixelPetRarity> rarities,
                              Set<PixelPetType> types
) {

    public static final Codec<EntityCondition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Type.CODEC.fieldOf("type").forGetter(EntityCondition::type),
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(EntityCondition::invert),
            Codec.DOUBLE.optionalFieldOf("min", -Double.MAX_VALUE).forGetter(EntityCondition::min),
            Codec.DOUBLE.optionalFieldOf("max", Double.MAX_VALUE).forGetter(EntityCondition::max),
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT)
                    .optionalFieldOf("status_effects")
                    .forGetter(EntityCondition::statusEffects),
            RegistryCodecs.homogeneousList(Registries.BIOME)
                    .optionalFieldOf("biomes")
                    .forGetter(EntityCondition::biomes),
            RegistryCodecs.homogeneousList(Registries.BLOCK)
                    .optionalFieldOf("blocks")
                    .forGetter(EntityCondition::blocks),
            PixelPetRarity.CODEC.listOf()
                    .xmap(Set::copyOf, List::copyOf)
                    .optionalFieldOf("rarities", Set.of(PixelPetRarity.values()))
                    .forGetter(EntityCondition::rarities),
            PixelPetType.CODEC.listOf()
                    .xmap(Set::copyOf, List::copyOf)
                    .optionalFieldOf("types", Set.of(PixelPetType.values()))
                    .forGetter(EntityCondition::types)
    ).apply(i, EntityCondition::new));

    public boolean test(LivingEntity entity) {
        return invert != type.test(entity, this);
    }

    public enum Type implements StringRepresentable {
        // Pet specific, will fail for player
        IS_PET_RANGED {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return (entity instanceof AbstractPixelPetEntity pet && pet.getRange() > 0.0f);
            }
        },
        IS_PET_MELEE {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return (entity instanceof AbstractPixelPetEntity pet && pet.getRange() <= 0.0f);
            }
        },
        IS_PET_RARITY {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                if (entity instanceof AbstractPixelPetEntity pet && pet.getData().isPresent()) {
                    return entityCondition.rarities.contains(pet.getData().get().rarity());
                }
                return false;
            }
        },
        IS_PET_ANY_TYPES {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                if (entity instanceof AbstractPixelPetEntity pet && pet.getData().isPresent()) {
                    return pet.getData().get().typings().stream().anyMatch(entityCondition.types::contains);
                }
                return false;
            }
        },
        IS_PET_ALL_TYPES {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                if (entity instanceof AbstractPixelPetEntity pet && pet.getData().isPresent()) {
                    return entityCondition.types.containsAll(pet.getData().get().typings());
                }
                return false;
            }
        },

        // Player specific
        PLAYER_PETS_EQUIPPED { // TODO make sure this works
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                if (entity instanceof PlayerPetAccess petAccess) {
                    int numPetsEquipped = petAccess.pixelPets$getActivePets().size();
                    return numPetsEquipped >= entityCondition.min && numPetsEquipped <= entityCondition.max;
                }
                return false;
            }
        },

        // Any entity compatible
        ALWAYS_TRUE {
            // Should only be used if previous pet levels aren't ALWAYS_TRUE (empty conditions defaults to this)
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return true;
            }
        },
        HEALTH_PERCENT {
            // For "HP greater than 20%", use min=20
            // For "HP less than 80%", use max=80
            // Precise interval possible with both
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                double hpPercent = 100.0 * Math.max(0, entity.getHealth() / entity.getMaxHealth());
                return hpPercent >= entityCondition.min && hpPercent <= entityCondition.max;
            }
        },
        HAS_STATUS_EFFECT {
            // Has at least one specified effect
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                if (entityCondition.statusEffects.isEmpty()) return true;

                var effects = entityCondition.statusEffects.get();
                var activeEffects = entity.getActiveEffectsMap();

                if (activeEffects.size() <= effects.size()) {
                    for (Holder<MobEffect> activeEffect : activeEffects.keySet()) {
                        if (effects.contains(activeEffect)) {
                            return true;
                        }
                    }
                } else {
                    for (Holder<MobEffect> effect : effects) {
                        if (activeEffects.containsKey(effect)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        },
        HAS_NEGATIVE_EFFECT {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.getActiveEffects().stream().anyMatch(effect -> !effect.getEffect().value().isBeneficial());
            }
        },
        IN_BIOME {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                Holder<Biome> biome = entity.level().getBiome(entity.blockPosition());
                return entityCondition.biomes()
                        .map(biomes -> biomes.contains(biome))
                        .orElse(false);
            }
        },
        ON_BLOCK {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                BlockState blockOn = entity.level().getBlockState(entity.blockPosition());
                return entityCondition.blocks().isEmpty() || blockOn.is(entityCondition.blocks().get());
            }
        },
        IN_WATER {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isInWater();
            }
        },
        IS_WET {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isInWaterRainOrBubble();
            }
        },
        IS_DAY {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.level().getDayTime() % 24000L < 12000L;
            }
        },
        IS_SWIMMING {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isSwimming();
            }
        },
        IS_SPRINTING {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isSprinting();
            }
        },
        IS_CROUCHING {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isCrouching();
            }
        },
        IS_GLOWING {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isCurrentlyGlowing();
            }
        },
        ON_GROUND {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.onGround();
            }
        },
        ON_FIRE {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isOnFire();
            }
        },
        IN_LAVA {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.isInLava();
            }
        },
        IS_RAINING {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.level().isRaining();
            }
        },
        IS_THUNDERING {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                return entity.level().isThundering();
            }
        },
        HEIGHT {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                double y = entity.getY();
                return y >= entityCondition.min && y <= entityCondition.max;
            }
        },
        BIOME_TEMPERATURE {
            @Override
            public boolean test(LivingEntity entity, EntityCondition entityCondition) {
                double temp = entity.level().getBiome(entity.blockPosition()).value().getBaseTemperature();
                return temp >= entityCondition.min && temp <= entityCondition.max;
            }
        };


        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        public abstract boolean test(LivingEntity entity, EntityCondition entityCondition);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
