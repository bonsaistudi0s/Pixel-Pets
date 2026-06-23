package com.bonsai.pixelpets.entities.goals;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;

public class AmphibiousStrollGoal extends RandomStrollGoal {

    public AmphibiousStrollGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier, 40);
    }

    protected Vec3 getPosition() {
        if (this.mob.isInWater()) {
            return BehaviorUtils.getRandomSwimmablePos(this.mob, 10, 7);
        }
        return super.getPosition();
    }
}
