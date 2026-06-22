package com.bonsai.pixelpets.pixelpets.pixelpetdata;

import net.minecraft.util.StringRepresentable;

public enum PixelPetRarity implements StringRepresentable {
    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    EPIC("epic"),
    LEGENDARY("legendary");

    public static final EnumCodec<PixelPetRarity> CODEC = StringRepresentable.fromEnum(PixelPetRarity::values);
    private final String name;

    PixelPetRarity(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    // Nullable
    public static PixelPetRarity byName(String name) {
        return CODEC.byName(name);
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
