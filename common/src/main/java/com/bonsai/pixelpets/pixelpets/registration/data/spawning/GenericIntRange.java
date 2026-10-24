package com.bonsai.pixelpets.pixelpets.registration.data.spawning;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record GenericIntRange(Optional<Integer> min, Optional<Integer> max, Optional<Boolean> invert) {

    public static final Codec<GenericIntRange> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("min").forGetter(GenericIntRange::min),
                    Codec.INT.optionalFieldOf("max").forGetter(GenericIntRange::max),
                    Codec.BOOL.optionalFieldOf("invert").forGetter(GenericIntRange::invert)
            ).apply(instance, GenericIntRange::new));

    public boolean test(long input) {
        boolean inRange = min.map(m -> input >= m).orElse(true)
                && max.map(m -> input <= m).orElse(true);
        return inRange != invert.orElse(false);
    }
}
