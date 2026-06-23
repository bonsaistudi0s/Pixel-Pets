package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final ResourcefulRegistry<SoundEvent> SOUND_EVENTS = ResourcefulRegistries.create(BuiltInRegistries.SOUND_EVENT, PixelPets.MOD_ID);

    // example:
    // public static final RegistryEntry<SoundEvent> ENTITY_COPPER_GOLEM_DEATH = registerSoundEvent("entity.copper_golem.death");

}
