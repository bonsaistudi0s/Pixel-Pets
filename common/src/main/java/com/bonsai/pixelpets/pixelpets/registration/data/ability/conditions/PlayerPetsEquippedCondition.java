package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public record PlayerPetsEquippedCondition(int min, int max) implements TargetCondition {

    public static final MapCodec<PlayerPetsEquippedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("min", 0).forGetter(condition -> condition.min),
            Codec.INT.optionalFieldOf("max", 4).forGetter(condition -> condition.max)
    ).apply(instance, PlayerPetsEquippedCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.PLAYER_PETS_EQUIPPED.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        if (entity instanceof PlayerPetAccess petAccess) {
            int numPetsEquipped = petAccess.pixelPets$getActivePets().size();
            return numPetsEquipped >= this.min && numPetsEquipped <= this.max;
        }
        return false;
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.player_pets_equipped");
    }

}
