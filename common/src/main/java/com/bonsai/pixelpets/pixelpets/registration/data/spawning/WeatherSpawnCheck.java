package com.bonsai.pixelpets.pixelpets.registration.data.spawning;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;

public record WeatherSpawnCheck(Optional<Boolean> isRaining, Optional<Boolean> isThundering) {

    public static final WeatherSpawnCheck ANY = new WeatherSpawnCheck(Optional.empty(), Optional.empty());

    public static final MapCodec<WeatherSpawnCheck> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("raining").forGetter(WeatherSpawnCheck::isRaining),
                    Codec.BOOL.optionalFieldOf("thundering").forGetter(WeatherSpawnCheck::isThundering)
            ).apply(instance, WeatherSpawnCheck::new));

    public boolean test(ServerLevel level) {
        return isRaining.map(r -> r == level.isRaining()).orElse(true)
                && isThundering.map(t -> t == level.isThundering()).orElse(true);
    }
}

