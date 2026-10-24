package com.bonsai.pixelpets.networking.packets.server;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.client.PixelPetsClientHandler;
import com.bonsai.pixelpets.client.gui.screens.PixelPetsInventoryScreen;
import com.bonsai.pixelpets.networking.NetworkHandler;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ClientboundSyncPetsPacket(CompoundTag inventoryTag) implements Packet<ClientboundSyncPetsPacket> {
    public static final ResourceLocation ID = PixelPets.id("clientbound_sync_pets");
    public static final ClientboundPacketType<ClientboundSyncPetsPacket> TYPE = new Handler();
    
    public static void sendToClient(Player player, CompoundTag inventoryTag) {
        NetworkHandler.CHANNEL.sendToPlayer(new ClientboundSyncPetsPacket(inventoryTag), player);
    }
    
    @Override
    public PacketType<ClientboundSyncPetsPacket> type() {
        return TYPE;
    }
    
    private static final class Handler implements ClientboundPacketType<ClientboundSyncPetsPacket> {
        @Override
        public void encode(ClientboundSyncPetsPacket message, RegistryFriendlyByteBuf buffer) {
            buffer.writeNbt(message.inventoryTag());
        }
        
        @Override
        public ClientboundSyncPetsPacket decode(RegistryFriendlyByteBuf buffer) {
            return new ClientboundSyncPetsPacket(buffer.readNbt());
        }
        
        @Override
        public ResourceLocation id() {
            return ID;
        }
        
        @Override
        public Runnable handle(ClientboundSyncPetsPacket message) {
            return () -> PixelPetsClientHandler.openInventoryScreen(message);
        }
    }
}
