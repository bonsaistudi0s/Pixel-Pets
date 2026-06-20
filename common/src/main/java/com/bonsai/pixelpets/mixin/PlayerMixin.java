package com.bonsai.pixelpets.mixin;

import com.bonsai.pixelpets.pixelpets.PixelPetsInventory;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerPetAccess {

    @Unique
    final PixelPetsInventory pixelPets$inventory = new PixelPetsInventory((Player) (Object) this); // FIXME needs testing, idk if player is accessible here

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void pixelPetsInventoryTick(CallbackInfo ci) {
        this.pixelPets$inventory.tick();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addPixelPetInventorySaveData(CompoundTag compound, CallbackInfo ci) {
        compound.put("PixelPetsInventory", this.pixelPets$inventory.save(new CompoundTag()));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readPixelPetInventorySaveData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("PixelPetsInventory", 10)) {
            this.pixelPets$inventory.load(compound.getCompound("PixelPetsInventory"));
        }
    }

    /// PlayerPetAccess methods:

    @Unique
    public List<UUID> pixelPets$getActivePets() {
        return this.pixelPets$inventory.getEquippedPetUUIDs();
    }

    @Override
    public PixelPetsInventory pixelPets$getPetInventory() {
        return this.pixelPets$inventory;
    }
}
