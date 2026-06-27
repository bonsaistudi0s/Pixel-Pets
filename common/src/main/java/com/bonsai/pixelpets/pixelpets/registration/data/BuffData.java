package com.bonsai.pixelpets.pixelpets.registration.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public record BuffData(
        BuffTarget target,
        List<EntityCondition> conditions,
        List<StatusEffectApplication> statusEffects,
        List<AttributeModifierApplication> attributeModifiers
) {

    private static final BuffTarget DEFAULT_TARGET = new BuffTarget(true, false, false);

    public static final Codec<BuffData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuffTarget.CODEC.optionalFieldOf("targets", DEFAULT_TARGET).forGetter(BuffData::target),
            EntityCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(BuffData::conditions),
            StatusEffectApplication.CODEC.listOf().optionalFieldOf("statusEffects", List.of()).forGetter(BuffData::statusEffects),
            AttributeModifierApplication.CODEC.listOf().optionalFieldOf("attributeModifiers", List.of()).forGetter(BuffData::attributeModifiers)
    ).apply(instance, BuffData::new));

    public boolean test(LivingEntity livingEntity) {
        if (this.conditions.isEmpty()) return true;
        return this.conditions.stream().allMatch((condition) -> condition.test(livingEntity));
    }

    // TODO should probably store a flag of if added already, to avoid trying to reapply frequently
    public void applyAttributeModifiers(LivingEntity livingEntity) {
        attributeModifiers.forEach((attributeModifierApplication -> attributeModifierApplication.apply(livingEntity)));
    }

    public void removeAttributeModifiers(LivingEntity livingEntity) {
        attributeModifiers.forEach((attributeModifierApplication -> attributeModifierApplication.remove(livingEntity)));
    }

}
