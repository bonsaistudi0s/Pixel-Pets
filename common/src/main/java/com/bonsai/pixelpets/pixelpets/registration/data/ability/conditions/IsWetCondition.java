package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsWetCondition() implements TargetCondition {

    public static final MapCodec<IsWetCondition> CODEC = MapCodec.unit(new IsWetCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_WET.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.isInWaterRainOrBubble();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_wet");
    }
}
