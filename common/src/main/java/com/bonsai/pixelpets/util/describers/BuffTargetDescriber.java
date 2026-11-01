package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.BuffTarget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// TODO test
public class BuffTargetDescriber implements ObjectDescriber<BuffTarget> {

    @Override
    public Component describe(BuffTarget target) {
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
            // Shouldn't get here
            return Component.literal("Targets nobody.");
        }

        return Component.literal("Targets ").append(ObjectDescriber.joinClauses(targets));
    }

    public Component describeModification(BuffTarget previous, BuffTarget current) {
        List<Component> changes = new ArrayList<>();

        if (previous.self() != current.self()) {
            changes.add(Component.literal("this pet is" + (current.self() ? " now a target" : " no longer a target")));
        }

        if (previous.owner() != current.owner()) {
            changes.add(Component.literal("the owner is" + (current.owner() ? " now a target" : " no longer a target")));
        }

        if (previous.otherPets() != current.otherPets()) {
            changes.add(Component.literal("the other pets are" + (current.otherPets() ? " now targets" : " no longer targets")));
        }

        if (changes.isEmpty()) {
            return Component.empty();
        }

        return ObjectDescriber.joinClauses(changes);
    }
}