package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModComponents {

    public static final ResourcefulRegistry<DataComponentType<?>> DATA_COMPONENTS = ResourcefulRegistries.create(BuiltInRegistries.DATA_COMPONENT_TYPE, PixelPets.MOD_ID);

}
