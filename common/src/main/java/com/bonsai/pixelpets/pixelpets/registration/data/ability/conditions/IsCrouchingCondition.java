package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsCrouchingCondition() implements TargetCondition {

    public static final MapCodec<IsCrouchingCondition> CODEC = MapCodec.unit(new IsCrouchingCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_CROUCHING.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.isCrouching();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_crouching");
    }
}
