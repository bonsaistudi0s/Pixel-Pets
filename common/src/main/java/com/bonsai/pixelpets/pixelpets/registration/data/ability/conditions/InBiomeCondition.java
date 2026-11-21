package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;

public record InBiomeCondition(HolderSet<Biome> biomes) implements TargetCondition {

    public static final MapCodec<InBiomeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BIOME)
                    .optionalFieldOf("biomes", HolderSet.empty())
                    .forGetter(condition -> condition.biomes)
    ).apply(instance, InBiomeCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.IN_BIOME.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        Holder<Biome> biome = entity.level().getBiome(entity.blockPosition());
        return this.biomes.contains(biome);
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.in_biome");
    }

}
