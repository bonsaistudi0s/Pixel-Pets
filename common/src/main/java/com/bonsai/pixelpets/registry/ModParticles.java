package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModParticles {

    public static final ResourcefulRegistry<ParticleType<?>> PARTICLE_TYPES = ResourcefulRegistries.create(BuiltInRegistries.PARTICLE_TYPE, PixelPets.MOD_ID);

    // example:
    //public static final RegistryEntry<SimpleParticleType> SIMPLE = PARTICLE_TYPES.register("totem_of_freezing", Services.PLATFORM::simpleParticleType);

}
