package com.bonsai.pixelpets.pixelpets.registration.data.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

// Pre 1.21.2 attributes have different names: https://minecraft.wiki/w/Attribute?oldid=2663201
// Or https://mcsrc.dev/2/1.21.1/net/minecraft/world/entity/ai/attributes/Attributes
public record AttributeModifierApplication(Holder<Attribute> attribute, AttributeModifier modifier) {

    public static final Codec<AttributeModifierApplication> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeModifierApplication::attribute),
            AttributeModifier.CODEC.fieldOf("modifier").forGetter(AttributeModifierApplication::modifier)
    ).apply(instance, AttributeModifierApplication::new));

    public void apply(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(this.attribute());
        if (instance != null && !instance.hasModifier(this.modifier().id())) {
            instance.addTransientModifier(this.modifier());
        }
    }

    public void remove(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(this.attribute());
        if (instance != null) {
            instance.removeModifier(this.modifier().id());
        }
    }

}
