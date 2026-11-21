package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.LeveledAttackData;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.StatusEffectApplication;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

public class LeveledAttackDescriber extends LeveledDescriber<LeveledAttackData> {

    private final StatusEffectDescriber statusEffectDescriber;

    public LeveledAttackDescriber() {
        this.statusEffectDescriber = new StatusEffectDescriber();
    }

    @Override
    protected void generateDescriptions(LeveledAttackData data) {
        LeveledAttackData.AttackData previous = null;

        for (int level = 1; level <= LEVEL_COUNT; level++) {
            Optional<LeveledAttackData.AttackData> resolved = data.resolve(level);

            if (resolved.isEmpty()) {
                setLevelDescription(level, Component.empty());
                previous = null;
                continue;
            }

            Component description;
            LeveledAttackData.AttackData current = resolved.get();
            if (current.descriptionOverride().isPresent()) {
                description = Component.translatable(current.descriptionOverride().get());
            } else {
                description = previous == null ? describeInitialAttack(current) : describeChanges(previous, current);
            }

            setLevelDescription(level, description);
            previous = current;
        }
    }

    private Component describeInitialAttack(LeveledAttackData.AttackData attack) {
        List<Component> clauses = new ArrayList<>();

        boolean ranged = attack.isRanged();
        MutableComponent output = ranged ? Component.literal("Ranged attack ") : Component.literal("Melee attack ");

        clauses.add(Component.literal("deals " + ObjectDescriber.formatNumber(attack.damage()) + " damage"));

        if (attack.knockback() != 0.0F) {
            clauses.add(Component.literal("has knockback " + ObjectDescriber.formatNumber(attack.knockback())));
        }

        if (ranged) {
            float range = attack.range() < 0.1f ? 10.0f : attack.range();
            clauses.add(Component.literal("has a range of " + ObjectDescriber.formatNumber(range) + " blocks"));
        }

        clauses.add(Component.literal("has a cooldown of " + ObjectDescriber.formatDuration(attack.cooldown())));

        if (!attack.statusEffects().isEmpty()) {
            clauses.add(Component.literal("applies: \n"));
        }

        output.append(ObjectDescriber.joinClauses(clauses));

        // Should effects be multi-line?
        for (StatusEffectApplication effect : attack.statusEffects()) {
            output.append(statusEffectDescriber.describe(effect)).append("\n");
        }
        output.getSiblings().removeLast();

        return output;
    }

    private Component describeChanges(LeveledAttackData.AttackData previous, LeveledAttackData.AttackData current) {
        List<Component> clauses = new ArrayList<>();

        // Attack type / range
        float currentRange = current.range() < 0.1F ? 10.0F : current.range();
        float previousRange = previous.range() < 0.1F ? 10.0F : previous.range();

        if (previous.isRanged() != current.isRanged()) {
            if (current.isRanged()) {
                clauses.add(Component.literal(
                        "the attack is now ranged with a range of "
                                + ObjectDescriber.formatNumber(currentRange) + " blocks"
                ));
            }
        } else if (current.isRanged() && Float.compare(previousRange, currentRange) != 0) {
            clauses.add(ObjectDescriber.numberChange(
                    "range",
                    previousRange,
                    currentRange,
                    ObjectDescriber::formatNumber,
                    " blocks"
            ));
        }

        // Damage
        if (Float.compare(previous.damage(), current.damage()) != 0) {
            clauses.add(ObjectDescriber.numberChange(
                    "damage",
                    previous.damage(),
                    current.damage(),
                    ObjectDescriber::formatNumber,
                    ""
            ));
        }

        // Knockback
        if (Float.compare(previous.knockback(), current.knockback()) != 0) {
            clauses.add(ObjectDescriber.numberChange(
                    "knockback",
                    previous.knockback(),
                    current.knockback(),
                    ObjectDescriber::formatNumber,
                    ""
            ));
        }

        // Cooldown
        if (previous.cooldown() != current.cooldown()) {
            clauses.add(ObjectDescriber.numberChange(
                    "cooldown",
                    previous.cooldown(),
                    current.cooldown(),
                    ObjectDescriber::formatDuration,
                    ""
            ));
        }

        // Status effects
        List<Component> effectChanges = new ArrayList<>();
        ChangingObjectDescriber.describeObjectChanges(
                statusEffectDescriber,
                statusEffectDescriber::describeSimply,
                previous.statusEffects(),
                current.statusEffects(),
                "applies"
        ).ifPresent(effectChanges::add);

        // Nothing changed
        if (clauses.isEmpty() && effectChanges.isEmpty()) {
            return Component.empty();
        }

        MutableComponent description = Component.empty();
        if (!clauses.isEmpty()) {
            description.append(ObjectDescriber.capitalizeFirst(ObjectDescriber.joinClauses(clauses)));
        }

        if (!effectChanges.isEmpty()) {
            description.append("\n");
            description.append(ObjectDescriber.multiLine(effectChanges));
        }

        return description;
    }

}