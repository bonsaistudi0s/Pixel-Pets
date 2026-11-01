package com.bonsai.pixelpets.util.describers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.Objects;

public abstract class LeveledDescriber<T> implements ObjectDescriber<T> {

    public static final int LEVEL_COUNT = 5;

    private final Component[] descriptions = new Component[LEVEL_COUNT];

    protected LeveledDescriber() {
        clearDescriptions();
    }

    // Isn't really useful
    @Override
    public Component describe(T value) {
        MutableComponent descriptionCollected = Component.empty();
        Arrays.stream(this.descriptions).forEach(component -> {
            descriptionCollected.append(component).append(Component.literal("\n"));
        });
        descriptionCollected.getSiblings().removeLast();
        return descriptionCollected;
    }

    public final void generate(T data) {
        clearDescriptions();
        generateDescriptions(Objects.requireNonNull(data));
    }

    protected abstract void generateDescriptions(T data);

    protected final void setLevelDescription(int level, Component description) {
        if (level < 1 || level > LEVEL_COUNT) {
            throw new IllegalArgumentException(
                    "Level must be between 1 and " + LEVEL_COUNT + ": " + level
            );
        }

        this.descriptions[level - 1] = Objects.requireNonNull(description);
    }

    public final Component getLevelDescription(int level) {
        int clampedLevel = Math.max(1, Math.min(LEVEL_COUNT, level));
        return this.descriptions[clampedLevel - 1];
    }

    public final Component[] getLevelDescriptions() {
        return this.descriptions;
    }

    private void clearDescriptions() {
        Arrays.fill(this.descriptions, Component.empty());
    }
}