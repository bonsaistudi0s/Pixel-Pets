package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.mojang.serialization.MapCodec;

public record TargetConditionType<T extends TargetCondition>(MapCodec<T> codec) {}
