package com.bonsai.pixelpets.entities;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.pixelpets.PixelPetStatus;
import com.bonsai.pixelpets.pixelpets.pixelpetdata.LeveledAttackData;
import com.bonsai.pixelpets.pixelpets.pixelpetdata.PixelPetData;
import com.bonsai.pixelpets.pixelpets.pixelpetdata.PixelPetDataRegistry;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.registry.ModAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
import java.util.function.Predicate;

// TODO Handle name displayed on death
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

    // TODO edit to account for modded creepers?
    public static final Predicate<LivingEntity> TARGET_SELECTOR = (entity) -> {
        EntityType<?> entitytype = entity.getType();
        return entitytype != EntityType.CREEPER && !(entity instanceof NeutralMob);
    };

    private int abilityLevel = 1;
    private PixelPetStatus status = PixelPetStatus.PASSIVE;
    private LeveledAttackData.PixelPetAttackData currentAttack = null;
    private EntityType<Projectile> projectile = null;
    private Goal currentAttackGoal = null;

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
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (getDataLocation() != null) {
            tag.putString("data_location", getDataLocation().toString());
        }
        tag.putInt("ability_level", this.abilityLevel);
        tag.putString("status", this.status.getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("data_location")) {
            setDataLocation(ResourceLocation.parse(tag.getString("data_location")));
        }
        setAbilityLevel(tag.contains("ability_level") ? tag.getInt("ability_level") : 1);
        setStatus(tag.contains("status") ? PixelPetStatus.byName(tag.getString("status")) : PixelPetStatus.PASSIVE);
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

    // TODO might need to make range an Attribute, so other pets can buff it
    private void refreshAttack() {
        LeveledAttackData.PixelPetAttackData resolved = getData().flatMap(data -> data.attack().resolve(this.abilityLevel)).orElse(null);

        // Attribute setting
        if (resolved != null) {
            AttributeInstance damageAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.setBaseValue(resolved.damage());
            }

            // Range affect melee and ranged attacks, but the scale and defaults for those are very different
            // Defaulted to NEG_INF in LeveledAttackData, true default applied here
            AttributeInstance rangeAttr = this.getAttribute(ModAttributes.RANGE);
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

    public List<LeveledAttackData.StatusEffectApplication> getOnHitStatuses() {
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
        AttributeInstance rangeAttr = this.getAttribute(ModAttributes.RANGE);
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

    protected void registerGoals() {
        //this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this)); // TODO custom goal here? idk if sitting should be possible for normal pet use
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                // Replaced later with PixelPetData value
                .add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(ModAttributes.RANGE, 0.0f)         // Controls melee and ranged range, default applies to melee
                .add(Attributes.ATTACK_SPEED, 20)       // # ticks until can attack again

                // Normal
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.7f)
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
                        this.heal(5.0F);
                    }

                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }

                InteractionResult interactionresult = super.mobInteract(player, hand);
                if (!interactionresult.consumesAction()) {
                    //this.setOrderedToSit(!this.isOrderedToSit()); // TODO see sitting comment above
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
            for (LeveledAttackData.StatusEffectApplication effect : this.getOnHitStatuses()) {
                if (this.getRandom().nextFloat() < effect.chance()) {
                    livingEntity.addEffect(new MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier()));
                }
            }
        }
        return super.doHurtTarget(entity);
    }

    /// Attack Goals

    public static class PixelPetMeleeAttackGoal extends Goal {
        protected final AbstractPixelPetEntity mob;
        private Path path;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private int ticksUntilNextPathRecalculation;
        private int ticksUntilNextAttack;
        private long lastCanUseCheck;

        public PixelPetMeleeAttackGoal(AbstractPixelPetEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            long i = this.mob.level().getGameTime();
            if (i - this.lastCanUseCheck < 20L) {
                return false;
            } else {
                this.lastCanUseCheck = i;
                LivingEntity livingentity = this.mob.getTarget();
                if (livingentity == null) {
                    return false;
                } else if (!livingentity.isAlive()) {
                    return false;
                } else {
                    this.path = this.mob.getNavigation().createPath(livingentity, 0);
                    return this.path != null ? true : this.mob.isWithinMeleeAttackRange(livingentity);
                }
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                return !this.mob.isWithinRestriction(livingentity.blockPosition()) ? false : !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
            }
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.path, 1.0);
            this.mob.setAggressive(true);
            this.ticksUntilNextPathRecalculation = 0;
            this.ticksUntilNextAttack = 0;
        }

        public void stop() {
            LivingEntity livingentity = this.mob.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.mob.setTarget(null);
            }

            this.mob.setAggressive(false);
            this.mob.getNavigation().stop();
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity != null) {
                this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
                if (this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == (double)0.0F && this.pathedTargetY == (double)0.0F && this.pathedTargetZ == (double)0.0F || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= (double)1.0F || this.mob.getRandom().nextFloat() < 0.05F)) {
                    this.pathedTargetX = livingentity.getX();
                    this.pathedTargetY = livingentity.getY();
                    this.pathedTargetZ = livingentity.getZ();
                    this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                    double d0 = this.mob.distanceToSqr(livingentity);
                    if (d0 > (double)1024.0F) {
                        this.ticksUntilNextPathRecalculation += 10;
                    } else if (d0 > (double)256.0F) {
                        this.ticksUntilNextPathRecalculation += 5;
                    }

                    if (!this.mob.getNavigation().moveTo(livingentity, 1.0f)) {
                        this.ticksUntilNextPathRecalculation += 15;
                    }

                    this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
                }

                this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                this.checkAndPerformAttack(livingentity);
            }

        }

        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(target);
            }

        }

        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = this.adjustedTickDelay((int) this.mob.getAttackSpeed());
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected boolean canPerformAttack(LivingEntity entity) {
            return this.isTimeToAttack() && this.mob.isWithinMeleeAttackRange(entity) && this.mob.getSensing().hasLineOfSight(entity);
        }
    }

    public static class PixelPetRangedAttackGoal extends Goal {
        private final AbstractPixelPetEntity mob;
        private LivingEntity target;
        private int attackTime;
        private int seeTime;
        public PixelPetRangedAttackGoal(AbstractPixelPetEntity rangedAttackMob) {
            this.attackTime = -1;
            this.mob = rangedAttackMob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                this.target = livingentity;
                return true;
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            return this.canUse() || this.target.isAlive() && !this.mob.getNavigation().isDone();
        }

        public void stop() {
            this.target = null;
            this.seeTime = 0;
            this.attackTime = -1;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            double d0 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
            boolean flag = this.mob.getSensing().hasLineOfSight(this.target);
            if (flag) {
                ++this.seeTime;
            } else {
                this.seeTime = 0;
            }

            if (!(d0 > (double)(this.mob.getRange() * this.mob.getRange())) && this.seeTime >= 5) {
                this.mob.getNavigation().stop();
            } else {
                this.mob.getNavigation().moveTo(this.target, 1.0f);
            }

            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            if (--this.attackTime == 0) {
                if (!flag) {
                    return;
                }

                float f = (float)Math.sqrt(d0) / this.mob.getRange();
                float f1 = Mth.clamp(f, 0.1F, 1.0F);
                this.mob.performRangedAttack(this.target, f1);
            }

            if (this.attackTime <= 0) {
                this.attackTime = (int) this.mob.getAttackSpeed();
            }

        }
    }


    /// Movement Goals

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

    // TODO test with flying
    public static class DefaultFollowOwnerGoal extends Goal {
        private final TamableAnimal tamable;
        private LivingEntity owner;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;

        public DefaultFollowOwnerGoal(TamableAnimal tamable, double speedModifier, float startDistance, float stopDistance) {
            this.tamable = tamable;
            this.speedModifier = speedModifier;
            this.navigation = tamable.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            if (!(this.navigation instanceof GroundPathNavigation)
                    && !(this.navigation instanceof FlyingPathNavigation)
                    && !(this.navigation instanceof AmphibiousPathNavigation)) {
                throw new IllegalArgumentException("Unsupported navigation type for DefaultFollowOwnerGoal");
            }
        }

        public boolean canUse() {
            LivingEntity livingentity = this.tamable.getOwner();
            if (livingentity == null) {
                return false;
            } else if (this.tamable.unableToMoveToOwner()) {
                return false;
            } else if (this.tamable.isInWater()) {
                return false;
            } else if (this.tamable.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return true;
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (this.tamable.isInWater()) {
                return false;
            } else {
                return this.tamable.unableToMoveToOwner() ? false : !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
        }

        public void tick() {
            boolean flag = this.tamable.shouldTryTeleportToOwner();
            if (!flag) {
                this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (flag) {
                    this.tamable.tryToTeleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }
    }

    public static class SwimFollowOwnerGoal extends Goal {
        private final TamableAnimal tamable;
        private LivingEntity owner;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private final float startDistance;

        public SwimFollowOwnerGoal(TamableAnimal tamable, double speedModifier, float startDistance, float stopDistance) {
            this.tamable = tamable;
            this.speedModifier = speedModifier;
            this.navigation = tamable.getNavigation();
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            if (!(this.navigation instanceof WaterBoundPathNavigation)
                    && !(this.navigation instanceof AmphibiousPathNavigation)) {
                throw new IllegalArgumentException("Unsupported navigation type for SwimFollowOwnerGoal");
            }
        }

        public boolean canUse() {
            LivingEntity livingentity = this.tamable.getOwner();
            if (livingentity == null) {
                return false;
            } else if (this.tamable.unableToMoveToOwner()) {
                return false;
            } else if (!this.tamable.isInWater()) {
                return false;
            } else if (this.tamable.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
                return false;
            } else {
                this.owner = livingentity;
                return true;
            }
        }

        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (!this.tamable.isInWater()) {
                return false;
            } else {
                return !this.tamable.unableToMoveToOwner() && !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
            }
        }

        public void start() {
            this.timeToRecalcPath = 0;
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
        }

        public void tick() {
            boolean flag = this.tamable.shouldTryTeleportToOwner();
            if (!flag) {
                this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
            }

            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (flag) {
                    this.tamable.tryToTeleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }
    }
}
