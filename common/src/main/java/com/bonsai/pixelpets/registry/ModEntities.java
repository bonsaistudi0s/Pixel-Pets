package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.entities.AmphibiousPixelPetEntity;
import com.bonsai.pixelpets.entities.SwimmingPixelPetEntity;
import com.bonsai.pixelpets.entities.WalkingPixelPetEntity;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class ModEntities {

    public static final ResourcefulRegistry<EntityType<?>> ENTITY_TYPES = ResourcefulRegistries.create(BuiltInRegistries.ENTITY_TYPE, PixelPets.MOD_ID);

    public static final Supplier<EntityType<AbstractPixelPetEntity>> WALKING_PET = ENTITY_TYPES.register("walking_pet",
            () -> registerPet("walking_pet", WalkingPixelPetEntity::new));

    public static final Supplier<EntityType<AbstractPixelPetEntity>> SWIMMING_PET = ENTITY_TYPES.register("swimming_pet",
            () -> registerPet("swimming_pet", SwimmingPixelPetEntity::new));

    public static final Supplier<EntityType<AbstractPixelPetEntity>> AMPHIBIOUS_PET = ENTITY_TYPES.register("amphibious_pet",
            () -> registerPet("amphibious_pet", AmphibiousPixelPetEntity::new));



    public static <T extends Entity> EntityType<T> registerPet(String name, EntityType.EntityFactory<T> factory) {
        return registerGeneric(name, factory, 0.4f, 0.4f, 0.25f);
    }

    public static <T extends Entity> EntityType<T> registerGeneric(String name, EntityType.EntityFactory<T> factory, float width, float height, float eyeHeight) {
        return EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(width, height)
                .eyeHeight(eyeHeight)
                .clientTrackingRange(10)
                .build(name);
    }

    public static <T extends Entity> EntityType<T> registerProjectile(String name, EntityType.EntityFactory<T> factory) {
        return EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(10)
                .build(name);
    }
}
