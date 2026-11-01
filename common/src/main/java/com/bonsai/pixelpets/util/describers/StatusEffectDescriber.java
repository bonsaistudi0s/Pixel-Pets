package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.StatusEffectApplication;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class StatusEffectDescriber implements ChangingObjectDescriber<StatusEffectApplication> {

    @Override
    public boolean sameIdentity(StatusEffectApplication first, StatusEffectApplication second) {
        return first.effect().equals(second.effect());
    }

    @Override
    public Component describe(StatusEffectApplication application) {
        MutableComponent description =  application.effect().value().getDisplayName().copy();

        if (application.amplifier() > 0) {
            description.append(Component.literal(" " + ObjectDescriber.intToRomanLookup(application.amplifier() + 1)));
        }

        description.append(Component.literal(" for " + ObjectDescriber.formatDuration(application.duration())));

        if (Float.compare(application.chance(), 1.0F) != 0) {
            description.append(Component.literal(" (" + ObjectDescriber.formatNumber(application.chance() * 100.0F) + "% chance)"));
        }

        return description;
    }

    public Component describeSimply(StatusEffectApplication application) {
        return application.effect().value().getDisplayName().copy();
    }


    @Override
    public Component describeModification(ChangingObjectDescriber.ObjectModification<StatusEffectApplication> modification) {
        StatusEffectApplication previous = modification.previous();
        StatusEffectApplication current = modification.current();

        List<Component> clauses = new ArrayList<>();

        if (previous.amplifier() != current.amplifier()) {
            clauses.add(Component.literal(
                    "level from "
                            + ObjectDescriber.intToRomanLookup(previous.amplifier() + 1)
                            + " to "
                            + ObjectDescriber.intToRomanLookup(current.amplifier() + 1)
            ));
        }

        if (previous.duration() != current.duration()) {
            clauses.add(Component.literal(
                    "duration from "
                            + ObjectDescriber.formatDuration(previous.duration())
                            + " to "
                            + ObjectDescriber.formatDuration(current.duration())
            ));
        }

        if (Float.compare(previous.chance(), current.chance()) != 0) {
            clauses.add(Component.literal(
                    "chance from "
                            + ObjectDescriber.formatNumber(previous.chance() * 100.0F)
                            + "% to "
                            + ObjectDescriber.formatNumber(current.chance() * 100.0F)
                            + "%"
            ));
        }

        return ObjectDescriber.joinClauses(clauses);
    }
}
