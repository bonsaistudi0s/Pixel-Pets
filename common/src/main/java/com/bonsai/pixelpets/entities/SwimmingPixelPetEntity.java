package com.bonsai.pixelpets.entities;

import com.bonsai.pixelpets.entities.goals.SwimFollowOwnerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

// TODO
public class SwimmingPixelPetEntity extends AbstractPixelPetEntity{
    public SwimmingPixelPetEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 2.0F, 0.0F, true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(6, new SwimFollowOwnerGoal(this, 1.0f, 6.0f, 1.5f));
        this.goalSelector.addGoal(10, new RandomSwimmingGoal(this, 1.0f, 40));
    }

    @Override
    boolean canTeleportTo(BlockPos pos) {
        if (!this.level().getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        } else {
            BlockPos blockpos = pos.subtract(this.blockPosition());
            return this.level().noCollision(this, this.getBoundingBox().move(blockpos));
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    protected void handleAirSupply(int airSupply) {
        if (this.isAlive() && !this.isInWaterOrBubble()) {
            this.setAirSupply(airSupply - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(this.damageSources().drown(), 2.0F);
            }
        } else {
            this.setAirSupply(300);
        }

    }

    public void baseTick() {
        int i = this.getAirSupply();
        super.baseTick();
        this.handleAirSupply(i);
    }

    public boolean isPushedByFluid() {
        return false;
    }
}
