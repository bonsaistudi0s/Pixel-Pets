package com.bonsai.pixelpets;


import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.pixelpetdata.PixelPetDataRegistry;
import com.bonsai.pixelpets.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(PixelPets.MOD_ID)
public class PixelPetsNeoforge {

    public static IEventBus eventBus;

    public PixelPetsNeoforge(IEventBus eventBus, Dist dist) {

        PixelPetsNeoforge.eventBus = eventBus;

        ModAttributes.init();
        bind(Registries.ATTRIBUTE, ModAttributes::registerAttributes);

        bind(Registries.PARTICLE_TYPE, ModParticles::register);

        bind(Registries.BLOCK, ModBlocks::registerBlocks);
        bind(Registries.ITEM, ModBlocks::registerItems);

        bind(Registries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(Registries.ITEM, ModItems::registerItems);
        bind(Registries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(Registries.RECIPE_SERIALIZER, ModItems::registerRecipes);

        bind(Registries.DATA_COMPONENT_TYPE, ModComponents::register);

        bind(Registries.ENTITY_TYPE, ModEntities::register);
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

    public static <T> void bind(ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
        eventBus.addListener((Consumer<RegisterEvent>) event -> {
            if (registry.equals(event.getRegistryKey())) {
                source.accept((t, rl) -> event.register(registry, rl, () -> t));
            }
        });
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        // TODO this is how I did entity attributes, might be an easier way in common?
        event.put(ModEntities.WALKING_PET, AbstractPixelPetEntity.createAttributes().build());
        event.put(ModEntities.SWIMMING_PET, AbstractPixelPetEntity.createAttributes().build());
        event.put(ModEntities.AMPHIBIOUS_PET, AbstractPixelPetEntity.createAttributes().build());
    }

    public void registerOnReloadMappings(AddReloadListenerEvent event) {
        event.addListener(PixelPetDataRegistry.INSTANCE);
    }

    public void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}