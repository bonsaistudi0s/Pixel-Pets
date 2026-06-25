package com.bonsai.pixelpets.entities.goals;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PixelPetRestWhenOrderedToGoal extends Goal {
    private final AbstractPixelPetEntity pet;
    private int timeTillSleep;

    public PixelPetRestWhenOrderedToGoal(AbstractPixelPetEntity pet) {
        this.pet = pet;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity owner = this.pet.getOwner();
        if (owner == null) {
            return this.pet.isOrderedToSit();
        }

        return this.pet.isOrderedToSit() && (owner.distanceToSqr(this.pet) < 196.0) && (owner.getLastHurtByMob() == null);
    }

    @Override
    public boolean canUse() {
        if (!this.pet.isTame()) {
            return false;
        } else if (this.pet.isInWaterOrBubble()) {
            return false;
        } else if (!this.pet.onGround()) {
            return false;
        }
        return this.pet.isOrderedToSit();
    }

    @Override
    public void start() {
        this.pet.getNavigation().stop();
        this.pet.setInSittingPose(true);
        this.pet.setResting(false);
        this.timeTillSleep = this.pet.getRandom().nextInt(200, 400);
    }

    @Override
    public void stop() {
        this.pet.setInSittingPose(false);
        this.pet.setOrderedToSit(false);
        this.pet.setResting(false);
    }

    @Override
    public void tick() {
        if (this.timeTillSleep > 0) {
            if (--this.timeTillSleep <= 0) {
                this.pet.setResting(true);
            }
        }
    }
}
