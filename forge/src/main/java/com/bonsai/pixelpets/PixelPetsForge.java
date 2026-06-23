package com.bonsai.pixelpets;

import com.bonsai.pixelpets.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(PixelPets.MOD_ID)
public class PixelPetsForge {

    public static IEventBus eventBus;

    public PixelPetsForge(IEventBus modEventBus, Dist dist) {
        PixelPetsForge.eventBus = modEventBus;

        eventBus.addListener(this::registerEntityAttributes);

        if (dist.isClient()) {
            eventBus.addListener(PixelPetsForgeClient::registerBlocks);
            eventBus.addListener(PixelPetsForgeClient::registerEntityRenderers);
            eventBus.addListener(PixelPetsForgeClient::registerParticleFactories);
        }

        PixelPets.init();
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        // TODO this is how I did entity attributes, might be an easier way in common?
    }
}