package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

public record HasStatusEffectCondition(HolderSet<MobEffect> statusEffects, boolean all) implements TargetCondition {

    public static final MapCodec<HasStatusEffectCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT)
                    .optionalFieldOf("status_effects", HolderSet.empty())
                    .forGetter(condition -> condition.statusEffects),
            Codec.BOOL
                    .optionalFieldOf("all", true)
                    .forGetter(condition -> condition.all)
    ).apply(instance, HasStatusEffectCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.HAS_STATUS_EFFECT.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        if (this.statusEffects.size() == 0) return true;

        var activeEffects = entity.getActiveEffectsMap();
        if (this.all) {
            return this.statusEffects.stream().allMatch((activeEffects::containsKey));
        }

        if (activeEffects.size() <= this.statusEffects.size()) {
            return activeEffects.keySet().stream().anyMatch((this.statusEffects::contains));
        }

        return this.statusEffects.stream().anyMatch((activeEffects::containsKey));
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.has_status_effects");
    }

}
