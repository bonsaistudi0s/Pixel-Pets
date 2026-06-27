package com.bonsai.pixelpets;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;

// TODO change icon.png
public class PixelPetsFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        registerEntityAttributes();

        DynamicRegistries.registerSynced(PixelPets.PET_DATA, PixelPetData.CODEC, PixelPetData.CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PixelPets.scaredByMap = new HashMap<>();
            server.registryAccess().registryOrThrow(PixelPets.PET_DATA).holders().forEach(holder -> {
                ResourceLocation id = holder.key().location();
                holder.value().scares().forEach(holderSet ->
                        holderSet.forEach(entityTypeHolder ->
                                PixelPets.scaredByMap
                                        .computeIfAbsent(entityTypeHolder.value(), k -> new HashSet<>())
                                        .add(id)
                        )
                );
            });
        });

        if (FabricLoader.getInstance().isModLoaded("fabric-command-api-v2")) {
            CommandRegistrationCallback.EVENT.register(ModCommands::register);
        }

        PixelPets.init();
    }

    private void registerEntityAttributes() {
        // TODO this is how I did entity attributes, might be an easier way in common?
        FabricDefaultAttributeRegistry.register(ModEntities.WALKING_PET.get(), AbstractPixelPetEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SWIMMING_PET.get(), AbstractPixelPetEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.AMPHIBIOUS_PET.get(), AbstractPixelPetEntity.createAttributes());
    }
}
