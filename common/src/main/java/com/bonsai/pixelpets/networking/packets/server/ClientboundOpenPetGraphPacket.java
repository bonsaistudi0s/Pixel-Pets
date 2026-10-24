package com.bonsai.pixelpets.networking.packets.server;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.client.PixelPetsClientHandler;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ClientboundOpenPetGraphPacket(List<String> caughtPets)
        implements Packet<ClientboundOpenPetGraphPacket> {

    public static final ClientboundPacketType<ClientboundOpenPetGraphPacket> TYPE =
            CodecPacketType.Client.create(
                    PixelPets.id("open_pet_graph"),
                    ByteCodec.STRING.listOf().map(ClientboundOpenPetGraphPacket::new, ClientboundOpenPetGraphPacket::caughtPets),
                    message -> () -> {
                        Set<ResourceLocation> caughtPets = message.caughtPets().stream()
                                .map(ResourceLocation::tryParse)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                        PixelPetsClientHandler.openGraphScreen(caughtPets);
                    }
            );

    public ClientboundOpenPetGraphPacket(Set<ResourceLocation> caughtPets) {
        this(caughtPets.stream().map(ResourceLocation::toString).toList());
    }

    @Override
    public PacketType<ClientboundOpenPetGraphPacket> type() {
        return TYPE;
    }
}