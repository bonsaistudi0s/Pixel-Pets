package com.bonsai.pixelpets.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

// TODO
// TODO ideas:
//     swim in the air
//     try to fly to deterministic point relative to player, indexed on equipped
public class FlyingPixelPetEntity extends AbstractPixelPetEntity{
    public FlyingPixelPetEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    boolean canTeleportTo(BlockPos pos) {
        return false;
    }


}
