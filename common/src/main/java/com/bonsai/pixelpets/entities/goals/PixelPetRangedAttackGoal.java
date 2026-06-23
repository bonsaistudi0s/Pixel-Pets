package com.bonsai.pixelpets.entities.goals;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PixelPetRangedAttackGoal extends Goal {
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

        if (!(d0 > (double) (this.mob.getRange() * this.mob.getRange())) && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.target, 1.0f);
        }

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime == 0) {
            if (!flag) {
                return;
            }

            float f = (float) Math.sqrt(d0) / this.mob.getRange();
            float f1 = Mth.clamp(f, 0.1F, 1.0F);
            this.mob.performRangedAttack(this.target, f1);
        }

        if (this.attackTime <= 0) {
            this.attackTime = (int) this.mob.getAttackSpeed();
        }

    }
}
