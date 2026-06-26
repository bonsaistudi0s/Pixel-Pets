package com.bonsai.pixelpets.pixelpets;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetDataRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class PixelPet {

    public static final PixelPet EMPTY = new PixelPet((Void)null);
    private PixelPet (Void v) {
        this.dataLocation = null;
    }

    private final ResourceLocation dataLocation;
    private String name = "Pixel Pet";
    private int level = 0;
    private UUID petUUID = null;

    private static final int MAX_FAINT_COOLDOWN = 20 * 60 * 5;
    private int faintCooldown;

    public static final Codec<PixelPet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("data").forGetter(PixelPet::getDataLocation),
            Codec.STRING.optionalFieldOf("name").forGetter(p -> Optional.ofNullable(p.getName())),
            ExtraCodecs.intRange(0, 99).fieldOf("level").orElse(0).forGetter(PixelPet::getLevel),
            UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(p -> Optional.ofNullable(p.getPetUUID())),
            ExtraCodecs.intRange(0, MAX_FAINT_COOLDOWN).fieldOf("faint_cooldown").orElse(0).forGetter(PixelPet::getFaintCooldown)
    ).apply(instance, PixelPet::new));

    public PixelPet(ResourceLocation dataLocation) {
        this.dataLocation = dataLocation;
    }

    public PixelPet(ResourceLocation dataLocation, Optional<String> name, int level, Optional<UUID> uuid, int faintCooldown) {
        this.dataLocation = dataLocation;
        name.ifPresent(n -> this.name = n);
        this.level = level;
        uuid.ifPresent(u -> this.petUUID = u);
        this.faintCooldown = faintCooldown;
    }

    public boolean isEmpty() {
        return this == EMPTY || this.dataLocation == null;
    }

    public ResourceLocation getDataLocation() {
        return this.dataLocation;
    }
    public Optional<PixelPetData> getData() {
        return PixelPetDataRegistry.INSTANCE.get(this.dataLocation);
    }

    public EntityType<? extends AbstractPixelPetEntity> getEntityType() {
        return getData()
                .map(PixelPetData::entityType)
                .orElseThrow(() -> new IllegalStateException("Unknown species: " + this.dataLocation));
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public UUID getPetUUID() {
        return petUUID;
    }
    public void setPetUUID(UUID petUUID) {
        this.petUUID = petUUID;
    }

    public int getFaintCooldown() {
        return faintCooldown;
    }
    public void setFaintCooldown(int faintCooldown) {
        this.faintCooldown = faintCooldown;
    }

    public void tick(Level level, Player player) {
        // Cooldown ticking
        // Respawning if possible
        // UUID checking
        // Apply effects
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.petUUID);
        } else {
            // Clientside
        }
    }

    public Tag save(HolderLookup.Provider registryAccess, Tag tag) {
        if (this.isEmpty()) throw new IllegalStateException("Cannot encode empty PixelPet");
        return CODEC.encode(this, registryAccess.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }

    public static PixelPet load(HolderLookup.Provider registryAccess, CompoundTag tag) {
        return CODEC.parse(
                registryAccess.createSerializationContext(NbtOps.INSTANCE),
                tag
        ).resultOrPartial(PixelPets.LOGGER::error).orElse(EMPTY);
    }
}
