package com.bonsai.pixelpets.client.renderer.entity;

import com.bonsai.pixelpets.entities.AbstractPixelPet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbstractPixelPetRenderer extends GeoEntityRenderer<AbstractPixelPet> {
    public AbstractPixelPetRenderer(EntityRendererProvider.Context context, EntityType<? extends AbstractPixelPet> entityType) {
        super(context, entityType);
    }
}
