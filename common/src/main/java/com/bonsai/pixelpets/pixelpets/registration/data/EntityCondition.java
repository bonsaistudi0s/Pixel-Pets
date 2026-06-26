package com.bonsai.pixelpets.pixelpets.registration.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

public record EntityCondition(Type type, double min, double max, boolean invert) {

    public static final Codec<EntityCondition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Type.CODEC.fieldOf("type").forGetter(EntityCondition::type),
            Codec.DOUBLE.optionalFieldOf("min", -Double.MAX_VALUE).forGetter(EntityCondition::min),
            Codec.DOUBLE.optionalFieldOf("max", Double.MAX_VALUE).forGetter(EntityCondition::max),
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(EntityCondition::invert)
    ).apply(i, EntityCondition::new));

    public boolean test(LivingEntity entity) {
        return invert != type.test(entity, min, max);
    }

    public enum Type implements StringRepresentable {
        ALWAYS_TRUE {
            // Should only be used if previous pet levels aren't ALWAYS_TRUE (empty conditions defaults to this)
            @Override
            public boolean test(LivingEntity entity, double min, double max) {
                return true;
            }
        },
        IN_WATER {
            @Override
            public boolean test(LivingEntity entity, double min, double max) {
                return entity.isInWater();
            }
        },
        IS_DAY {
            @Override
            public boolean test(LivingEntity entity, double min, double max) {
                return entity.level().getDayTime() % 24000L < 12000L;
            }
        },
        HEIGHT {
            @Override
            public boolean test(LivingEntity entity, double min, double max) {
                double y = entity.getY();
                return y >= min && y <= max;
            }
        },
        BIOME_TEMPERATURE {
            @Override
            public boolean test(LivingEntity entity, double min, double max) {
                double temp = entity.level().getBiome(entity.blockPosition()).value().getBaseTemperature();
                return temp >= min && temp <= max;
            }
        };

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        public abstract boolean test(LivingEntity entity, double min, double max);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
