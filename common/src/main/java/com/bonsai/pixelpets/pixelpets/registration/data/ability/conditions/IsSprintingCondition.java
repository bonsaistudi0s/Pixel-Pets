package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsSprintingCondition() implements TargetCondition {

    public static final MapCodec<IsSprintingCondition> CODEC = MapCodec.unit(new IsSprintingCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_SPRINTING.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.isSprinting();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_sprinting");
    }
}
