package com.bonsai.pixelpets.pixelpets.registration.data;

import com.bonsai.pixelpets.PixelPets;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum Rarity implements StringRepresentable {
    COMMON("common", ChatFormatting.WHITE),
    UNCOMMON("uncommon", ChatFormatting.YELLOW),
    RARE("rare", ChatFormatting.AQUA),
    EPIC("epic", ChatFormatting.LIGHT_PURPLE),
    LEGENDARY("legendary", ChatFormatting.GOLD);

    public static final EnumCodec<Rarity> CODEC = StringRepresentable.fromEnum(Rarity::values);
    private final String name;
    private final ChatFormatting color;

    private final ResourceLocation caughtFrameTexture;
    private final ResourceLocation catchableFrameTexture;
    private final ResourceLocation uncatchableFrameTexture;

    Rarity(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
        this.caughtFrameTexture = PixelPets.id("textures/gui/pet_graph/frame/caught_frame_" + name + ".png");
        this.catchableFrameTexture = PixelPets.id("textures/gui/pet_graph/frame/catchable_frame_" + name + ".png");
        this.uncatchableFrameTexture = PixelPets.id("textures/gui/pet_graph/frame/uncatchable_frame_" + name + ".png");
    }

    public String getName() {
        return this.name;
    }
    public ChatFormatting color() {
        return this.color;
    }

    public ResourceLocation getCaughtFrameTexture() {
        return caughtFrameTexture;
    }
    public ResourceLocation getCatchableFrameTexture() {
        return catchableFrameTexture;
    }
    public ResourceLocation getUncatchableFrameTexture() {
        return uncatchableFrameTexture;
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
