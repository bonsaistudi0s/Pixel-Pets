package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsHeightCondition(int min, int max) implements TargetCondition {

    public static final MapCodec<IsHeightCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("min", -1024).forGetter(condition -> condition.min),
            Codec.INT.optionalFieldOf("max", 1024).forGetter(condition -> condition.max)
    ).apply(instance, IsHeightCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_HEIGHT.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        double y = entity.getY();
        return y >= this.min && y <= this.max;
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_height");
    }

}
