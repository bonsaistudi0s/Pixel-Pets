package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record OnFireCondition() implements TargetCondition {

    public static final MapCodec<OnFireCondition> CODEC = MapCodec.unit(new OnFireCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.ON_FIRE.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.isOnFire();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.on_fire");
    }
}
