package com.bonsai.pixelpets.pixelpets.registration.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

// TODO consider adding a cooldown field?
public record LeveledBuffData(NavigableMap<Integer, PartialBuffData> keyframes) {

    private static final BuffTarget DEFAULT_TARGET = new BuffTarget(true, false, false);

    public static final Codec<LeveledBuffData> CODEC = Codec.unboundedMap(
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
            PartialBuffData.CODEC
    ).xmap(m -> new LeveledBuffData(new TreeMap<>(m)), LeveledBuffData::keyframes);

    // Concatenate entries with different ids
    // Merge/replace entries with the same id
    public PixelPetBuffData resolve(int level) {
        LinkedHashMap<String, PartialPermanentBuffData> permanentMap = new LinkedHashMap<>();
        LinkedHashMap<String, PartialTransientBuffData> transientMap = new LinkedHashMap<>();

        for (Map.Entry<Integer, PartialBuffData> entry : this.keyframes.entrySet()) {
            if (entry.getKey() > level) break;
            entry.getValue().permanentBuffs().ifPresent(list ->
                    list.forEach(p -> permanentMap.merge(p.id(), p, PartialPermanentBuffData::mergeWith))
            );
            entry.getValue().transientBuffs().ifPresent(list ->
                    list.forEach(t -> transientMap.merge(t.id(), t, PartialTransientBuffData::mergeWith))
            );
        }

        return new PixelPetBuffData(
                permanentMap.values().stream().map(PartialPermanentBuffData::toComplete).toList(),
                transientMap.values().stream().map(PartialTransientBuffData::toComplete).toList()
        );
    }

    public record PartialBuffData(
            Optional<List<PartialPermanentBuffData>> permanentBuffs,
            Optional<List<PartialTransientBuffData>> transientBuffs
    ) {
        public static final Codec<PartialBuffData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PartialPermanentBuffData.CODEC.listOf().optionalFieldOf("permanent_buffs").forGetter(PartialBuffData::permanentBuffs),
                PartialTransientBuffData.CODEC.listOf().optionalFieldOf("transient_buffs").forGetter(PartialBuffData::transientBuffs)
        ).apply(instance, PartialBuffData::new));
    }

    public record PartialPermanentBuffData(
            String id,
            Optional<BuffTarget> target,
            Optional<List<AttributeModifierApplication>> attributeModifiers
    ) {

        public static final Codec<PartialPermanentBuffData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(PartialPermanentBuffData::id),
                BuffTarget.CODEC.optionalFieldOf("targets").forGetter(PartialPermanentBuffData::target),
                AttributeModifierApplication.CODEC.listOf().optionalFieldOf("attribute_modifiers").forGetter(PartialPermanentBuffData::attributeModifiers)
        ).apply(instance, PartialPermanentBuffData::new));

        public PartialPermanentBuffData mergeWith(PartialPermanentBuffData next) {
            return new PartialPermanentBuffData(
                    this.id,
                    next.target.or(() -> this.target),
                    next.attributeModifiers.or(() -> this.attributeModifiers)
            );
        }

        public BuffData.Permanent toComplete() {
            return new BuffData.Permanent(
                    this.target.orElse(DEFAULT_TARGET),
                    this.attributeModifiers.orElse(List.of())
            );
        }
    }

    public record PartialTransientBuffData(
            String id,
            Optional<BuffTarget> target,
            Optional<List<EntityCondition>> conditions,
            Optional<List<StatusEffectApplication>> statusEffects,
            Optional<List<AttributeModifierApplication>> attributeModifiers
    ) {

        public static final Codec<PartialTransientBuffData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(PartialTransientBuffData::id),
                BuffTarget.CODEC.optionalFieldOf("targets").forGetter(PartialTransientBuffData::target),
                EntityCondition.CODEC.listOf().optionalFieldOf("conditions").forGetter(PartialTransientBuffData::conditions),
                StatusEffectApplication.CODEC.listOf().optionalFieldOf("status_effects").forGetter(PartialTransientBuffData::statusEffects),
                AttributeModifierApplication.CODEC.listOf().optionalFieldOf("attribute_modifiers").forGetter(PartialTransientBuffData::attributeModifiers)
        ).apply(instance, PartialTransientBuffData::new));

        public PartialTransientBuffData mergeWith(PartialTransientBuffData next) {
            return new PartialTransientBuffData(
                    this.id,
                    next.target.or(() -> this.target),
                    next.conditions.or(() -> this.conditions),
                    next.statusEffects.or(() -> this.statusEffects),
                    next.attributeModifiers.or(() -> this.attributeModifiers)
            );
        }

        public BuffData.Transient toComplete() {
            return new BuffData.Transient(
                    this.target.orElse(DEFAULT_TARGET),
                    this.conditions.orElse(List.of()),
                    this.statusEffects.orElse(List.of()),
                    this.attributeModifiers.orElse(List.of())
            );
        }
    }

    public record PixelPetBuffData(
            List<BuffData.Permanent> permanentBuffs,
            List<BuffData.Transient> transientBuffs
    ) {}


    public sealed interface BuffData {

        BuffTarget target();
        List<AttributeModifierApplication> attributeModifiers();

        default void applyAttributeModifiers(LivingEntity livingEntity) {
            attributeModifiers().forEach(a -> a.apply(livingEntity));
        }

        default void removeAttributeModifiers(LivingEntity livingEntity) {
            attributeModifiers().forEach(a -> a.remove(livingEntity));
        }

        // Apply once (if target applicable) on:
        //  equip
        //  summon
        //  level change
        // Remove on:
        //  level change
        //  unequip
        record Permanent(
                BuffTarget target,
                List<AttributeModifierApplication> attributeModifiers
        ) implements BuffData {}

        // Attempt to apply on a cooldown
        //  AND all conditions
        //  Remove attributeModifiers on failure
        record Transient(
                BuffTarget target,
                List<EntityCondition> conditions,
                List<StatusEffectApplication> statusEffects,
                List<AttributeModifierApplication> attributeModifiers
        ) implements BuffData {

            public boolean test(LivingEntity livingEntity) {
                return this.conditions.isEmpty() || this.conditions.stream().allMatch(c -> c.test(livingEntity));
            }
        }
    }
}
