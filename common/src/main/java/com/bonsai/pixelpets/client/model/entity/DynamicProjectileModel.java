package com.bonsai.pixelpets.client.model.entity;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetProjectile;
import com.bonsai.pixelpets.pixelpets.registration.data.ProjectileRenderData;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class DynamicProjectileModel extends DefaultedEntityGeoModel<AbstractPixelPetProjectile> {

    private static final ResourceLocation FALLBACK = PixelPets.identifier("empty_pet_projectile");

    public DynamicProjectileModel() {
        super(FALLBACK);
    }

    private ResourceLocation pathOrDefault(ResourceLocation loc) {
        return (loc == null || loc.getPath().isEmpty()) ? FALLBACK : loc;
    }

    @Override
    public ResourceLocation getModelResource(AbstractPixelPetProjectile projectile) {
        return buildFormattedModelPath(pathOrDefault(renderData(projectile).model()));
    }

    @Override
    public ResourceLocation getTextureResource(AbstractPixelPetProjectile projectile) {
        return buildFormattedTexturePath(pathOrDefault(renderData(projectile).texture()));
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractPixelPetProjectile projectile) {
        return buildFormattedAnimationPath(pathOrDefault(renderData(projectile).animation()));
    }

    private ProjectileRenderData.Modeled renderData(AbstractPixelPetProjectile projectile) {
        return (ProjectileRenderData.Modeled) projectile.getRenderData();
    }
}
