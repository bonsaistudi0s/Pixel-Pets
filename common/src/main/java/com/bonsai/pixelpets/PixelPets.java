package com.bonsai.pixelpets;

import com.bonsai.pixelpets.platform.Services;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PixelPets {

    public static final String MOD_ID = "pixelpets";
    public static final String MOD_NAME = "Pixel Pets";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"}; // Nice for components

    public static void init() {
        LOGGER.info("Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
    }

    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(PixelPets.MOD_ID, path);
    }
}