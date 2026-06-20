package com.bonsai.pixelpets.entities;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.pixelpets.PixelPetData;
import com.bonsai.pixelpets.pixelpets.PixelPetDataRegistry;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AbstractPixelPetEntity extends TamableAnimal implements GeoEntity {

    private static final Logger log = LoggerFactory.getLogger(AbstractPixelPetEntity.class);
    /// Geckolib </br>
    /// DevNote: Make sure the animations in *.animation.json match the form below
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.walk");
    public static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.run");
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    public static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.sit");
    public static final RawAnimation SLEEPING = RawAnimation.begin().thenLoop("animation.sleeping");
    public static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.swim");
    public static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.attack");
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>(this, "Sit", 0,
                        (state) -> (this.isInSittingPose()) ? state.setAndContinue(SIT) : PlayState.STOP),
                new AnimationController<>(this, "Swim", 0,
                        (state) -> this.isSwimming() ? state.setAndContinue(SWIM) : PlayState.STOP),
                new AnimationController<>(this, "Walk/Run/Idle", 0,
                        (state) -> state.isMoving() ? state.setAndContinue(this.isSprinting() ? RUN : WALK) : state.setAndContinue(IDLE))
        );
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /// Pet stuff

    private static final EntityDataAccessor<String> DATA_ID =
            SynchedEntityData.defineId(AbstractPixelPetEntity.class, EntityDataSerializers.STRING);

    public AbstractPixelPetEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(false, false);

        float missingHealth = this.getBaseHealth() - this.getHealth();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getBaseHealth());
        this.setHealth(Math.max(1.0f, getBaseHealth() - missingHealth));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID, "");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getDataLocation() != null) {
            tag.putString("data_location", getDataLocation().toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("data_location")) {
            setDataLocation(ResourceLocation.parse(tag.getString("data_location")));
        }
    }

    public void setDataLocation(ResourceLocation id) {
        this.entityData.set(DATA_ID, id.toString());
    }

    public ResourceLocation getDataLocation() {
        return ResourceLocation.parse(this.entityData.get(DATA_ID));
    }

    public Optional<PixelPetData> getData() {
        return PixelPetDataRegistry.INSTANCE.get(getDataLocation());
    }

    public ResourceLocation getAnimationId() {
        return getData().map(PixelPetData::animationId).orElse(PixelPetData.DEFAULT_ANIMATION_ID);
    }

    public int getBaseHealth() {
        return getData().map(PixelPetData::baseHealth).orElse(PixelPetData.DEFAULT_BASE_HEALTH);
    }

    public Item getTameItem() {
        return getData().map(PixelPetData::tameItem).orElse(PixelPetData.DEFAULT_TAME_ITEM);
    }

    public int getTameChance() {
        return getData().map(PixelPetData::tameChance).orElse(PixelPetData.DEFAULT_TAME_CHANCE);
    }

    public String getGenericName() {
        return getData().map(PixelPetData::genericName).orElse(PixelPetData.DEFAULT_GENERIC_NAME);
    }

    public boolean canAttack() {
        return getData().map(PixelPetData::attacking).orElse(PixelPetData.DEFAULT_ATTACKING);
    }

    private List<UUID> getOtherPets() {
        if (!this.isTame()) return List.of();
        if (this.getOwner() == null) return List.of();
        if (this.getOwner() instanceof Player player) {
            return ((PlayerPetAccess) player).pixelPets$getActivePets();
        }
        return List.of();
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new GentleFloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this)); // TODO custom goal here? idk if sitting should be possible for normal pet use
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0f, 6.0f, 1.5f));
        this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0f));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                // Replaced in constructor with PixelPetData value
                .add(Attributes.MAX_HEALTH, 8)

                // Normal
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.7f)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.FOLLOW_RANGE, 10);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(getTameItem());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    if (!this.level().isClientSide()) {
                        this.usePlayerItem(player, hand, itemstack);
                        this.heal(5.0F); // TODO choose good heal amount
                    }

                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }

                InteractionResult interactionresult = super.mobInteract(player, hand);
                if (!interactionresult.consumesAction()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }

                return interactionresult;
            }
        } else if (this.isFood(itemstack)) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem(player, hand, itemstack);
                this.tryToTame(player);
                this.setPersistenceRequired();
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    private void tryToTame(Player player) {
        if (this.random.nextInt(100) <= getTameChance()) {
            // TODO add to player pet inv here
            //  If empty slot, auto equip and link uuid
            //  Else despawn pixel effect
            this.tame(player);
            this.level().broadcastEntityEvent(this, (byte)7);
        } else {
            this.level().broadcastEntityEvent(this, (byte)6);
        }

    }

    @Override
    public void tick() {
        super.tick();

        this.setSprinting(this.getOwner() != null && this.getOwner().isSprinting());
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return super.canSpawnSprintParticle() && this.getDeltaMovement().lengthSqr() > 0.01f;
    }

    @Override
    public void updateSwimming() {
        this.setSwimming(this.isInWater() && !this.isPassenger());
    }

    public static class GentleFloatGoal extends FloatGoal {
        private final Mob mob;

        public GentleFloatGoal(Mob mob) {
            super(mob);
            this.mob = mob;
        }

        @Override
        public void tick() {
            if (this.mob.isInLava()) {
                if (this.mob.getRandom().nextFloat() < 0.8F) {
                    this.mob.getJumpControl().jump();
                }
            } else {
                Vec3 motion = this.mob.getDeltaMovement();

                double fluidHeight = this.mob.getFluidHeight(FluidTags.WATER);
                double targetSubmersion = 0.1f;

                double diff = fluidHeight - targetSubmersion;

                double correction = Math.clamp(diff * 0.2D, -0.04D, 0.04D);

                this.mob.setDeltaMovement(motion.x, motion.y + correction, motion.z);
            }
        }
    }
}
