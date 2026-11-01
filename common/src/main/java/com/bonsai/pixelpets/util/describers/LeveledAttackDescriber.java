package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.LeveledAttackData;
import com.bonsai.pixelpets.pixelpets.registration.data.ability.StatusEffectApplication;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

public class LeveledAttackDescriber extends LeveledDescriber<LeveledAttackData> {

    private final StatusEffectDescriber statusEffectDescriber;

    public LeveledAttackDescriber() {
        this(new StatusEffectDescriber());
    }

    public LeveledAttackDescriber(StatusEffectDescriber statusEffectDescriber) {
        this.statusEffectDescriber = Objects.requireNonNull(statusEffectDescriber);
    }

    @Override
    protected void generateDescriptions(LeveledAttackData data) {
        LeveledAttackData.PixelPetAttackData previous = null;

        for (int level = 1; level <= LEVEL_COUNT; level++) {
            Optional<LeveledAttackData.PixelPetAttackData> resolved = data.resolve(level);

            if (resolved.isEmpty()) {
                setLevelDescription(level, Component.empty());
                previous = null;
                continue;
            }

            Component description;
            LeveledAttackData.PixelPetAttackData current = resolved.get();
            if (current.descriptionOverride().isPresent()) {
                description = Component.translatable(current.descriptionOverride().get());
            } else {
                description = previous == null ? describeInitialAttack(current) : describeChanges(previous, current);
            }

            setLevelDescription(level, description);
            previous = current;
        }
    }

    private Component describeInitialAttack(LeveledAttackData.PixelPetAttackData attack) {
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

    private Component describeChanges(LeveledAttackData.PixelPetAttackData previous, LeveledAttackData.PixelPetAttackData current) {
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
        Optional<Component> statusEffectChanges = describeStatusEffectChanges(previous.statusEffects(), current.statusEffects());

        // Nothing changed
        if (clauses.isEmpty() && statusEffectChanges.isEmpty()) {
            return Component.empty();
        }

        MutableComponent description = Component.empty();
        if (!clauses.isEmpty()) {
            clauses.set(0, ObjectDescriber.capitalizeFirst(clauses.getFirst()));
            description.append(ObjectDescriber.joinClauses(clauses)).append(Component.literal("."));
        }

        statusEffectChanges.ifPresent(statusEffects -> {
            if (!clauses.isEmpty()) {
                description.append("\n");
            }

            description.append(statusEffects);
        });

        return description;
    }

    private Optional<Component> describeStatusEffectChanges(List<StatusEffectApplication> previous, List<StatusEffectApplication> current) {
        var differences = statusEffectDescriber.findDifferences(previous, current);

        List<Component> changes = new ArrayList<>();

        // Modified effects
        for (var modification : differences.modified()) {
            changes.add(
                    statusEffectDescriber.describeSimply(modification.previous())
                            .copy()
                            .append(Component.literal(" changes "))
                            .append(statusEffectDescriber.describeModification(modification))
                            .append(Component.literal("."))
            );
        }

        // Removed effects
        if (!differences.removed().isEmpty()) {
            changes.add(
                    Component.literal("No longer applies: ")
                            .append(ObjectDescriber.joinClauses(
                                    differences.removed().stream()
                                            .map(statusEffectDescriber::describeSimply)
                                            .toList()
                            ))
                            .append(Component.literal("."))
            );
        }

        // Added effects
        if (!differences.added().isEmpty()) {
            changes.add(
                    Component.literal("Now also applies: ")
                            .append(ObjectDescriber.joinClauses(
                                    differences.added().stream()
                                            .map(statusEffectDescriber::describe)
                                            .toList()
                            ))
                            .append(Component.literal("."))
            );
        }

        if (changes.isEmpty()) {
            return Optional.empty();
        }

        MutableComponent result = Component.empty();

        for (int i = 0; i < changes.size(); i++) {
            if (i > 0) {
                result.append(Component.literal("\n"));
            }
            result.append(changes.get(i));
        }


        return Optional.of(result);
    }

}