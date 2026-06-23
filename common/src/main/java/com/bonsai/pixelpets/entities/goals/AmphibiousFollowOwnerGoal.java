package com.bonsai.pixelpets.entities.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

public class AmphibiousFollowOwnerGoal extends Goal {
    private final TamableAnimal tamable;
    private LivingEntity owner;
    private final double landSpeed;
    private final double swimSpeed;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;

    public AmphibiousFollowOwnerGoal(TamableAnimal tamable, double landSpeed, double swimSpeed, float startDistance, float stopDistance) {
        this.tamable = tamable;
        this.landSpeed = landSpeed;
        this.swimSpeed = swimSpeed;
        this.navigation = tamable.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.tamable.getOwner();
        if (livingentity == null) return false;
        if (this.tamable.unableToMoveToOwner()) return false;
        if (this.tamable.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) return false;
        this.owner = livingentity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) return false;
        if (this.tamable.unableToMoveToOwner()) return false;
        return !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
    }

    @Override
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
                double speed = this.tamable.isInWater() ? this.swimSpeed : this.landSpeed;
                this.navigation.moveTo(this.owner, speed);
            }
        }
    }
}
