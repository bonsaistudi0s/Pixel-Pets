package com.bonsai.pixelpets.entities.goals;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class AvoidPixelPetGoal extends Goal {

    private final Set<ResourceLocation> scaryPets = new HashSet<>();

    protected final PathfinderMob mob;

    @Nullable
    protected AbstractPixelPetEntity toAvoid;

    @Nullable
    protected Path path;

    protected final PathNavigation pathNav;
    
    protected final Predicate<LivingEntity> avoidPredicate = (entity) -> {
        if (entity instanceof AbstractPixelPetEntity pet) {
            return this.scaryPets.contains(pet.getDataLocation());
        }
        return false;
    };
    private final TargetingConditions avoidEntityTargeting;

    public AvoidPixelPetGoal(PathfinderMob mob, Set<ResourceLocation> scaryPets) {
        this.scaryPets.addAll(scaryPets);
        this.mob = mob;
        this.pathNav = mob.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.avoidEntityTargeting = TargetingConditions.forCombat().range(6.0f).selector(avoidPredicate);
    }

    public boolean canUse() {
        this.toAvoid = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(AbstractPixelPetEntity.class, this.mob.getBoundingBox().inflate((double)6.0f, (double)3.0F, (double)6.0f), (livingEntity) -> true), this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
        if (this.toAvoid == null) {
            return false;
        } else {
            Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
            if (vec3 == null) {
                return false;
            } else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob)) {
                return false;
            } else {
                this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                return this.path != null;
            }
        }
    }

    public boolean canContinueToUse() {
        return !this.pathNav.isDone();
    }

    public void start() {
        this.pathNav.moveTo(this.path, 1.0f);
    }

    public void stop() {
        this.toAvoid = null;
    }

    public void tick() {
        if (this.toAvoid != null && this.mob.distanceToSqr(this.toAvoid) < 49.0) {
            this.mob.getNavigation().setSpeedModifier(1.2f);
        } else {
            this.mob.getNavigation().setSpeedModifier(1.0f);
        }
    }
}
