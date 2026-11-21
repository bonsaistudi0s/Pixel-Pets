package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsDayCondition() implements TargetCondition {

    public static final MapCodec<IsDayCondition> CODEC = MapCodec.unit(new IsDayCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_DAY.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.level().getDayTime() % 24000L < 12000L;
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_day");
    }
}
