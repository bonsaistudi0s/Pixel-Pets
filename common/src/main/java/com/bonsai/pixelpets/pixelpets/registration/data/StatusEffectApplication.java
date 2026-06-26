package com.bonsai.pixelpets.pixelpets.registration.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public record StatusEffectApplication(Holder<MobEffect> effect, int amplifier, int duration, float chance) {
    public static final Codec<StatusEffectApplication> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(StatusEffectApplication::effect),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(StatusEffectApplication::amplifier),
            Codec.INT.optionalFieldOf("duration", 40).forGetter(StatusEffectApplication::duration),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(StatusEffectApplication::chance)
    ).apply(instance, StatusEffectApplication::new));
}
