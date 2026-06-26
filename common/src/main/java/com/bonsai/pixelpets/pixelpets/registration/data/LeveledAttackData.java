package com.bonsai.pixelpets.pixelpets.registration.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

public record LeveledAttackData(NavigableMap<Integer, PartialAttackData> keyframes) {

    public static final Codec<LeveledAttackData> CODEC = Codec.unboundedMap(
            Codec.STRING.comapFlatMap(
                    s -> {
                        try {
                            return DataResult.success(Integer.valueOf(s));
                        } catch (NumberFormatException e) {
                            return DataResult.error(() -> "Not a valid level: " + s);
                        }
                    },
                    String::valueOf
            ),
            PartialAttackData.CODEC
    ).xmap(m -> new LeveledAttackData(new TreeMap<>(m)), LeveledAttackData::keyframes);

    public Optional<PixelPetAttackData> resolve(int level) {
        PartialAttackData merged = null;
        for (Map.Entry<Integer, PartialAttackData> entry : this.keyframes.entrySet()) {
            if (entry.getKey() > level) {
                break;
            }
            merged = (merged == null) ? entry.getValue() : merged.mergeWith(entry.getValue());
        }
        return Optional.ofNullable(merged).map(PartialAttackData::toComplete);
    }

    public record PartialAttackData(
            Optional<Float> damage,
            Optional<Float> knockback,
            Optional<Float> range,
            Optional<Integer> cooldown,
            Optional<EntityType<Projectile>> projectile,
            Optional<List<String>> projectileEffects,
            Optional<List<StatusEffectApplication>> statusEffects
    ) {
        public static final Codec<PartialAttackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("damage").forGetter(PartialAttackData::damage),
                Codec.FLOAT.optionalFieldOf("knockback").forGetter(PartialAttackData::knockback),
                Codec.FLOAT.optionalFieldOf("range").forGetter(PartialAttackData::range),
                Codec.INT.optionalFieldOf("cooldown").forGetter(PartialAttackData::cooldown),
                ResourceLocation.CODEC.flatXmap(rl -> Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.get(rl))
                                        .map(t -> (EntityType<Projectile>) t)
                                        .map(DataResult::success)
                                        .orElseGet(() -> DataResult.error(() -> "Unknown or non-projectile entity type: " + rl)),
                                type -> DataResult.success(BuiltInRegistries.ENTITY_TYPE.getKey(type))
                        )
                        .optionalFieldOf("projectile")
                        .forGetter(PartialAttackData::projectile),
                Codec.STRING.listOf().optionalFieldOf("projectile_effects").forGetter(PartialAttackData::projectileEffects), // TODO
                StatusEffectApplication.CODEC.listOf().optionalFieldOf("status_effects").forGetter(PartialAttackData::statusEffects)
        ).apply(instance, PartialAttackData::new));

        public PartialAttackData mergeWith(PartialAttackData next) {
            return new PartialAttackData(
                    next.damage.or(() -> this.damage),
                    next.knockback.or(() -> this.knockback),
                    next.range.or(() -> this.range),
                    next.cooldown.or(() -> this.cooldown),
                    next.projectile.or(() -> this.projectile),
                    next.projectileEffects.or(() -> this.projectileEffects),
                    next.statusEffects.or(() -> this.statusEffects)
            );
        }

        public PixelPetAttackData toComplete() {
            return new PixelPetAttackData(
                    this.damage.orElse(4.0f),
                    this.knockback.orElse(0.0F),
                    this.range.orElse(Float.NEGATIVE_INFINITY),
                    this.cooldown.orElse(20),
                    this.projectile,
                    this.projectileEffects.orElse(List.of()),
                    this.statusEffects.orElse(List.of())
            );
        }
    }

    public record PixelPetAttackData(
            float damage,
            float knockback,
            float range,
            int cooldown,
            Optional<EntityType<Projectile>> projectile,
            List<String> projectileEffects,                     // TODO stuff like ricochet/grow will go here
            List<StatusEffectApplication> statusEffects
    ) {
        // Note: Because ranged vs melee is determined by projectile,
        // a melee pet can become a ranged pet, but a ranged pet cannot become a melee pet
        // Also, a pet with attacks can't lose attacks at higher levels
        public boolean isRanged() {
            return this.projectile.isPresent();
        }
    }

}
