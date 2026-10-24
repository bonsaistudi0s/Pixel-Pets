package com.bonsai.pixelpets.pixelpets;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PlayerPetAccess {

    List<UUID> pixelPets$getActivePets();

    PixelPetsInventory pixelPets$getPetInventory();


    Set<ResourceLocation> pixelPets$getCaughtPets();
    boolean pixelPets$addCaughtPet(ResourceLocation id);
    // Just for commands/testing
    boolean pixelPets$removeCaughtPet(ResourceLocation id);
}
