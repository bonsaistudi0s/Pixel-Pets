package com.bonsai.pixelpets.pixelpets.registration;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.pixelpets.registration.data.ScareValue;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.util.*;


// TODO consider switching to datapack registry approach?
public class PixelPetDataRegistry extends SimpleJsonResourceReloadListener {

    public static final PixelPetDataRegistry INSTANCE = new PixelPetDataRegistry();
    private Map<ResourceLocation, PixelPetData> species = new HashMap<>();

    private Map<EntityType<?>, Set<ResourceLocation>> scaredByMap = new HashMap<>();

    public PixelPetDataRegistry() {
        super(new GsonBuilder().create(), "pet_data");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager mgr, ProfilerFiller profiler) {
        species = new HashMap<>();
        jsons.forEach((id, json) -> {
            PixelPetData.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .result()
                    .ifPresentOrElse(data -> {
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
                    }, () -> PixelPets.LOGGER.info("Failed to load PixelPet {}", id));
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

    public Set<ResourceLocation> getScaryPets(EntityType<?> t) {
        return scaredByMap.getOrDefault(t, Collections.emptySet());
    }

    /// Built in {@link com.bonsai.pixelpets.mixin.ReloadableServerResourcesMixin} </br>
    /// Resulting map used in {@link com.bonsai.pixelpets.mixin.MobMixin} to give AvoidPixelPetGoals to relevant entities </br>
    public void buildScaredByMap() {
        scaredByMap = new HashMap<>();
        species.forEach((id, data) -> {
            data.scares().forEach(scareValue -> {
                switch (scareValue) {
                    case ScareValue.Element e -> {
                        BuiltInRegistries.ENTITY_TYPE.getOptional(e.id())
                                .ifPresent(type -> scaredByMap.computeIfAbsent(type, k -> new HashSet<>()).add(id));
                    }
                    case ScareValue.Tag t -> {
                        BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(t.tag())
                                .forEach(holder -> scaredByMap.computeIfAbsent(holder.value(), k -> new HashSet<>()).add(id));
                    }
                }
            });
        });

        //logScaredByMap();
    }

    private void logScaredByMap() {
        PixelPets.LOGGER.info("Scared-by map ({} entries):", this.scaredByMap.size());
        this.scaredByMap.forEach((entityType, petIds) -> {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            PixelPets.LOGGER.info("  {} is scared by: {}", entityId, petIds);
        });
    }

}
