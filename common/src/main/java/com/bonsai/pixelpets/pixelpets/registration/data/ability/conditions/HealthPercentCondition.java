package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record HealthPercentCondition(int min, int max) implements TargetCondition {

    public static final MapCodec<HealthPercentCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("min", 0).forGetter(condition -> condition.min),
            Codec.INT.optionalFieldOf("max", 100).forGetter(condition -> condition.max)
    ).apply(instance, HealthPercentCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.HEALTH_PERCENT.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        double hpPercent = 100.0 * Math.max(0, entity.getHealth() / entity.getMaxHealth());
        return hpPercent >= this.min && hpPercent <= this.max;
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.health_percent");
    }

}
