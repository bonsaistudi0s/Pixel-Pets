package com.bonsai.pixelpets.entities;

import com.bonsai.pixelpets.entities.goals.AmphibiousFollowOwnerGoal;
import com.bonsai.pixelpets.entities.goals.AmphibiousStrollGoal;
import com.bonsai.pixelpets.entities.goals.DefaultFollowOwnerGoal;
import com.bonsai.pixelpets.entities.goals.SwimFollowOwnerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class AmphibiousPixelPetEntity extends AbstractPixelPetEntity{
    public AmphibiousPixelPetEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.moveControl = new AmphibiousMoveControl(this, 85, 10, 1.5F, true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(6, new AmphibiousFollowOwnerGoal(this, 1.0f, 1.2f, 6.0f, 1.5f));
        this.goalSelector.addGoal(10, new AmphibiousStrollGoal(this, 1.0f));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source) || source.is(DamageTypes.DROWN);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.8f;
    }

    @Override
    protected boolean canTeleportTo(BlockPos pos) {
        boolean validGround = WalkNodeEvaluator.getPathTypeStatic(this, pos) == PathType.WALKABLE;
        boolean validWater = this.level().getFluidState(pos).is(FluidTags.WATER);
        if (!validGround && !validWater) {
            return false;
        }
        if (validGround) {
            BlockState blockstate = this.level().getBlockState(pos.below());
            if (!this.canFlyToOwner() && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            }
        }
        BlockPos blockpos = pos.subtract(this.blockPosition());
        return this.level().noCollision(this, this.getBoundingBox().move(blockpos));
    }

    public static class AmphibiousMoveControl extends MoveControl {
        private final int maxTurnX;
        private final int maxTurnY;
        private final float inWaterSpeedModifier;
        private final boolean applyGravity;

        public AmphibiousMoveControl(Mob mob, int maxTurnX, int maxTurnY, float inWaterSpeedModifier, boolean applyGravity) {
            super(mob);
            this.maxTurnX = maxTurnX;
            this.maxTurnY = maxTurnY;
            this.inWaterSpeedModifier = inWaterSpeedModifier;
            this.applyGravity = applyGravity;
        }

        @Override
        public void tick() {
            if (!this.mob.isInWater()) {
                super.tick();
                return;
            }

            if (this.applyGravity) {
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.005, 0.0));
            }

            if (this.operation == Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
                double d0 = this.wantedX - this.mob.getX();
                double d1 = this.wantedY - this.mob.getY();
                double d2 = this.wantedZ - this.mob.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d3 < 2.5000003E-7F) {
                    this.mob.setZza(0.0F);
                } else {
                    float f = (float)(Mth.atan2(d2, d0) * (double)180.0F / (double)(float)Math.PI) - 90.0F;
                    this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, (float)this.maxTurnY));
                    this.mob.yBodyRot = this.mob.getYRot();
                    this.mob.yHeadRot = this.mob.getYRot();
                    float f1 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    this.mob.setSpeed(f1 * this.inWaterSpeedModifier);
                    double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                    if (Math.abs(d1) > (double)1.0E-5F || Math.abs(d4) > (double)1.0E-5F) {
                        float f3 = -((float)(Mth.atan2(d1, d4) * (double)180.0F / (double)(float)Math.PI));
                        f3 = Mth.clamp(Mth.wrapDegrees(f3), (float)(-this.maxTurnX), (float)this.maxTurnX);
                        this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f3, 5.0F));
                    }

                    float f6 = Mth.cos(this.mob.getXRot() * ((float)Math.PI / 180F));
                    float f4 = Mth.sin(this.mob.getXRot() * ((float)Math.PI / 180F));
                    this.mob.zza = f6 * f1;
                    this.mob.yya = -f4 * f1;
                }
            } else {
                this.mob.setSpeed(0.0F);
                this.mob.setXxa(0.0F);
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
            }
        }
    }

}
