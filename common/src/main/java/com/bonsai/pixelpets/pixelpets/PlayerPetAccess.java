package com.bonsai.pixelpets.pixelpets;

import java.util.List;
import java.util.UUID;

public interface PlayerPetAccess {

    List<UUID> pixelPets$getActivePets();

    PixelPetsInventory pixelPets$getPetInventory();
}
