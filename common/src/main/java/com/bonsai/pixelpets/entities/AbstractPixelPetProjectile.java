package com.bonsai.pixelpets.entities;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.pixelpets.registration.data.ProjectileRenderData;
import com.bonsai.pixelpets.pixelpets.registration.data.StatusEffectApplication;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractPixelPetProjectile extends Projectile implements GeoEntity {

    /// Geckolib
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final RawAnimation ANIMATE = RawAnimation.begin().thenLoop("animate");
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "Animate", 0, (state) -> state.setAndContinue(ANIMATE))
        );
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }


    private static final EntityDataAccessor<String> DATA_ID =
            SynchedEntityData.defineId(AbstractPixelPetProjectile.class, EntityDataSerializers.STRING);

    private final TargetingConditions TARGETING_CONDITION = TargetingConditions.forCombat().selector((entity -> !this.ricochetIgnoreEntityIds.contains(entity.getId())));

    @Nullable
    private IntOpenHashSet ricochetIgnoreEntityIds;
    private int ricochet = 0;

    // Growth scale
    private static final EntityDataAccessor<Float> GROWTH =
            SynchedEntityData.defineId(AbstractPixelPetProjectile.class, EntityDataSerializers.FLOAT);
    // Growth rate
    private static final EntityDataAccessor<Integer> GROWING =
            SynchedEntityData.defineId(AbstractPixelPetProjectile.class, EntityDataSerializers.INT);

    public AbstractPixelPetProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        if (owner instanceof AbstractPixelPetEntity pet) {
            this.ricochet = pet.getRicochet();
            setGrowing(pet.getGrowing());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID, "");
        builder.define(GROWTH, 1.0f);
        builder.define(GROWING, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getDataLocation() != null) {
            tag.putString("data_location", getDataLocation().toString());
        }
        tag.putFloat("growth", getGrowth());
        tag.putInt("growing", getGrowing());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("data_location")) {
            setDataLocation(ResourceLocation.parse(tag.getString("data_location")));
        }
        setGrowth(tag.contains("growth") ? tag.getFloat("growth") : 1.0f);
        setGrowing(tag.contains("growing") ? tag.getInt("growing") : 0);
    }

    public void setGrowth(float growth) {
        this.entityData.set(GROWTH, growth);
    }
    public float getGrowth() {
        return this.entityData.get(GROWTH);
    }

    public void setGrowing(int growing) {
        this.entityData.set(GROWING, growing);
    }
    public int getGrowing() {
        return this.entityData.get(GROWING);
    }

    // 3x is 200% bigger
    // growing 1 takes 2 seconds (40 ticks) to reach 3x
    public void increaseGrowth() {
        if (getGrowing() > 0) {
            setGrowth(Math.clamp(getGrowth() + ((float) getGrowing() * 0.1f), 1.0f, 3.0f));
            this.refreshDimensions();
        }
    }

    public void setDataLocation(ResourceLocation id) {
        this.entityData.set(DATA_ID, id.toString());
        this.refreshDimensions();
    }

    public ResourceLocation getDataLocation() {
        return ResourceLocation.parse(this.entityData.get(DATA_ID));
    }

    public Optional<PixelPetData> getData() {
        return level().registryAccess()
                .registry(PixelPets.PET_DATA)
                .flatMap(reg -> reg.getOptional(getDataLocation()));
    }

    public ProjectileRenderData getRenderData() {
        return getData().map(PixelPetData::projectileRenderData).orElse(PixelPetData.DEFAULT_PROJECTILE_RENDER_DATA);
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hitresult);
        }

        // Once every 2 ticks feels smoother in-game at 20TPS
        if (this.level().getGameTime() % 2 == 0) increaseGrowth();

        movementUpdate();
    }

    protected abstract void movementUpdate();

    @Override
    protected boolean canHitEntity(Entity target) {
        if (this.ricochetIgnoreEntityIds != null && this.ricochetIgnoreEntityIds.contains(target.getId())) {
            return false;
        }

        // Prevent pet projectiles from hitting their owner or friends
        if (this.getOwner() instanceof AbstractPixelPetEntity pixelPet) {
            UUID ownerUUID = pixelPet.getOwnerUUID();
            if (ownerUUID == null) {
                return super.canHitEntity(target);
            }

            if (target.getUUID().equals(ownerUUID)) {
                return false;
            }

            Player ownerOwner = this.level().getPlayerByUUID(ownerUUID);
            if (ownerOwner != null) {
                if ((((PlayerPetAccess) ownerOwner).pixelPets$getActivePets()).contains(target.getUUID())) {
                    return false;
                }
            }
        }

        // Prevent player projectiles from hitting their pets // TODO uncertain if this should be added
//        else if (self.getOwner() instanceof Player player) {
//            if ((((PlayerPetAccess) player).pixelPets$getActivePets()).contains(target.getUUID())) {
//                cir.setReturnValue(false);
//            }
//        }

        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.getOwner() == null) {
            this.discard();
            return;
        }

        Entity target = result.getEntity();
        if (this.ricochet > 0) {
            if (this.ricochetIgnoreEntityIds == null) {
                this.ricochetIgnoreEntityIds = new IntOpenHashSet(this.ricochet);
            }

            if (this.ricochetIgnoreEntityIds.size() >= this.ricochet) {
                this.discard();
            } else {
                this.ricochetIgnoreEntityIds.add(target.getId());

                // TODO it's a bit jerky
                if (!this.level().isClientSide()) {
                    LivingEntity newTarget = this.level().getNearestEntity(
                            LivingEntity.class,
                            TARGETING_CONDITION,
                            (LivingEntity) this.getOwner(),
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            this.getBoundingBox().inflate(6, 1, 6)); // TODO choose good size, maybe only check in front of the projectile path?
                    if (newTarget != null) {
                        double velocity = this.getDeltaMovement().length();
                        double d0 = newTarget.getEyeY() - (double)1.1F;
                        double d1 = newTarget.getX() - this.getX();
                        double d2 = d0 - this.getY();
                        double d3 = newTarget.getZ() - this.getZ();
                        double d4 = Math.sqrt(d1 * d1 + d3 * d3) * (double)0.2F;

                        this.shoot(d1, d2 + d4, d3, (float) velocity, 0.0f);
                    }
                }
            }
        }

        if (this.getOwner() instanceof AbstractPixelPetEntity pixelPet) {
            if (target instanceof LivingEntity livingEntity) {
                doHitEntity(pixelPet, livingEntity);
            }
        }

        if (this.ricochet <= 0) {
            this.discard();
        }
    }

    // Override for more unique effects (like lingering puddle)
    private void doHitEntity(AbstractPixelPetEntity pixelPet, LivingEntity target) {
        DamageSource damagesource = this.damageSources().mobProjectile(this, pixelPet);
        if (target.hurt(damagesource, pixelPet.getDamage() * this.getGrowth())) {
            doKnockback(pixelPet, target);

            // Status Effects
            for (StatusEffectApplication effect : pixelPet.getOnHitStatuses()) {
                if (pixelPet.getRandom().nextFloat() < effect.chance()) {
                    target.addEffect(new MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier()));
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    protected void doKnockback(AbstractPixelPetEntity pixelPet, LivingEntity entity) {
        double d1 = Math.max(0.0, 1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(pixelPet.getKnockback() * 0.6 * d1);
        if (vec3.lengthSqr() > 0.0) {
            entity.push(vec3.x, 0.1, vec3.z);
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(this.getGrowth() * this.getRenderData().scale());
    }
}
