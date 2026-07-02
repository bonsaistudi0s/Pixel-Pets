package com.bonsai.pixelpets.client.renderer.entity;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.client.model.entity.DynamicProjectileModel;
import com.bonsai.pixelpets.entities.AbstractPixelPetProjectile;
import com.bonsai.pixelpets.pixelpets.registration.data.ProjectileRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PixelPetProjectileRenderer extends GeoEntityRenderer<AbstractPixelPetProjectile> {

    private final ItemRenderer itemRenderer;

    public PixelPetProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new DynamicProjectileModel());
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void actuallyRender(PoseStack poseStack, AbstractPixelPetProjectile animatable, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        ProjectileRenderData data = animatable.getRenderData();

        float s = data.scale() * animatable.getGrowth();
        poseStack.pushPose();
        poseStack.scale(s,s,s);

        if (data instanceof ProjectileRenderData.ItemBased itemBased) {
            renderAsItem(animatable, itemBased.item().get(), poseStack, bufferSource, packedLight);
        } else {
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        }

        poseStack.popPose();
    }

    private void renderAsItem(AbstractPixelPetProjectile entity, Item item, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)) {
            poseStack.pushPose();
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            this.itemRenderer
                    .renderStatic(
                            new ItemStack(item),
                            ItemDisplayContext.GROUND,
                            packedLight,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            bufferSource,
                            entity.level(),
                            entity.getId()
                    );
            poseStack.popPose();
        }
    }

    @Override
    protected int getBlockLightLevel(AbstractPixelPetProjectile entity, BlockPos pos) {
        ProjectileRenderData data = entity.getRenderData();
        return data.fullBright() ? 15 : super.getBlockLightLevel(entity, pos);
    }
}
