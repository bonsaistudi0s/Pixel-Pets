package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsThunderingCondition() implements TargetCondition {

    public static final MapCodec<IsThunderingCondition> CODEC = MapCodec.unit(new IsThunderingCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_THUNDERING.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.level().isThundering();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_thundering");
    }
}
