package com.bonsai.pixelpets;

import com.bonsai.pixelpets.client.renderer.entity.AbstractPixelPetRenderer;
import com.bonsai.pixelpets.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class PixelPetsNeoforgeClient {
    @SubscribeEvent
    public static void registerBlocks(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            //ItemBlockRenderTypes.setRenderLayer();
        });
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Block entity renderers
        //event.registerBlockEntityRenderer();

        // Entity renderers
        event.registerEntityRenderer(ModEntities.WALKING_PET, AbstractPixelPetRenderer::new);
        event.registerEntityRenderer(ModEntities.SWIMMING_PET, AbstractPixelPetRenderer::new);
        event.registerEntityRenderer(ModEntities.AMPHIBIOUS_PET, AbstractPixelPetRenderer::new);

        // Projectile renderers
        //event.registerEntityRenderer();
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        //event.registerSpriteSet();
    }

    // Custom S2C payload handlers
    public static class ClientPayloadHandler {

    }
}
