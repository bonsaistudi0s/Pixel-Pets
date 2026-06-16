package com.bonsai.pixelpets.pixelpets;

import net.minecraft.util.StringRepresentable;

public enum PixelPetTyping implements StringRepresentable {
    MYTHICAL("mythical"),
    SAVANNA("savanna"),
    ARCTIC("arctic"),
    OCEAN("ocean"),
    OVERWORLD("overworld"),
    OTHER("other");

    public static final StringRepresentable.EnumCodec<PixelPetStatus> CODEC = StringRepresentable.fromEnum(PixelPetStatus::values);
    private final String name;

    PixelPetTyping(String name) {
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
