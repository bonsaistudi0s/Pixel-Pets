package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsPetRangedCondition() implements TargetCondition {

    public static final MapCodec<IsPetRangedCondition> CODEC = MapCodec.unit(new IsPetRangedCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_PET_RANGED.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        return (entity instanceof AbstractPixelPetEntity pet && pet.getRange() > 0.0f);
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_pet_ranged");
    }

}
