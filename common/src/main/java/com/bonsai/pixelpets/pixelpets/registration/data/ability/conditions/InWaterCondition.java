package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record InWaterCondition() implements TargetCondition {

    public static final MapCodec<InWaterCondition> CODEC = MapCodec.unit(new InWaterCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IN_WATER.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.isInWater();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.in_water");
    }
}
