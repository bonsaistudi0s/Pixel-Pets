package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsTimeCondition(int min, int max) implements TargetCondition {

    public static final MapCodec<IsTimeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("min", 0).forGetter(condition -> condition.min),
            Codec.INT.optionalFieldOf("max", 24000).forGetter(condition -> condition.max)
    ).apply(instance, IsTimeCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_TIME.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        long time = entity.level().getDayTime() % 24000L;
        return time <= this.max && time >= this.min;
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_time");
    }

}
