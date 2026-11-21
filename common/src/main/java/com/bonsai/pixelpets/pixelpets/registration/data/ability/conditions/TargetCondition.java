package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.PixelPets;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public interface TargetCondition {

    TargetConditionType<?> type();

    boolean test(LivingEntity entity);

    Component describe();

    Codec<TargetCondition> CODEC = PixelPets.TARGET_CONDITION_TYPES.byNameCodec().dispatch(TargetCondition::type, TargetConditionType::codec);
}

