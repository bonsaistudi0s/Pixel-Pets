package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.data.PixelPetRarity;
import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;

public record IsPetRarityCondition(Set<PixelPetRarity> rarities) implements TargetCondition {

    public IsPetRarityCondition(Set<PixelPetRarity> rarities) {
        this.rarities = Set.copyOf(rarities);
    }

    public static final MapCodec<IsPetRarityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PixelPetRarity.CODEC.listOf()
                    .xmap(Set::copyOf, List::copyOf)
                    .optionalFieldOf("rarities", Set.of(PixelPetRarity.values()))
                    .forGetter(condition -> condition.rarities)
    ).apply(instance, IsPetRarityCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_PET_RARITY.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        if (entity instanceof AbstractPixelPetEntity pet && pet.getData().isPresent()) {
            return this.rarities.contains(pet.getData().get().rarity());
        }
        return false;
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_pet_rarity");
    }

}
