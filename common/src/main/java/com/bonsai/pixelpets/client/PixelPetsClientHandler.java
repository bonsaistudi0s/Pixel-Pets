package com.bonsai.pixelpets.client;

import com.bonsai.pixelpets.client.gui.screens.PixelPetsInventoryScreen;
import com.bonsai.pixelpets.client.gui.screens.graph.PixelPetGraphScreen;
import com.bonsai.pixelpets.networking.packets.server.ClientboundSyncPetsPacket;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class PixelPetsClientHandler {

    public static void openGraphScreen(Set<ResourceLocation> caughtPets) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        minecraft.setScreen(new PixelPetGraphScreen(Set.copyOf(caughtPets)));
    }

    public static void openInventoryScreen(ClientboundSyncPetsPacket message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ((PlayerPetAccess) mc.player).pixelPets$getPetInventory().load(message.inventoryTag());
            if (mc.screen instanceof PixelPetsInventoryScreen screen) {
                screen.updateSelectedPet(null, -1, false);
            } else {
                mc.setScreen(new PixelPetsInventoryScreen(Component.literal("Debug Pets")));
            }
        }
    }
}