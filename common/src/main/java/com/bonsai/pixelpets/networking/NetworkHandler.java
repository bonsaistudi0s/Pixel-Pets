package com.bonsai.pixelpets.networking;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.networking.packets.client.ServerboundRequestEquipPetPacket;
import com.bonsai.pixelpets.networking.packets.server.ClientboundSyncPetsPacket;
import com.teamresourceful.resourcefullib.common.network.Network;
import net.minecraft.resources.ResourceLocation;

public final class NetworkHandler {
    public static final Network CHANNEL = new Network(ResourceLocation.fromNamespaceAndPath(PixelPets.MOD_ID, "main"), 0);
    
    public static void init() {
        CHANNEL.register(ServerboundRequestEquipPetPacket.TYPE);
        CHANNEL.register(ClientboundSyncPetsPacket.TYPE);
    }
}