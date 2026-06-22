package com.bonsai.pixelpets.registry;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.entities.AmphibiousPixelPetEntity;
import com.bonsai.pixelpets.entities.SwimmingPixelPetEntity;
import com.bonsai.pixelpets.entities.WalkingPixelPetEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ModEntities {
    public static final LinkedHashMap<String, EntityType<?>> REGISTERED_ENTITIES = new LinkedHashMap<>();

    public static final EntityType<AbstractPixelPetEntity> WALKING_PET = registerPet("walking_pet", WalkingPixelPetEntity::new);
    public static final EntityType<AbstractPixelPetEntity> SWIMMING_PET = registerPet("swimming_pet", SwimmingPixelPetEntity::new);
    public static final EntityType<AbstractPixelPetEntity> AMPHIBIOUS_PET = registerPet("amphibious_pet", AmphibiousPixelPetEntity::new);

    public static <T extends Entity> EntityType<T> registerPet(String name, EntityType.EntityFactory<T> factory) {
        return registerGeneric(name, factory, 0.4f, 0.4f, 0.25f);
    }

    public static <T extends Entity> EntityType<T> registerGeneric(String name, EntityType.EntityFactory<T> factory, float width, float height, float eyeHeight) {
        var entity = EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(width, height)
                .eyeHeight(eyeHeight)
                .clientTrackingRange(10)
                .build(name);
        REGISTERED_ENTITIES.put(name, entity);
        return entity;
    }

    public static <T extends Entity> EntityType<T> registerProjectile(String name, EntityType.EntityFactory<T> factory) {
        var entity = EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(10)
                .build(name);
        REGISTERED_ENTITIES.put(name, entity);
        return entity;
    }

    /// BINDER
    public static void register(BiConsumer<EntityType<?>, ResourceLocation> consumer) {
        REGISTERED_ENTITIES.forEach((key, value) -> consumer.accept(value, PixelPets.identifier(key)));
    }
}
