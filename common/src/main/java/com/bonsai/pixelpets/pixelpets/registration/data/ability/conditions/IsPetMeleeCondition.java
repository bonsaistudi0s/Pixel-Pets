package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record IsPetMeleeCondition() implements TargetCondition {

    public static final MapCodec<IsPetMeleeCondition> CODEC = MapCodec.unit(new IsPetMeleeCondition());

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_PET_MELEE.get();
    }

    // TODO test
    @Override
    public boolean test(LivingEntity entity) {
        return (entity instanceof AbstractPixelPetEntity pet && pet.getRange() <= 0.0f);
    }

    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_pet_melee");
    }

}
