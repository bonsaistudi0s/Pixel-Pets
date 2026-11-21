package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.pixelpets.registration.data.PixelPetType;
import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Set;

public record IsPetTypeCondition(Set<PixelPetType> types, boolean all) implements TargetCondition {

    public IsPetTypeCondition(Set<PixelPetType> types, boolean all) {
        this.types = Set.copyOf(types);
        this.all = all;
    }

    public static final MapCodec<IsPetTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PixelPetType.CODEC.listOf()
                    .xmap(Set::copyOf, List::copyOf)
                    .optionalFieldOf("types", Set.of(PixelPetType.values()))
                    .forGetter(condition -> condition.types),
            Codec.BOOL
                    .optionalFieldOf("all", true)
                    .forGetter(condition -> condition.all)
    ).apply(instance, IsPetTypeCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IS_PET_TYPE.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        if (entity instanceof AbstractPixelPetEntity pet && pet.getData().isPresent()) {
            if (this.all) {
                return this.types.containsAll(pet.getData().get().typings());
            } else {
                return pet.getData().get().typings().stream().anyMatch(this.types::contains);
            }
        }
        return false;
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.is_pet_type");
    }

}
