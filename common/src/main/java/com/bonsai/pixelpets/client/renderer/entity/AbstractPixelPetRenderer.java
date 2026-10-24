package com.bonsai.pixelpets.client.renderer.entity;

import com.bonsai.pixelpets.client.model.entity.AbstractPixelPetModel;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbstractPixelPetRenderer extends GeoEntityRenderer<AbstractPixelPetEntity> {

    public AbstractPixelPetRenderer(EntityRendererProvider.Context context) {
        super(context, new AbstractPixelPetModel());
    }
}
