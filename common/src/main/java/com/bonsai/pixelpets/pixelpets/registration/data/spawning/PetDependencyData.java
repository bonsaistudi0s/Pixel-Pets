package com.bonsai.pixelpets.pixelpets.registration.data.spawning;

import com.bonsai.pixelpets.util.IDependencyData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public record PetDependencyData(Set<ResourceLocation> prerequisites, boolean requireAll)
        implements IDependencyData<ResourceLocation> {

    public static PetDependencyData empty() {
        return new PetDependencyData(Set.of(), true);
    }

    public static final Codec<PetDependencyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().xmap(Set::copyOf, List::copyOf)
                    .optionalFieldOf("prerequisites", Set.of()).forGetter(PetDependencyData::prerequisites),
            Codec.BOOL.optionalFieldOf("require_all", true).forGetter(PetDependencyData::requireAll)
    ).apply(instance, PetDependencyData::new));

    // TODO test func
}
