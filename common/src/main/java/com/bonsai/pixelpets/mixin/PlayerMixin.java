package com.bonsai.pixelpets.mixin;

import com.bonsai.pixelpets.pixelpets.PixelPetsInventory;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerPetAccess {

    // TODO should despawn all pets on teleport and log out

    @Unique
    final PixelPetsInventory pixelPets$inventory = new PixelPetsInventory((Player) (Object) this); // FIXME needs testing, idk if player is accessible here

    @Unique
    final Set<ResourceLocation> pixelPets$caughtPets = new HashSet<>();

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void pixelPetsInventoryTick(CallbackInfo ci) {
        this.pixelPets$inventory.tick();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addPixelPetsSaveData(CompoundTag compound, CallbackInfo ci) {
        compound.put("PixelPetsInventory", this.pixelPets$inventory.save(new CompoundTag()));

        ListTag list = new ListTag();
        for (ResourceLocation id : pixelPets$caughtPets) {
            list.add(StringTag.valueOf(id.toString()));
        }
        compound.put("PixelPetsCaughtPets", list);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readPixelPetsSaveData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("PixelPetsInventory", 10)) {
            this.pixelPets$inventory.load(compound.getCompound("PixelPetsInventory"));
        }

        pixelPets$caughtPets.clear();
        ListTag list = compound.getList("PixelPetsCaughtPets", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                pixelPets$caughtPets.add(id);
            }
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

    @Override
    public Set<ResourceLocation> pixelPets$getCaughtPets() {
        return this.pixelPets$caughtPets;
    }

    @Override
    public boolean pixelPets$addCaughtPet(ResourceLocation id) {
        if (id == null) return false;
        return this.pixelPets$caughtPets.add(id);
    }

    @Override
    public boolean pixelPets$removeCaughtPet(ResourceLocation id) {
        return this.pixelPets$caughtPets.remove(id);
    }
}
