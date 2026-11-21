package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record InLavaCondition() implements TargetCondition {

    public static final MapCodec<InLavaCondition> CODEC = MapCodec.unit(new InLavaCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IN_LAVA.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return entity.isInLava();
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.in_lava");
    }
}
