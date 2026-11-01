package com.bonsai.pixelpets.pixelpets.registration.data.cosmetic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ParticleData(ParticleType<?> particleType, int count) {

    public static final Codec<ParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.<ParticleType<?>>flatXmap(
                            rl -> Optional.ofNullable(BuiltInRegistries.PARTICLE_TYPE.get(rl))
                                    .map(t -> (ParticleType<?>) t)
                                    .map(DataResult::success)
                                    .orElseGet(() -> DataResult.error(() -> "Unknown particle type: " + rl)),
                            type -> DataResult.success(BuiltInRegistries.PARTICLE_TYPE.getKey(type))
                    )
                    .fieldOf("particle_type")
                    .forGetter(ParticleData::particleType),
            Codec.INT.optionalFieldOf("count", 5).forGetter(ParticleData::count)
    ).apply(instance, ParticleData::new));

}
