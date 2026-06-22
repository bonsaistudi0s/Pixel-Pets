package com.bonsai.pixelpets.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

// TODO
public class FlyingPixelPetEntity extends AbstractPixelPetEntity{
    public FlyingPixelPetEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    boolean canTeleportTo(BlockPos pos) {
        return false;
    }


}
