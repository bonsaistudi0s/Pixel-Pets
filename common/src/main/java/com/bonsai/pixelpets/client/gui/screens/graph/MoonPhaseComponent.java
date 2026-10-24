package com.bonsai.pixelpets.client.gui.screens.graph;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.pixelpets.registration.data.spawning.GenericIntRange;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class MoonPhaseComponent {

    private static final ResourceLocation MOON_PHASES = PixelPets.id("textures/gui/pet_graph/moon_phases.png");

    private static final Component[] MOON_PHASE_NAMES = {
            Component.translatable("gui.pixelpets.pet_graph.moon.full_moon"),
            Component.translatable("gui.pixelpets.pet_graph.moon.waning_gibbous"),
            Component.translatable("gui.pixelpets.pet_graph.moon.third_quarter"),
            Component.translatable("gui.pixelpets.pet_graph.moon.waning_crescent"),
            Component.translatable("gui.pixelpets.pet_graph.moon.new_moon"),
            Component.translatable("gui.pixelpets.pet_graph.moon.waxing_crescent"),
            Component.translatable("gui.pixelpets.pet_graph.moon.first_quarter"),
            Component.translatable("gui.pixelpets.pet_graph.moon.waxing_gibbous")
    };

    public static void render(GuiGraphics g, Font font, Optional<GenericIntRange> phases, int x, int y, int size, int padding, double mouseX, double mouseY) {
        int rendered = 0;

        for (int i = 0; i < MOON_PHASE_NAMES.length; i++) {
            if (phases.isPresent() && !phases.get().test(i)) {
                continue;
            }

            int iconX = x + rendered * (size + padding);
            g.blit(MOON_PHASES, iconX, y, i * size, 0, size, size, size * 8, size);
            if (mouseX >= iconX && mouseX < iconX + size && mouseY >= y && mouseY < y + size) {
                g.renderTooltip(font, MOON_PHASE_NAMES[i], (int) mouseX, (int) mouseY);
            }
            rendered++;
        }
    }
}
