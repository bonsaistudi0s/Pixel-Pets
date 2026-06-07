package com.bonsai.pixelpets.pixelpets;

import com.bonsai.pixelpets.PixelPets;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

public class PixelPetsInventory {

    // 20 slots is 1 page size
    // multiple lists for each page or 1 list for all pages?
    public final NonNullList<PixelPet> stored = NonNullList.withSize(20, PixelPet.EMPTY);

    public final NonNullList<PixelPet> equipped = NonNullList.withSize(4, PixelPet.EMPTY);

    public PixelPetStatus petStatus = PixelPetStatus.PASSIVE;

    public final Player player;

    public PixelPetsInventory(Player player) {
        this.player = player;
    }

    public void tick() {
        for (PixelPet pet : this.equipped) {
            if (!pet.isEmpty()) {
                pet.tick(this.player.level(), this.player);
            }
        }
    }

    public CompoundTag save(CompoundTag compoundTag) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.stored.size(); i++) {
            if (!this.stored.get(i).isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte)i);
                listTag.add(this.stored.get(i).save(this.player.registryAccess(), compoundtag));
            }
        }

        for (int i = 0; i < this.equipped.size(); i++) {
            if (!this.equipped.get(i).isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("EquippedSlot", (byte)i);
                listTag.add(this.equipped.get(i).save(this.player.registryAccess(), compoundtag));
            }
        }

        compoundTag.put("Pets", listTag);
        compoundTag.putString("Status", this.petStatus.name());
        return compoundTag;
    }

    public void load(CompoundTag compoundTag) {
        this.stored.clear();
        this.equipped.clear();

        ListTag listTag = compoundTag.getList("Pets", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);

            if (tag.contains("Slot")) {
                int slot = tag.getByte("Slot") & 255;
                if (slot < this.stored.size()) {
                    PixelPet pet = PixelPet.load(this.player.registryAccess(), tag);
                    if (!pet.isEmpty()) this.stored.set(slot, pet);
                }
            } else if (tag.contains("EquippedSlot")) {
                int slot = tag.getByte("EquippedSlot") & 255;
                if (slot < this.equipped.size()) {
                    PixelPet pet = PixelPet.load(this.player.registryAccess(), tag);
                    if (!pet.isEmpty()) this.equipped.set(slot, pet);
                }
            }
        }

        if (compoundTag.contains("Status")) {
            try {
                this.petStatus = PixelPetStatus.valueOf(compoundTag.getString("Status"));
            } catch (IllegalArgumentException e) {
                PixelPets.LOGGER.warn("Unknown PixelPetStatus '{}', defaulting to PASSIVE", compoundTag.getString("Status"));
                this.petStatus = PixelPetStatus.PASSIVE;
            }
        }
    }

}
