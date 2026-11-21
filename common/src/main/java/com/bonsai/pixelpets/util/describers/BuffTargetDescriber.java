package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.BuffTarget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// TODO test
public class BuffTargetDescriber implements ObjectDescriber<BuffTarget> {

    @Override
    public Component describe(BuffTarget target) {
        return Component.literal("Targets ").append(describeTargets(target));
    }

    public Component describeTargets(BuffTarget target) {
        List<Component> targets = new ArrayList<>();

        if (target.owner()) {
            targets.add(Component.literal("its owner"));
        }

        if (target.self() && target.otherPets()) {
            targets.add(Component.literal("all pets"));
        } else {
            if (target.self()) {
                targets.add(Component.literal("itself"));
            }

            if (target.otherPets()) {
                targets.add(Component.literal("other pets"));
            }
        }

        if (targets.isEmpty()) {
            return Component.literal("nobody");
        }

        return ObjectDescriber.joinClauses(targets);
    }

    public List<Component> describeModification(BuffTarget previous, BuffTarget current) {
        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();

        if (previous.self() != current.self()) {
            (current.self() ? added : removed).add("this pet");
        }

        if (previous.owner() != current.owner()) {
            (current.owner() ? added : removed).add("the owner");
        }

        if (previous.otherPets() != current.otherPets()) {
            (current.otherPets() ? added : removed).add("other pets");
        }

        List<Component> changes = new ArrayList<>();

        if (!added.isEmpty()) {
            changes.add(describeTargetChange(added, true));
        }

        if (!removed.isEmpty()) {
            changes.add(describeTargetChange(removed, false));
        }

        return changes;
    }

    private static Component describeTargetChange(List<String> targets, boolean added) {
        boolean plural = targets.size() > 1 || targets.contains("Other pets");
        List<Component> targeted = new ArrayList<>(List.of());
        targets.forEach((s -> targeted.add(Component.literal(s))));
        return Component.literal(
                ObjectDescriber.joinClauses(targeted).getString()
                        + (plural ? " are " : " is ")
                        + (added ? "now " : "no longer ")
                        + (plural ? "targets" : "a target")
        );
    }
}