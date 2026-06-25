package com.bonsai.pixelpets.mixin;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.pixelpets.pixelpetdata.LeveledAttackData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Projectile.class)
public class ProjectileMixin {

    @Shadow
    private UUID ownerUUID;

    // TODO untestable until petInventory implemented
    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    protected void canHitEntity(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Projectile self = (Projectile) (Object) this;

        // Prevent pet projectiles from hitting their owner or friends
        if ((self.getOwner() instanceof AbstractPixelPetEntity pixelPet)) {
            UUID ownerUUID = pixelPet.getOwnerUUID();
            if (ownerUUID == null) {
                return;
            }

            if (target.getUUID().equals(ownerUUID)) {
                cir.setReturnValue(false);
                return;
            }

            Player ownerOwner = self.level().getPlayerByUUID(ownerUUID);
            if (ownerOwner != null) {
                if ((((PlayerPetAccess) ownerOwner).pixelPets$getActivePets()).contains(target.getUUID())) {
                    cir.setReturnValue(false);
                }
            }
        }
        // Prevent player projectiles from hitting their pets // TODO uncertain if this should be added
//        else if (self.getOwner() instanceof Player player) {
//            if ((((PlayerPetAccess) player).pixelPets$getActivePets()).contains(target.getUUID())) {
//                cir.setReturnValue(false);
//            }
//        }
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    protected void onHitEntity(EntityHitResult result, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (this.ownerUUID == null) return;
        if (self.getOwner() instanceof AbstractPixelPetEntity pixelPet) {
            Entity target = result.getEntity();
            if (target instanceof LivingEntity livingEntity) {
                // Damage and Knockback
                DamageSource damagesource = self.damageSources().mobProjectile(self, pixelPet);
                if (target.hurt(damagesource, pixelPet.getDamage())) {
                    pixelPets$doKnockback(self, pixelPet, livingEntity);
                }

                // Status Effects
                for (LeveledAttackData.StatusEffectApplication effect : pixelPet.getOnHitStatuses()) {
                    if (pixelPet.getRandom().nextFloat() < effect.chance()) {
                        livingEntity.addEffect(new MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier()));
                    }
                }


            }

            // TODO handle projectile effects (ricochet, grow, etc.)
        }
    }

    @Unique
    protected void pixelPets$doKnockback(Projectile self, AbstractPixelPetEntity pixelPet, LivingEntity entity) {
        double d1 = Math.max(0.0, 1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 vec3 = self.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(pixelPet.getKnockback() * 0.6 * d1);
        if (vec3.lengthSqr() > 0.0) {
            entity.push(vec3.x, 0.1, vec3.z);
        }
    }

}
