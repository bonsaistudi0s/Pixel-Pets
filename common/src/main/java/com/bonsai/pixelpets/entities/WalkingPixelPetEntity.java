package com.bonsai.pixelpets.entities;

import com.bonsai.pixelpets.entities.goals.DefaultFollowOwnerGoal;
import com.bonsai.pixelpets.entities.goals.GentleFloatGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class WalkingPixelPetEntity extends AbstractPixelPetEntity {
    public WalkingPixelPetEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new GentleFloatGoal(this));
        this.goalSelector.addGoal(6, new DefaultFollowOwnerGoal(this, 1.0f, 6.0f, 1.5f));
        this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1.0f));
    }

    protected boolean canTeleportTo(BlockPos pos) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(this, pos);
        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.level().getBlockState(pos.below());
            if (!this.canFlyToOwner() && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pos.subtract(this.blockPosition());
                return this.level().noCollision(this, this.getBoundingBox().move(blockpos));
            }
        }
    }
}
