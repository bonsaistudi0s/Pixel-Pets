package com.bonsai.pixelpets;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PixelPetDataRegistry;
import com.bonsai.pixelpets.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO change icon.png
public class PixelPetsFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {

        bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);

        bind(BuiltInRegistries.BLOCK, ModBlocks::registerBlocks);
        bind(BuiltInRegistries.ITEM, ModBlocks::registerItems);

        bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);

        bind(BuiltInRegistries.ITEM, ModItems::registerItems);
        bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModItems::registerTabs);
        bind(BuiltInRegistries.RECIPE_SERIALIZER, ModItems::registerRecipes);

        bind(BuiltInRegistries.DATA_COMPONENT_TYPE, ModComponents::register);

        bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
        registerEntityAttributes();

        registerReloadListener(PixelPets.identifier("pet_data"), PixelPetDataRegistry.INSTANCE);

        if (FabricLoader.getInstance().isModLoaded("fabric-command-api-v2")) {
            CommandRegistrationCallback.EVENT.register(ModCommands::register);
        }

        PixelPets.init();
    }

    public static <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
        source.accept((t, rl) -> Registry.register(registry, rl, t));
    }

    private void registerEntityAttributes() {
        // TODO this is how I did entity attributes, might be an easier way in common?
        FabricDefaultAttributeRegistry.register(ModEntities.DEFAULT_PET, AbstractPixelPetEntity.createAttributes());
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
