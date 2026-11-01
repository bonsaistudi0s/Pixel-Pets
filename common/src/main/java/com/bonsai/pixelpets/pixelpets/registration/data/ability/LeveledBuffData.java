package com.bonsai.pixelpets.pixelpets.registration.data.ability;

import com.bonsai.pixelpets.pixelpets.registration.data.cosmetic.ParticleData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

// TODO consider adding particles/sfx when applying
public record LeveledBuffData(NavigableMap<Integer, List<PartialBuff>> keyframes) {

    private static final BuffTarget DEFAULT_TARGET = new BuffTarget(true, false, false);
    private static final int DEFAULT_COOLDOWN = 200;

    public static final Codec<LeveledBuffData> CODEC = Codec.unboundedMap(Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(Integer.valueOf(s));
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Not a valid level: " + s);
        }
    }, String::valueOf), PartialBuff.CODEC.listOf()).xmap(map -> new LeveledBuffData(new TreeMap<>(map)), LeveledBuffData::keyframes);

    public PixelPetBuffData resolve(int level) {
        Map<String, PartialBuff> merged = new LinkedHashMap<>();

        for (List<PartialBuff> buffs : this.keyframes.headMap(level, true).values()) {
            buffs.forEach(buff -> merged.merge(buff.id(), buff, PartialBuff::mergeWith));
        }

        return new PixelPetBuffData(merged.values().stream().map(PartialBuff::toComplete).toList());
    }

    public record PartialBuff(String id,
                              Optional<BuffTarget> target,
                              Optional<Integer> cooldown,
                              Optional<List<EntityCondition>> conditions,
                              Optional<List<StatusEffectApplication>> statusEffects,
                              Optional<List<AttributeModifierApplication>> attributeModifiers,
                              Optional<ParticleData> particles) {

        public static final Codec<PartialBuff> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(PartialBuff::id),
                BuffTarget.CODEC.optionalFieldOf("targets").forGetter(PartialBuff::target),
                Codec.INT.optionalFieldOf("cooldown").forGetter(PartialBuff::cooldown),
                EntityCondition.CODEC.listOf().optionalFieldOf("conditions").forGetter(PartialBuff::conditions),
                StatusEffectApplication.CODEC.listOf().optionalFieldOf("status_effects").forGetter(PartialBuff::statusEffects),
                AttributeModifierApplication.CODEC.listOf().optionalFieldOf("attribute_modifiers").forGetter(PartialBuff::attributeModifiers),
                ParticleData.CODEC.optionalFieldOf("particles").forGetter(PartialBuff::particles)
        ).apply(instance, PartialBuff::new));

        public PartialBuff mergeWith(PartialBuff next) {
            return new PartialBuff(this.id,
                    next.target.or(() -> this.target),
                    next.cooldown.or(() -> this.cooldown),
                    next.conditions.or(() -> this.conditions),
                    next.statusEffects.or(() -> this.statusEffects),
                    next.attributeModifiers.or(() -> this.attributeModifiers),
                    next.particles.or(() -> this.particles)
            );
        }

        public BuffData toComplete() {
            return new BuffData(this.id,
                    this.target.orElse(DEFAULT_TARGET),
                    this.cooldown.orElse(DEFAULT_COOLDOWN),
                    this.conditions.orElse(List.of()),
                    this.statusEffects.orElse(List.of()),
                    this.attributeModifiers.orElse(List.of()),
                    this.particles);
        }
    }

    public record PixelPetBuffData(List<BuffData> buffs) {}

    public record BuffData(String id,
                           BuffTarget target,
                           int cooldown,
                           List<EntityCondition> conditions,
                           List<StatusEffectApplication> statusEffects,
                           List<AttributeModifierApplication> attributeModifiers,
                           Optional<ParticleData> particles) {

        public boolean test(LivingEntity entity) {
            return this.conditions.isEmpty() || this.conditions.stream().allMatch(condition -> condition.test(entity));
        }
    }
}