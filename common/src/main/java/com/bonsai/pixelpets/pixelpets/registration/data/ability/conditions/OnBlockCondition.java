package com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions;

import com.bonsai.pixelpets.registry.ModTargetConditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record OnBlockCondition(HolderSet<Block> blocks) implements TargetCondition {

    public static final MapCodec<OnBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK)
                    .optionalFieldOf("blocks", HolderSet.empty())
                    .forGetter(condition -> condition.blocks)
    ).apply(instance, OnBlockCondition::new));

    @Override
    public TargetConditionType<?> type() {
        return ModTargetConditions.ON_BLOCK.get();
    }

    @Override
    public boolean test(LivingEntity entity) {
        BlockState blockOn = entity.level().getBlockState(entity.blockPosition());
        return blockOn.is(this.blocks);
    }

    // TODO
    @Override
    public Component describe() {
        return Component.translatable("pixelpets.target_conditions.on_block");
    }

}
