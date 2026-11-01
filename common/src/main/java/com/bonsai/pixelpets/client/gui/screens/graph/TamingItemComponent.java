package com.bonsai.pixelpets.client.gui.screens.graph;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class TamingItemComponent {
    private static final int ITEM_CYCLE_TICKS = 15;
    private static final int ITEM_ICON_SIZE = 16;

    private static final ItemStack ERROR_ITEM = new ItemStack(Blocks.BARRIER.asItem());
    private static final List<Component> ERROR_TOOLTIP = List.of(Component.literal("!ERROR!"));

    private int ticks;
    private int itemIndex;

    public void tick() {
        if (++ticks >= ITEM_CYCLE_TICKS) {
            ticks = 0;
            itemIndex++;
        }
    }

    public int render(GuiGraphics g, Font font, Ingredient ingredient,
                      int x, int y, int mouseX, int mouseY) {

        List<ItemStack> items = Arrays.stream(ingredient.getItems())
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();

        boolean showTooltip = mouseX >= x && mouseX < x + ITEM_ICON_SIZE
                && mouseY >= y && mouseY < y + ITEM_ICON_SIZE;

        if (items.isEmpty()) {
            g.renderItem(ERROR_ITEM, x, y);
            if (showTooltip) {
                g.renderTooltip(font, ERROR_TOOLTIP, Optional.empty(), mouseX, mouseY);
            }
            return ITEM_ICON_SIZE;
        }

        ItemStack displayedItem = items.get(itemIndex % items.size());
        g.renderItem(displayedItem, x, y);

        if (showTooltip) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("gui.pixelpets.pet_graph.panel.tame_item.tooltip"));

            for (ItemStack stack : items) {
                tooltip.add(Component.literal(stack.getHoverName().getString())
                        .withStyle(ChatFormatting.GRAY));
            }

            g.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        return ITEM_ICON_SIZE;
    }
}