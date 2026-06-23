package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.teamresourceful.resourcefullib.common.registry.HolderRegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;


public class ModAttributes {

    public static final ResourcefulRegistry<Attribute> ATTRIBUTES = ResourcefulRegistries.create(BuiltInRegistries.ATTRIBUTE, PixelPets.MOD_ID);

    public static final HolderRegistryEntry<Attribute> RANGE = ATTRIBUTES.registerHolder("pixelpets.range", () ->
            new RangedAttribute("attribute.name.pixelpets.range", 0.0f, 0.0f, 64.0f));

}
