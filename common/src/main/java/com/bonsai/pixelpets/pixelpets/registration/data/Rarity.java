package com.bonsai.pixelpets.pixelpets.registration.data;

import net.minecraft.util.StringRepresentable;

public enum Rarity implements StringRepresentable {
    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    EPIC("epic"),
    LEGENDARY("legendary");

    public static final EnumCodec<Rarity> CODEC = StringRepresentable.fromEnum(Rarity::values);
    private final String name;

    Rarity(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    // Nullable
    public static Rarity byName(String name) {
        return CODEC.byName(name);
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
