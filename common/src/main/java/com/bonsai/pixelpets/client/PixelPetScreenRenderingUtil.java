package com.bonsai.pixelpets.client;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PixelPetStatus;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.pixelpets.registration.data.spawning.PetDependencyData;
import com.bonsai.pixelpets.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

import static com.bonsai.pixelpets.PixelPets.PET_DATA;

public class PixelPetScreenRenderingUtil {

    public static final int NODE_SIZE = 24;
    public static final LayeredGraph.Config LAYERED_CONFIG =   new LayeredGraph.Config(NODE_SIZE, NODE_SIZE * 1.5, 36, 1.2, 12);
    private static final LayeredGraph<ResourceLocation> EMPTY = LayeredGraph.generate(Map.of(), LAYERED_CONFIG);

    private static WeakReference<RegistryAccess> builtFrom = new WeakReference<>(null);
    private static IGraphLayout<ResourceLocation> graph = EMPTY;

    private static final Map<ResourceLocation, LivingEntity> ENTITIES = new HashMap<>();
    private static ClientLevel cachedLevel;

    // For testing a more complex graph, lacks actual pet data
    private static final Map<ResourceLocation, IDependencyData<ResourceLocation>> testGraph = RandomDependencyGraph.generate(100, 1236, i -> PixelPets.id("pet_" + i), DependencyData::new);
    private static final LayeredGraph<ResourceLocation> TEST_LAYERED = LayeredGraph.generate(testGraph, LAYERED_CONFIG);

    public static IGraphLayout<ResourceLocation> getGraph() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            PixelPets.LOGGER.warn("ClientLevel wasn't ready for graph creation");
            return EMPTY;
        }

        RegistryAccess registries = level.registryAccess();

        if (builtFrom.get() != registries) {
            graph = build(registries);
            builtFrom = new WeakReference<>(registries);
        }
        return TEST_LAYERED;
    }

    public static LivingEntity getEntity(ResourceLocation id) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (level == null) {
            ENTITIES.clear();
            cachedLevel = null;
            return null;
        }

        if (cachedLevel != level) {
            ENTITIES.clear();
            cachedLevel = level;
        }

        return ENTITIES.computeIfAbsent(id, key -> {
            RegistryAccess registries = level.registryAccess();

            PixelPetData data = registries.lookup(PET_DATA)
                    .flatMap(registry -> registry.get(ResourceKey.create(PET_DATA, key)))
                    .map(Holder::value)
                    .orElse(null);

            if (data == null) return null;

            return createEntity(key, data, level);
        });
    }


    private static IGraphLayout<ResourceLocation> build(
            HolderLookup.Provider registries) {

        Map<ResourceLocation, PetDependencyData> dependencies =
                new TreeMap<>();

        registries.lookup(PET_DATA).ifPresent(lookup ->
                lookup.listElements().forEach(holder ->
                        dependencies.put(holder.key().location(), holder.value().spawnData().prerequisitePets())
                )
        );

        try {
            return LayeredGraph.generate(dependencies, LAYERED_CONFIG);
        } catch (IllegalStateException e) {
            PixelPets.LOGGER.error("Invalid pet dependency data, graph left empty: {}", e.getMessage());
            return EMPTY;
        }
    }

    @Nullable
    private static LivingEntity createEntity(ResourceLocation id, PixelPetData data, ClientLevel level) {
        try {
            EntityType<?> type = data.entityType();
            if (type == null) return null;
            Entity entity = type.create(level);
            if (entity instanceof AbstractPixelPetEntity pixelPetEntity) {
                pixelPetEntity.initialize(id, 1, PixelPetStatus.PASSIVE);
            }
            return entity instanceof LivingEntity living ? living : null;
        } catch (Exception e) {
            PixelPets.LOGGER.error("Failed to create preview entity for pet {}", id, e);
            return null;
        }
    }

    @Nullable
    public static Registry<PixelPetData> getPetRegistry() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return null;
        }

        return minecraft.level.registryAccess().registry(PixelPets.PET_DATA).orElse(null);
    }
}