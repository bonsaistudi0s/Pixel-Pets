package com.bonsai.pixelpets;


import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.HashMap;
import java.util.HashSet;

@Mod(PixelPets.MOD_ID)
public class PixelPetsNeoforge {

    public static IEventBus eventBus;

    public PixelPetsNeoforge(IEventBus eventBus, Dist dist) {

        PixelPetsNeoforge.eventBus = eventBus;

        eventBus.addListener(this::registerDatapackRegistries);

        eventBus.addListener(this::registerEntityAttributes);
        
        if (dist.isClient()) {
            eventBus.addListener(PixelPetsNeoforgeClient::registerBlocks);
            eventBus.addListener(PixelPetsNeoforgeClient::registerEntityRenderers);
            eventBus.addListener(PixelPetsNeoforgeClient::registerParticleFactories);
        }

        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        PixelPets.init();

    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        // TODO this is how I did entity attributes, might be an easier way in common?
        event.put(ModEntities.WALKING_PET.get(), AbstractPixelPetEntity.createAttributes().build());
        event.put(ModEntities.SWIMMING_PET.get(), AbstractPixelPetEntity.createAttributes().build());
        event.put(ModEntities.AMPHIBIOUS_PET.get(), AbstractPixelPetEntity.createAttributes().build());
    }

    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    public void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                PixelPets.PET_DATA,
                PixelPetData.CODEC,
                PixelPetData.CODEC,
                builder -> builder.onBake((registry) -> {
                    PixelPets.scaredByMap = new HashMap<>();
                    registry.holders().forEach(holder -> {
                        ResourceLocation id = holder.key().location();
                        holder.value().scares().forEach(holderSet ->
                                holderSet.forEach(entityTypeHolder ->
                                        PixelPets.scaredByMap
                                                .computeIfAbsent(entityTypeHolder.value(), k -> new HashSet<>())
                                                .add(id)
                                )
                        );
                    });
                })
        );
    }
}