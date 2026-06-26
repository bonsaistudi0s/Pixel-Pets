package com.bonsai.pixelpets.pixelpets.registration.data;

import com.bonsai.pixelpets.pixelpets.PixelPetStatus;
import net.minecraft.util.StringRepresentable;

public enum Typing implements StringRepresentable {
    MYTHICAL("mythical"),
    SAVANNA("savanna"),
    ARCTIC("arctic"),
    OCEAN("ocean"),
    OVERWORLD("overworld"),
    OTHER("other");

    public static final StringRepresentable.EnumCodec<PixelPetStatus> CODEC = StringRepresentable.fromEnum(PixelPetStatus::values);
    private final String name;

    Typing(String name) {
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
