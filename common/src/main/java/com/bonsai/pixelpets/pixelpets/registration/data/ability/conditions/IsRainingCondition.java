package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsRainingCondition() implements TargetCondition {

    public static final MapCodec<IsRainingCondition> CODEC = MapCodec.unit(new IsRainingCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_RAINING.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.level().isRaining();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_raining");
    }
}
