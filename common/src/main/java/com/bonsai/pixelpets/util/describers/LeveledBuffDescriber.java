package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.LeveledBuffData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.function.Function;

public class LeveledBuffDescriber extends LeveledDescriber<LeveledBuffData> {

    private final StatusEffectDescriber statusEffectDescriber;
    private final AttributeModifierDescriber attributeModifierDescriber;
    private final BuffTargetDescriber buffTargetDescriber;
    private final TargetConditionDescriber targetConditionDescriber;

    public LeveledBuffDescriber()
    {
        this.statusEffectDescriber = new StatusEffectDescriber();
        this.attributeModifierDescriber = new AttributeModifierDescriber();
        this.buffTargetDescriber = new BuffTargetDescriber();
        this.targetConditionDescriber = new TargetConditionDescriber();
    }

    @Override
    protected void generateDescriptions(LeveledBuffData data) {
        LeveledBuffData.PixelPetBuffData previous = null;

        for (int level = 1; level <= LEVEL_COUNT; level++) {
            LeveledBuffData.PixelPetBuffData resolved = data.resolve(level);

            if (resolved.buffs().isEmpty()) {
                setLevelDescription(level, Component.empty());
                previous = null;
                continue;
            }

            Component description;

            if (resolved.descriptionOverride().isPresent()) {
                description = Component.translatable(resolved.descriptionOverride().get());
            } else {
                description = previous == null
                        ? describeInitialBuffs(resolved)
                        : describeChanges(previous, resolved);
            }

            setLevelDescription(level, description);
            previous = resolved;
        }
    }

    private Component describeInitialBuffs(LeveledBuffData.PixelPetBuffData buffs) {
        List<Component> buffLines = new ArrayList<>();
        for (LeveledBuffData.BuffData buff : buffs.buffs()) {
            buffLines.add(describeBuff(buff));
        }

        return ObjectDescriber.multiLine(buffLines);
    }

    private Component describeChanges(
            LeveledBuffData.PixelPetBuffData previous,
            LeveledBuffData.PixelPetBuffData current)
    {
        Map<String, LeveledBuffData.BuffData> previousBuffs = new LinkedHashMap<>();
        for (LeveledBuffData.BuffData buff : previous.buffs()) {
            previousBuffs.put(buff.id(), buff);
        }

        List<Component> added = new ArrayList<>();
        List<Component> modified = new ArrayList<>();

        for (LeveledBuffData.BuffData buff : current.buffs()) {
            LeveledBuffData.BuffData old = previousBuffs.get(buff.id());

            if (old == null) {
                added.add(describeBuff(buff));
                continue;
            }

            Component modification = describeBuffChanges(old, buff);

            if (!modification.equals(Component.empty())) {
                modified.add(Component.literal(buff.id() + ": ").append(modification));
            }
        }

        added.addAll(modified);

        return ObjectDescriber.multiLine(added);
    }

    private Component describeBuff(LeveledBuffData.BuffData buff) {
        List<Component> statusEffects = new ArrayList<>();
        buff.statusEffects().stream()
                .map(statusEffectDescriber::describe)
                .forEach(statusEffects::add);

        List<Component> attributeModifiers = new ArrayList<>();
        buff.attributeModifiers().stream()
                .map(attributeModifierDescriber::describe)
                .forEach(attributeModifiers::add);

        MutableComponent description = Component.translatable(buff.id() + ": ");

        description.append(Component.literal( "Every " + ObjectDescriber.formatDuration(buff.cooldown()) + " tries to apply "));
        statusEffects.addAll(attributeModifiers);
        description.append(statusEffects.isEmpty()
                ? Component.literal("nothing")
                : ObjectDescriber.joinClauses(statusEffects));

        description.append(Component.literal(" onto ")).append(buffTargetDescriber.describeTargets(buff.target()));

        if (!buff.conditions().isEmpty()) {
            List<Component> conditions = new ArrayList<>();
            buff.conditions().forEach(targetCondition -> conditions.add(targetCondition.describe()));
            description.append(Component.literal(" while "));
            description.append(ObjectDescriber.joinClauses(conditions));
        }

        return description;
    }

    private Component describeBuffChanges(
            LeveledBuffData.BuffData previous,
            LeveledBuffData.BuffData current)
    {
        List<Component> changes = new ArrayList<>();

        // Cooldown changes
        if (previous.cooldown() != current.cooldown()) {
            changes.add(ObjectDescriber.numberChange("cooldown", previous.cooldown(), current.cooldown(), ObjectDescriber::formatDuration, ""));
        }

        // Target changes
        if (!previous.target().equals(current.target())) {
            changes.addAll(buffTargetDescriber.describeModification(
                    previous.target(), current.target()
            ));
        }

        // Condition changes
        List<Component> conditionChanges = new ArrayList<>();
        ChangingObjectDescriber.describeObjectChanges(
                targetConditionDescriber,
                targetConditionDescriber::describe,
                previous.conditions(),
                current.conditions(),
                "requires"
        ).ifPresent(conditionChanges::add);

        // Status effect changes
        List<Component> effectChanges = new ArrayList<>();
        ChangingObjectDescriber.describeObjectChanges(
                statusEffectDescriber,
                statusEffectDescriber::describeSimply,
                previous.statusEffects(),
                current.statusEffects(),
                "applies"
        ).ifPresent(effectChanges::add);

        // Attribute modifier changes
        ChangingObjectDescriber.describeObjectChanges(
                attributeModifierDescriber,
                attributeModifierDescriber::describeSimply,
                previous.attributeModifiers(),
                current.attributeModifiers(),
                "applies"
        ).ifPresent(effectChanges::add);

        MutableComponent result = ObjectDescriber.capitalizeFirst(ObjectDescriber.joinClauses(changes));
        if (!conditionChanges.isEmpty()) {
            result.append("\n");
            result.append(ObjectDescriber.multiLine(conditionChanges));
        }
        if (!effectChanges.isEmpty()) {
            result.append("\n");
            result.append(ObjectDescriber.multiLine(effectChanges));
        }
        return result;
    }


}
