package com.bonsai.pixelpets;

import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.platform.Services;
import com.bonsai.pixelpets.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PixelPets {

    public static final String MOD_ID = "pixelpets";
    public static final String MOD_NAME = "Pixel Pets";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"}; // Nice for components

    public static final ResourceKey<Registry<PixelPetData>> PET_DATA = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("pet_data"));
    public static Map<EntityType<?>, Set<ResourceLocation>> scaredByMap = new HashMap<>();

    public static void init() {
        LOGGER.info("Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());

        ModParticles.PARTICLE_TYPES.init();

        ModSounds.SOUND_EVENTS.init();

        ModAttributes.ATTRIBUTES.init();

        ModComponents.DATA_COMPONENTS.init();

        ModEffects.STATUS_EFFECTS.init();

        ModBlocks.BLOCKS.init();
        ModBlockEntities.BLOCK_ENTITY_TYPE.init();

        ModItems.ITEMS.init();
        ModItems.CREATIVE_TABS.init();

        ModEntities.ENTITY_TYPES.init();
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(PixelPets.MOD_ID, path);
    }

    public static Set<ResourceLocation> getScaryPets(EntityType<?> t) {
        return scaredByMap.getOrDefault(t, Collections.emptySet());
    }
}