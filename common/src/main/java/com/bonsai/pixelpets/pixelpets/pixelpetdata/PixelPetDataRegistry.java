package com.bonsai.pixelpets.pixelpets.pixelpetdata;

import com.bonsai.pixelpets.PixelPets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PixelPetDataRegistry extends SimpleJsonResourceReloadListener {

    public static final PixelPetDataRegistry INSTANCE = new PixelPetDataRegistry();
    private Map<ResourceLocation, PixelPetData> species = new HashMap<>();

    public PixelPetDataRegistry() {
        super(new GsonBuilder().create(), "pet_data");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager mgr, ProfilerFiller profiler) {
        species = new HashMap<>();
        jsons.forEach((id, json) -> {
            PixelPetData.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> PixelPets.LOGGER.error("Failed to load species {}: {}", id, err))
                    .ifPresent(data -> {
                        species.put(id, new PixelPetData(
                                id,
                                data.entityType(),
                                data.genericName(),
                                data.tameItem(),
                                data.baseHealth(),
                                data.animationId(),
                                data.attack(),
                                data.tameChance(),
                                data.scares(),
                                data.rarity()
                        ));
                    });
        });
        PixelPets.LOGGER.info("Loaded {} PixelPet species", species.size());
    }

    public Optional<PixelPetData> get(ResourceLocation id) {
        return Optional.ofNullable(species.get(id));
    }

    public Collection<PixelPetData> getAll() {
        return species.values();
    }

    public Optional<PixelPetData> getById(ResourceLocation id) {
        return Optional.ofNullable(species.get(id));
    }
}
