package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.AttributeModifierApplication;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;

// TODO test
public class AttributeModifierDescriber implements ChangingObjectDescriber<AttributeModifierApplication> {

    @Override
    public boolean sameIdentity(AttributeModifierApplication first, AttributeModifierApplication second) {
        return first.attribute().equals(second.attribute()) && first.modifier().id().equals(second.modifier().id());
    }

    @Override
    public Component describe(AttributeModifierApplication application) {
        AttributeModifier modifier = application.modifier();
        return describeSimply(application).copy().append(Component.literal(" " + formatAmount(modifier) + " (" + operationName(modifier.operation()) + ")"));
    }

    public Component describeSimply(AttributeModifierApplication application) {
        return Component.translatable(application.attribute().value().getDescriptionId());
    }

    public Component describeModification(ChangingObjectDescriber.ObjectModification<AttributeModifierApplication> modification) {
        AttributeModifier previous = modification.previous().modifier();
        AttributeModifier current = modification.current().modifier();

        List<Component> clauses = new ArrayList<>();

        boolean amountChanged = Double.compare(previous.amount(), current.amount()) != 0;

        boolean operationChanged = previous.operation() != current.operation();

        if (amountChanged || operationChanged) {
            String previousDescription = formatAmount(previous);
            String currentDescription = formatAmount(current);

            if (operationChanged) {
                previousDescription += " (" + operationName(previous.operation()) + ")";
                currentDescription += " (" + operationName(current.operation()) + ")";
            }

            clauses.add(Component.literal("from " + previousDescription + " to " + currentDescription));
        }

        return ObjectDescriber.joinClauses(clauses);
    }

    // Warframe-esque numbering, additive multipliers are % while true multipliers are x
    private static String formatAmount(AttributeModifier modifier) {
        double amount = modifier.amount();

        return switch (modifier.operation()) {
            case ADD_VALUE -> ObjectDescriber.formatSignedNumber(amount);
            case ADD_MULTIPLIED_BASE -> ObjectDescriber.formatSignedNumber(amount * 100.0) + "%";
            case ADD_MULTIPLIED_TOTAL -> "x" + ObjectDescriber.formatNumber(1.0 + amount);
        };
    }

    private static String operationName(AttributeModifier.Operation operation) {
        return switch (operation) {
            case ADD_VALUE -> "additive";
            case ADD_MULTIPLIED_BASE -> "additive multiplier";
            case ADD_MULTIPLIED_TOTAL -> "total multiplier";
        };
    }
}