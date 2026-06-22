package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.BiConsumer;

public class ModAttributes {

    public static void init() {}

    public static final RegistryHelper.HolderProxy<Attribute> RANGE = RegistryHelper.holderProxy(
            Registries.ATTRIBUTE, PixelPets.identifier("pixelpets.range"),
            new RangedAttribute("attribute.name.pixelpets.range", 0.0f, 0.0f, 64.0f));

//    public static void registerAttributes(Registry<Attribute> registry) {
//        RANGE.register(registry);
//    }

    public static void registerAttributes(BiConsumer<Attribute, ResourceLocation> consumer) {
        consumer.accept(RANGE.value(), RANGE.key().location());
    }

}
