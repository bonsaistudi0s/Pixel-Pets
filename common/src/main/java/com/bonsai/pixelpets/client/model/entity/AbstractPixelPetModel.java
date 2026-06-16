package com.bonsai.pixelpets.client.model.entity;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class AbstractPixelPetModel extends DefaultedEntityGeoModel<AbstractPixelPetEntity> {

    private static final ResourceLocation FALLBACK = PixelPets.identifier("capybara");
    private static final ResourceLocation ANIMATION_DEFAULT = PixelPets.identifier("default_pet");

    public AbstractPixelPetModel() {
        super(FALLBACK);
        this.withAltAnimations(ANIMATION_DEFAULT);
    }

    private ResourceLocation pathOrDefault(ResourceLocation loc) {
        return (loc == null || loc.getPath().isEmpty()) ? FALLBACK : loc;
    }

    private ResourceLocation animationPathOrDefault(ResourceLocation loc) {
        return (loc == null || loc.getPath().isEmpty()) ? ANIMATION_DEFAULT : loc;
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractPixelPetEntity animatable) {
        return buildFormattedAnimationPath(animationPathOrDefault(animatable.getAnimationId()));
    }

    @Override
    public ResourceLocation getModelResource(AbstractPixelPetEntity animatable) {
        return buildFormattedModelPath(pathOrDefault(animatable.getDataLocation()));
    }

    @Override
    public ResourceLocation getTextureResource(AbstractPixelPetEntity animatable) {
        return buildFormattedTexturePath(pathOrDefault(animatable.getDataLocation()));
    }
}