package com.bonsai.pixelpets.pixelpets.registration.data;

import net.minecraft.util.StringRepresentable;

public enum PixelPetType implements StringRepresentable {
    MYTHICAL("mythical"),
    FLYING("flying"),
    ARCTIC("arctic"),
    LAND("ocean"),
    AQUATIC("overworld");

    public static final StringRepresentable.EnumCodec<PixelPetType> CODEC = StringRepresentable.fromEnum(PixelPetType::values);
    private final String name;

    PixelPetType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    // Nullable
    public static PixelPetType byName(String name) {
        return CODEC.byName(name);
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
