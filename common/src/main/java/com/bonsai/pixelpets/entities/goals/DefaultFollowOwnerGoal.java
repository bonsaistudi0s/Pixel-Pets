package com.bonsai.pixelpets.entities.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

// TODO test with flying
public class DefaultFollowOwnerGoal extends Goal {
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
        } else if (this.tamable.isUnderWater()) {
            return false;
        } else if (this.tamable.distanceToSqr(livingentity) < (double) (this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = livingentity;
            return true;
        }
    }

    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else if (this.tamable.isUnderWater()) {
            return false;
        } else {
            return this.tamable.unableToMoveToOwner() ? false : !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
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
            this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float) this.tamable.getMaxHeadXRot());
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
