package com.bonsai.pixelpets.pixelpets.registration.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BuffTarget(boolean self, boolean owner, boolean otherPets) {

    public static final Codec<BuffTarget> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("self", false).forGetter(BuffTarget::self),
            Codec.BOOL.optionalFieldOf("owner", false).forGetter(BuffTarget::owner),
            Codec.BOOL.optionalFieldOf("otherPets", false).forGetter(BuffTarget::otherPets)
    ).apply(i, BuffTarget::new));

}
