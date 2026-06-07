package com.bonsai.pixelpets.pixelpets;

import net.minecraft.util.StringRepresentable;

public enum PixelPetStatus implements StringRepresentable {
    RESTING("resting"),
    PASSIVE("passive"),
    ACTIVE("active");

    public static final StringRepresentable.EnumCodec<PixelPetStatus> CODEC = StringRepresentable.fromEnum(PixelPetStatus::values);
    private final String name;

    PixelPetStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    // Nullable
    public static PixelPetStatus byName(String name) {
        return CODEC.byName(name);
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
