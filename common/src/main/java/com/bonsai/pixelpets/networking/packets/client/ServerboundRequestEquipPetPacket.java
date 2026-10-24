package com.bonsai.pixelpets.networking.packets.client;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.networking.NetworkHandler;
import com.bonsai.pixelpets.networking.packets.server.ClientboundSyncPetsPacket;
import com.bonsai.pixelpets.pixelpets.PixelPet;
import com.bonsai.pixelpets.pixelpets.PixelPetsInventory;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public record ServerboundRequestEquipPetPacket(int slotIndex, boolean fromEquipped) implements Packet<ServerboundRequestEquipPetPacket> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PixelPets.MOD_ID, "request_equip_pet");
    public static final ServerboundPacketType<ServerboundRequestEquipPetPacket> TYPE = new Handler();
    
    public static void sendToServer(int slotIndex, boolean fromEquipped) {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundRequestEquipPetPacket(slotIndex, fromEquipped));
    }
    
    @Override
    public PacketType<ServerboundRequestEquipPetPacket> type() {
        return TYPE;
    }
    
    private static final class Handler implements ServerboundPacketType<ServerboundRequestEquipPetPacket> {
        @Override
        public void encode(ServerboundRequestEquipPetPacket message, RegistryFriendlyByteBuf buffer) {
            buffer.writeInt(message.slotIndex());
            buffer.writeBoolean(message.fromEquipped());
        }
        
        @Override
        public ServerboundRequestEquipPetPacket decode(RegistryFriendlyByteBuf buffer) {
            return new ServerboundRequestEquipPetPacket(buffer.readInt(), buffer.readBoolean());
        }
        
        @Override
        public ResourceLocation id() {
            return ID;
        }
        
        @Override
        public Consumer<Player> handle(ServerboundRequestEquipPetPacket message) {
            return player -> {
                if (player != null && player.level() instanceof ServerLevel server) {
                    PixelPetsInventory inventory = ((PlayerPetAccess) player).pixelPets$getPetInventory();
                    
                    PixelPet target = message.fromEquipped() ? inventory.equipped.get(message.slotIndex()) : inventory.stored.get(message.slotIndex());
                    
                    if (message.fromEquipped()) {
                        if (target.getPetUUID() != null) {
                            Entity entity = server.getEntity(target.getPetUUID());
                            if (entity != null) entity.discard();
                        }
                        
                        this.storePet(inventory, message.slotIndex());
                    } else {
                        if (!target.isEmpty()) {
                            try {
                                EntityType<? extends AbstractPixelPetEntity> type = target.getEntityType(player.registryAccess());
                                AbstractPixelPetEntity pet = type.create(server);
                                
                                if (pet != null) {
                                    pet.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                                    
                                    pet.initialize(target.getDataLocation(), target.getLevel(), inventory.petStatus);
                                    pet.tame(player);
                                    
                                    server.addFreshEntity(pet);
                                    target.setPetUUID(pet.getUUID());
                                }
                            } catch (Exception error) {
                                PixelPets.LOGGER.error("Failed on loading tamed pet", error);
                            }
                        }
                        
                        this.equipPet(inventory, message.slotIndex());
                    }
                    
                    CompoundTag inventoryTag = new CompoundTag();
                    inventory.save(inventoryTag);
                    ClientboundSyncPetsPacket.sendToClient(player, inventoryTag);
                }
            };
        }
        
        private void storePet(PixelPetsInventory inventory, int equippedIndex) {
            if (equippedIndex < 0 || equippedIndex >= inventory.equipped.size()) return;
            PixelPet pet = inventory.equipped.get(equippedIndex);
            
            if (!pet.isEmpty()) {
                for (int i = 0; i < inventory.stored.size(); i++) {
                    if (inventory.stored.get(i).isEmpty()) {
                        pet.setPetUUID(null);
                        inventory.stored.set(i, pet);
                        inventory.equipped.set(equippedIndex, PixelPet.EMPTY);
                        break;
                    }
                }
            }
        }
        
        private void equipPet(PixelPetsInventory inventory, int storageIndex) {
            if (storageIndex < 0 || storageIndex >= inventory.stored.size()) return;
            PixelPet pet = inventory.stored.get(storageIndex);
            
            if (!pet.isEmpty()) {
                for (int i = 0; i < inventory.equipped.size(); i++) {
                    if (inventory.equipped.get(i).isEmpty()) {
                        inventory.equipped.set(i, pet);
                        inventory.stored.set(storageIndex, PixelPet.EMPTY);
                        return;
                    }
                }
                
                PixelPet firstEquipped = inventory.equipped.get(0);
                if (firstEquipped.getPetUUID() != null && inventory.player.level() instanceof ServerLevel serverLevel) {
                    Entity oldEntity = serverLevel.getEntity(firstEquipped.getPetUUID());
                    if (oldEntity != null) oldEntity.discard();
                }
                
                firstEquipped.setPetUUID(null);
                inventory.equipped.set(0, pet);
                inventory.stored.set(storageIndex, firstEquipped);
            }
        }
    }
}