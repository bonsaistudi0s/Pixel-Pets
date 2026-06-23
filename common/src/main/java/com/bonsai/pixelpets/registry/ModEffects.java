package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class ModEffects {

    public static final ResourcefulRegistry<MobEffect> STATUS_EFFECTS = ResourcefulRegistries.create(BuiltInRegistries.MOB_EFFECT, PixelPets.MOD_ID);

    // example:
    // public static final ReferenceRegistryEntry<MobEffect> REACH = STATUS_EFFECTS.registerReference("reach", () -> new ReachStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFE984B));

}
