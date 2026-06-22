package com.bonsai.pixelpets.pixelpets;

import com.bonsai.pixelpets.PixelPets;
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

    public static PixelPetStatus byName(String name) {
        PixelPetStatus result = CODEC.byName(name);
        if (result == null) {
            PixelPets.LOGGER.warn("Unknown PixelPetStatus '{}', defaulting to PASSIVE", name);
            return PixelPetStatus.PASSIVE;
        }
        return result;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
