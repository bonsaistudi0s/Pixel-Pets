package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsSwimmingCondition() implements TargetCondition {

    public static final MapCodec<IsSwimmingCondition> CODEC = MapCodec.unit(new IsSwimmingCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_SWIMMING.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.isSwimming();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_swimming");
    }
}
