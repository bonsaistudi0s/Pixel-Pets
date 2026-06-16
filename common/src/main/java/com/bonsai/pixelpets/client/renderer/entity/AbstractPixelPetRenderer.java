package com.bonsai.pixelpets.client.renderer.entity;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.client.model.entity.AbstractPixelPetModel;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AbstractPixelPetRenderer extends GeoEntityRenderer<AbstractPixelPetEntity> {

    private static final ResourceLocation DEFAULT_TEXTURE = PixelPets.identifier("textures/entity/capybara.png");

    public AbstractPixelPetRenderer(EntityRendererProvider.Context context) {
        super(context, new AbstractPixelPetModel());
    }

//    @Override
//    public ResourceLocation getTextureLocation(AbstractPixelPetEntity entity) {
//        return entity.getDataLocation() == null ? DEFAULT_TEXTURE
//                : PixelPets.identifier("textures/entity/" + entity.getDataLocation().getPath() + ".png");
//    }
}
