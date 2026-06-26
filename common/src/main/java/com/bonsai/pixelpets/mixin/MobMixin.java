package com.bonsai.pixelpets.mixin;

import com.bonsai.pixelpets.entities.goals.AvoidPixelPetGoal;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetDataRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Mob.class)
public class MobMixin {

    @Final
    @Shadow
    protected GoalSelector goalSelector;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addPixelPetScareGoal(EntityType entityType, Level level, CallbackInfo ci) {
        if (level.isClientSide()) return;

        // AvoidPixelPetGoal is very similar to vanilla, thus only works on PathfinderMob
        if ((Object) this instanceof PathfinderMob pathfinderMob) {
            Set<ResourceLocation> scaryPets = PixelPetDataRegistry.INSTANCE.getScaryPets(pathfinderMob.getType());
            if (!scaryPets.isEmpty()) {
                //PixelPets.LOGGER.info("EntityType: " + self.getType());
                //PixelPets.LOGGER.info("Scared of: " + scaryPets);
                this.goalSelector.addGoal(3, new AvoidPixelPetGoal(pathfinderMob, scaryPets));
            }
        }
    }

}
