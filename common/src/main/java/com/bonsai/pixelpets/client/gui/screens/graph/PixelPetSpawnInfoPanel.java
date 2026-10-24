package com.bonsai.pixelpets.client.gui.screens.graph;

import com.bonsai.pixelpets.client.PixelPetScreenRenderingUtil;
import com.bonsai.pixelpets.pixelpets.registration.PixelPetData;
import com.bonsai.pixelpets.pixelpets.registration.data.Rarity;
import com.bonsai.pixelpets.pixelpets.registration.data.spawning.GenericIntRange;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class PixelPetSpawnInfoPanel {
    private static final int PANEL_WIDTH = 128;
    private static final int PANEL_HEIGHT = 200;
    private static final int PADDING = 6;
    private static final int ROW_HEIGHT = 14;

    // TODO replace with textures
    private static final int BACKGROUND = 0xF0101010;
    private static final int BORDER = 0xFF777777;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int HOVER = 0xFF555555;

    private static final int CLOSE_SIZE = 16;

    private final Runnable onClose;
    private ResourceLocation petId;
    private int x;
    private int y;
    private int width;
    private int height;
    private int closeX;
    private int closeY;

    public PixelPetSpawnInfoPanel(ResourceLocation petId, Runnable onClose) {
        this.petId = petId;
        this.onClose = onClose;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public ResourceLocation petId() {
        return petId;
    }

    public void setPetId(ResourceLocation petId) {
        this.petId = petId;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }

        if (button == 0 && mouseX >= closeX && mouseX < closeX + CLOSE_SIZE && mouseY >= closeY && mouseY < closeY + CLOSE_SIZE) {
            onClose.run();
        }

        return true;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        width = Math.min(PANEL_WIDTH, screenWidth - 2 * PADDING);
        height = Math.min(PANEL_HEIGHT, screenHeight - 2 * PADDING);
        x = screenWidth - PADDING - width;
        y = (screenHeight - height) / 2;

        closeX = x + width - PADDING - CLOSE_SIZE;
        closeY = y + PADDING - 2;

        @Nullable Component hoveredTooltip = null;

        // Panel background and border
        g.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER);
        g.fill(x, y, x + width, y + height, BACKGROUND);

        Registry<PixelPetData> registry = PixelPetScreenRenderingUtil.getPetRegistry();
        PixelPetData data = registry != null ? registry.get(petId) : null;

        String name = data != null ? data.genericName() : petId.toString();
        Rarity rarity = data != null ? data.rarity() : PixelPetData.DEFAULT_RARITY;

        // Title
        g.drawString(Minecraft.getInstance().font, Component.literal(name).withStyle(rarity.color()).withStyle(ChatFormatting.BOLD), x + PADDING, y + PADDING, TEXT);

        // Close button
        boolean closeHovered = mouseX >= closeX && mouseX < closeX + CLOSE_SIZE && mouseY >= closeY && mouseY < closeY + CLOSE_SIZE;
        if (closeHovered) {
            g.fill(closeX, closeY, closeX + CLOSE_SIZE, closeY + CLOSE_SIZE, HOVER);
            hoveredTooltip = Component.translatable("gui.pixelpets.pet_graph.panel.close");
        }
        g.drawCenteredString(Minecraft.getInstance().font, Component.literal("×"), closeX + CLOSE_SIZE / 2, closeY + 3, TEXT);

        Font font = Minecraft.getInstance().font;
        int textX = x + PADDING;
        int rowY = y + PADDING + 22;

        // Actual data

        g.drawString(font, Component.translatable("gui.pixelpets.pet_graph.panel.rarity").append(Component.literal(rarity.name()).withStyle(rarity.color())), textX, rowY, TEXT);
        rowY += ROW_HEIGHT;

        Ingredient ingredient = data != null ? data.tameItem() : PixelPetData.DEFAULT_TAME_ITEM;
        rowY += TamingItemComponent.render(g, font, ingredient, textX, rowY, mouseX, mouseY) + 6;

        int tamingChance = data != null ? data.tameChance() : PixelPetData.DEFAULT_TAME_CHANCE;
        g.drawString(font, Component.translatable("gui.pixelpets.pet_graph.panel.tame_chance").append(Component.literal(tamingChance + "%")), textX, rowY, TEXT);
        rowY += ROW_HEIGHT;

        Optional<GenericIntRange> moonPhase = data != null ? data.spawnData().moonPhase() : Optional.empty();
        MoonPhaseComponent.render(g, font, moonPhase, textX, rowY, 12, 2, mouseX, mouseY);

        if (hoveredTooltip != null) {
            g.renderTooltip(font, hoveredTooltip, mouseX, mouseY);
        }
    }

    public int left(int screenWidth) {
        int panelWidth = Math.min(PANEL_WIDTH, screenWidth - 2 * PADDING);
        return screenWidth - PADDING - panelWidth;
    }
}

