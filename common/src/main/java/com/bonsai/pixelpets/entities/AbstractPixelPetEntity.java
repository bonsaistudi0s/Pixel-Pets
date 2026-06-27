package com.bonsai.pixelpets.entities;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.goals.PixelPetMeleeAttackGoal;
import com.bonsai.pixelpets.entities.goals.PixelPetRangedAttackGoal;
import com.bonsai.pixelpets.entities.goals.PixelPetRestWhenOrderedToGoal;
import com.bonsai.pixelpets.pixelpets.PixelPetStatus;
import com.bonsai.pixelpets.pixelpets.registration.data.LeveledAttackData;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.pixelpets.registration.data.StatusEffectApplication;
import com.bonsai.pixelpets.registry.ModAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class AbstractPixelPetEntity extends TamableAnimal implements GeoEntity, RangedAttackMob {
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
                new AnimationController<>(this, "Sit/Sleep", 10,
                        (state) -> (this.isInSittingPose()) ? (this.isResting() ? state.setAndContinue(SLEEPING) : state.setAndContinue(SIT)) : PlayState.STOP),
                new AnimationController<>(this, "Swim", 0,
                        (state) -> this.isSwimming() ? state.setAndContinue(SWIM) : PlayState.STOP),
                new AnimationController<>(this, "Walk/Run/Idle", 0,
                        (state) -> {
                            if (this.isInSittingPose() || this.isSwimming()) return PlayState.STOP;
                            return state.isMoving() ? state.setAndContinue(this.isSprinting() ? RUN : WALK) : state.setAndContinue(IDLE);
                        }),
                new AnimationController<>(this,"Attack", 0,
                        (animTest) -> PlayState.STOP).triggerableAnim("attack", ATTACK)
        );
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /// Pet stuff

    private static final EntityDataAccessor<String> DATA_ID =
            SynchedEntityData.defineId(AbstractPixelPetEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> RESTING =
            SynchedEntityData.defineId(AbstractPixelPetEntity.class, EntityDataSerializers.BOOLEAN);;

    // TODO edit to account for modded creepers?
    public static final Predicate<LivingEntity> TARGET_SELECTOR = (entity) -> {
        EntityType<?> entitytype = entity.getType();
        return entitytype != EntityType.CREEPER && !(entity instanceof NeutralMob);
    };

    private int abilityLevel = 1;
    private PixelPetStatus status = PixelPetStatus.PASSIVE;
    private LeveledAttackData.PixelPetAttackData currentAttack = null;
    private EntityType<Projectile> projectile = null;

    @Nullable
    private Goal currentAttackGoal = null;
    @Nullable
    private Goal activeTargetingGoal = null;

    public AbstractPixelPetEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public void initialize(ResourceLocation id, int level, PixelPetStatus status) {
        this.entityData.set(DATA_ID, id.toString());
        this.setAbilityLevel(level);
        this.setStatus(status);

        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            float missingHealth = (float) (maxHealth.getBaseValue() - this.getHealth());
            maxHealth.setBaseValue(getBaseHealth());
            this.setHealth(Math.max(1.0f, getBaseHealth() - missingHealth));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID, "");
        builder.define(RESTING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getDataLocation() != null) {
            tag.putString("data_location", getDataLocation().toString());
        }
        tag.putInt("ability_level", this.abilityLevel);
        tag.putString("status", this.status.getSerializedName());

        tag.putBoolean("sleeping", this.isResting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("data_location")) {
            setDataLocation(ResourceLocation.parse(tag.getString("data_location")));
        }
        setAbilityLevel(tag.contains("ability_level") ? tag.getInt("ability_level") : 1);
        setStatus(tag.contains("status") ? PixelPetStatus.byName(tag.getString("status")) : PixelPetStatus.PASSIVE);

        setResting(tag.contains("resting") && tag.getBoolean("resting"));
    }

    public void setStatus(PixelPetStatus status) {
        this.status = status;
        if (this.activeTargetingGoal != null) {
            this.targetSelector.removeGoal(this.activeTargetingGoal);
            this.activeTargetingGoal = null;
        }
        if (status == PixelPetStatus.ACTIVE && this.isTame()) {
            this.activeTargetingGoal = new NearestAttackableTargetGoal<>(this, Monster.class, false, TARGET_SELECTOR);
            this.targetSelector.addGoal(5, this.activeTargetingGoal);
        }
    }

    public void setAbilityLevel(int level) {
        this.abilityLevel = level;
        this.refreshAttack();
        // TODO other stuff that changes with level
    }

    private void refreshAttack() {
        LeveledAttackData.PixelPetAttackData resolved = getData().flatMap(data -> data.attack().resolve(this.abilityLevel)).orElse(null);

        // Attribute setting
        if (resolved != null) {
            AttributeInstance damageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.setBaseValue(resolved.damage());
            }

            // Range affects melee and ranged attacks, but the scale and defaults for those are very different
            // Defaulted to NEG_INF in LeveledAttackData, true default applied here
            AttributeInstance rangeAttr = this.getAttribute(ModAttributes.RANGE.holder());
            if (rangeAttr != null) {
                float r = resolved.range();
                if (r < 0.0f) r = (resolved.isRanged() ? 10.0f : 0.0f);
                rangeAttr.setBaseValue(r);
            }

            AttributeInstance atkSpdAttr = this.getAttribute(Attributes.ATTACK_SPEED);
            if (atkSpdAttr != null) {
                atkSpdAttr.setBaseValue(resolved.cooldown());
            }

            AttributeInstance knockbackAttr = this.getAttribute(Attributes.ATTACK_KNOCKBACK);
            if (knockbackAttr != null) {
                knockbackAttr.setBaseValue(resolved.knockback());
            }

            if (resolved.projectile().isPresent()) {
                Entity test = resolved.projectile().get().create(this.level());
                this.projectile = (test instanceof Projectile) ? resolved.projectile().get() : null;
                if (this.projectile != null) test.discard();
            }
        }

        boolean wasRanged = this.currentAttack != null && this.currentAttack.isRanged();
        boolean isRanged = resolved != null && resolved.isRanged();
        boolean typeChanged = this.currentAttack == null || wasRanged != isRanged;

        if (typeChanged) {
            if (this.currentAttackGoal != null) {
                this.goalSelector.removeGoal(this.currentAttackGoal);
                this.currentAttackGoal = null;
            }
            if (resolved != null) {
                this.currentAttackGoal = resolved.isRanged()
                        ? new PixelPetRangedAttackGoal(this)
                        : new PixelPetMeleeAttackGoal(this);
                this.goalSelector.addGoal(5, this.currentAttackGoal);
            }
        }

        this.currentAttack = resolved;
    }

    public void setDataLocation(ResourceLocation id) {
        this.entityData.set(DATA_ID, id.toString());
        this.setAbilityLevel(1);
    }

    public ResourceLocation getDataLocation() {
        return ResourceLocation.parse(this.entityData.get(DATA_ID));
    }

    public Optional<PixelPetData> getData() {
        return level().registryAccess()
                .registry(PixelPets.PET_DATA)
                .flatMap(reg -> reg.getOptional(getDataLocation()));
    }

    public ResourceLocation getAnimationId() {
        return getData().map(PixelPetData::animationId).orElse(PixelPetData.DEFAULT_ANIMATION_ID);
    }

    public int getBaseHealth() {
        return getData().map(PixelPetData::baseHealth).orElse(PixelPetData.DEFAULT_BASE_HEALTH);
    }

    public Ingredient getTameItemTest() {
        return getData().map(PixelPetData::tameItem).orElse(PixelPetData.DEFAULT_TAME_ITEM);
    }

    public int getTameChance() {
        return getData().map(PixelPetData::tameChance).orElse(PixelPetData.DEFAULT_TAME_CHANCE);
    }

    public String getGenericName() {
        return getData().map(PixelPetData::genericName).orElse(PixelPetData.DEFAULT_GENERIC_NAME);
    }

    public List<StatusEffectApplication> getOnHitStatuses() {
        if (this.currentAttack == null) return List.of();
        return this.currentAttack.statusEffects();
    }

    public float getDamage() {
        AttributeInstance damageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        return (damageAttr != null) ? (float) damageAttr.getValue() : 4.0f;
    }

    public float getKnockback() {
        AttributeInstance knockbackAttr = this.getAttribute(Attributes.ATTACK_KNOCKBACK);
        return (knockbackAttr != null) ? (float) knockbackAttr.getValue() : 0.0f;
    }

    public float getAttackSpeed() {
        AttributeInstance atkSpdAttr = this.getAttribute(Attributes.ATTACK_SPEED);
        return (atkSpdAttr != null) ? (float) atkSpdAttr.getValue() : 20.0f;
    }

    public float getRange() {
        AttributeInstance rangeAttr = this.getAttribute(ModAttributes.RANGE.holder());
        return (rangeAttr != null) ? (float) rangeAttr.getValue() : (this.currentAttack.isRanged() ? 10.0f : 0.0f);
    }

    private List<UUID> getOtherPets() {
        if (!this.isTame()) return List.of();
        if (this.getOwner() == null) return List.of();
        if (this.getOwner() instanceof Player player) {
            return ((PlayerPetAccess) player).pixelPets$getActivePets().stream().filter(uuid -> uuid.compareTo(this.uuid) != 0).toList();
        }
        return List.of();
    }

    public void setResting(boolean resting) {
        this.entityData.set(RESTING, resting);
    }
    public boolean isResting() {
        return this.entityData.get(RESTING);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(2, new PixelPetRestWhenOrderedToGoal(this));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                // Replaced later with PixelPetData value
                .add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(ModAttributes.RANGE.holder(), 0.0f)         // Controls melee and ranged range, default applies to melee
                .add(Attributes.ATTACK_SPEED, 20)       // # ticks until can attack again

                // Normal
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.75f)
                .add(Attributes.FOLLOW_RANGE, 10);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return getTameItemTest().test(itemStack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    if (!this.level().isClientSide()) {
                        this.usePlayerItem(player, hand, itemstack);
                        this.heal(5.0F);
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
            //  Else despawn
            this.tame(player);
            this.setStatus(this.status);
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

    // TODO despawn pet if can't catch up to player
    public void tryToTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        if (livingentity != null) {
            this.teleportToAroundBlockPos(livingentity.blockPosition());
        }
    }

    private void teleportToAroundBlockPos(BlockPos pos) {
        for(int i = 0; i < 10; ++i) {
            int j = this.random.nextIntBetweenInclusive(-3, 3);
            int k = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextIntBetweenInclusive(-1, 1);
                if (this.maybeTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return;
                }
            }
        }
    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.moveTo((double)x + (double)0.5F, y, (double)z + (double)0.5F, this.getYRot(), this.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    abstract boolean canTeleportTo(BlockPos pos);

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return super.canSpawnSprintParticle() && this.getDeltaMovement().lengthSqr() > 0.01f;
    }

    // TODO currently just snow-golem-like shooting logic
    /// Status effect applications etc. handled in: {@link com.bonsai.pixelpets.mixin.ProjectileMixin}
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.projectile == null) return;

        Projectile p = this.projectile.create(this.level());
        if (p == null) return;

        p.moveTo(this.getX(), this.getEyeY(), this.getZ());
        p.setOwner(this);

        double d0 = target.getEyeY() - (double)1.1F;
        double d1 = target.getX() - this.getX();
        double d2 = d0 - p.getY();
        double d3 = target.getZ() - this.getZ();
        double d4 = Math.sqrt(d1 * d1 + d3 * d3) * (double)0.2F;
        p.shoot(d1, d2 + d4, d3, 1.6F, 6.0F);

        this.level().addFreshEntity(p);
    }

    @Override
    protected AABB getAttackBoundingBox() {
        if (this.currentAttack == null || this.currentAttack.range() < 0.0f) return super.getAttackBoundingBox();
        return super.getAttackBoundingBox().inflate(this.getRange(), 0.0f, this.getRange());
    }

    // Melee status effect application
    @Override
    public boolean doHurtTarget(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            for (StatusEffectApplication effect : this.getOnHitStatuses()) {
                if (this.getRandom().nextFloat() < effect.chance()) {
                    livingEntity.addEffect(new MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier()));
                }
            }
        }
        triggerAnim("Attack", "attack");
        return super.doHurtTarget(entity);
    }

    @Override
    public void updateSwimming() {
        this.setSwimming(this.isInWater() && !this.isPassenger());
    }

}
