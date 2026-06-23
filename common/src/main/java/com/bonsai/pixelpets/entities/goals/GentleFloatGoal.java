package com.bonsai.pixelpets.entities.goals;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.phys.Vec3;

public class GentleFloatGoal extends FloatGoal {
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
