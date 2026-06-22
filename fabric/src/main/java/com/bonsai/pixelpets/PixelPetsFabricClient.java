package com.bonsai.pixelpets;

import com.bonsai.pixelpets.client.renderer.entity.AbstractPixelPetRenderer;
import com.bonsai.pixelpets.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class PixelPetsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // Entities
        EntityRendererRegistry.register(ModEntities.WALKING_PET, AbstractPixelPetRenderer::new);
        EntityRendererRegistry.register(ModEntities.SWIMMING_PET, AbstractPixelPetRenderer::new);
        EntityRendererRegistry.register(ModEntities.AMPHIBIOUS_PET, AbstractPixelPetRenderer::new);

    }

}
