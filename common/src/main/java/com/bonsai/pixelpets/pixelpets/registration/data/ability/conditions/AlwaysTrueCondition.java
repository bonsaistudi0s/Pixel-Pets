package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record AlwaysTrueCondition() implements TargetCondition {

    public static final MapCodec<AlwaysTrueCondition> CODEC = MapCodec.unit(new AlwaysTrueCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.ALWAYS_TRUE.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return true;
    }

    // TODO bit weird, maybe something like "alive"
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.always_true");
    }
}
