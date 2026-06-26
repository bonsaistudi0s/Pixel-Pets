package com.bonsai.pixelpets;


import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetDataRegistry;
import com.bonsai.pixelpets.registry.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(PixelPets.MOD_ID)
public class PixelPetsNeoforge {

    public static IEventBus eventBus;

    public PixelPetsNeoforge(IEventBus eventBus, Dist dist) {

        PixelPetsNeoforge.eventBus = eventBus;

        eventBus.addListener(this::registerEntityAttributes);
        
        if (dist.isClient()) {
            eventBus.addListener(PixelPetsNeoforgeClient::registerBlocks);
            eventBus.addListener(PixelPetsNeoforgeClient::registerEntityRenderers);
            eventBus.addListener(PixelPetsNeoforgeClient::registerParticleFactories);
        }

        NeoForge.EVENT_BUS.addListener(this::registerOnReloadMappings);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        PixelPets.init();

    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        // TODO this is how I did entity attributes, might be an easier way in common?
        event.put(ModEntities.WALKING_PET.get(), AbstractPixelPetEntity.createAttributes().build());
        event.put(ModEntities.SWIMMING_PET.get(), AbstractPixelPetEntity.createAttributes().build());
        event.put(ModEntities.AMPHIBIOUS_PET.get(), AbstractPixelPetEntity.createAttributes().build());
    }

    public void registerOnReloadMappings(AddReloadListenerEvent event) {
        event.addListener(PixelPetDataRegistry.INSTANCE);
    }

    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}