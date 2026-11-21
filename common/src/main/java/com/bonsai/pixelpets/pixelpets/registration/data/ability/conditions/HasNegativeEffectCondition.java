package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record HasNegativeEffectCondition() implements TargetCondition {

    public static final MapCodec<HasNegativeEffectCondition> CODEC = MapCodec.unit(new HasNegativeEffectCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.HAS_NEGATIVE_EFFECT.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.getActiveEffects().stream().anyMatch(effect -> !effect.getEffect().value().isBeneficial());
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.has_negative_effect");
    }
}
