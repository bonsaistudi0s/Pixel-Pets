package com.bonsai.pixelpets.client.gui;

import com.bonsai.pixelpets.client.gui.screens.PixelPetsInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientAccess {
    public static void openPetScreen() {
        Minecraft.getInstance().setScreen(new PixelPetsInventoryScreen(Component.literal("Debug Pets")));
    }
}