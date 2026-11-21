package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record InColdBiomeCondition() implements TargetCondition {

    public static final MapCodec<InColdBiomeCondition> CODEC = MapCodec.unit(new InColdBiomeCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IN_COLD_BIOME.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        double temp = entity.level().getBiome(entity.blockPosition()).value().getBaseTemperature();
        return temp <= 0.2;
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.in_cold_biome");
    }
}
