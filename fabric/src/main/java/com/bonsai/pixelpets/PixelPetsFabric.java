package com.bonsai.pixelpets;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.pixelpetdata.PixelPetDataRegistry;
import com.bonsai.pixelpets.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO change icon.png
public class PixelPetsFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        registerEntityAttributes();

        registerReloadListener(PixelPets.identifier("pet_data"), PixelPetDataRegistry.INSTANCE);

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



    // Yelf42: Taken from my other mod, might be better ways
    private void registerReloadListener(ResourceLocation id, SimpleJsonResourceReloadListener listener) {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new IdentifiableResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return id;
                    }

                    @Override
                    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                                          ProfilerFiller prepProfiler, ProfilerFiller applyProfiler,
                                                          Executor prepExec, Executor applyExec) {
                        return listener.reload(barrier, manager, prepProfiler, applyProfiler, prepExec, applyExec);
                    }
                }
        );
    }
}
