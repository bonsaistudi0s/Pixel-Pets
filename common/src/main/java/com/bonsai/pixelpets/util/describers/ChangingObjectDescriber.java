package com.bonsai.pixelpets.util.describers;

import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Function;

public interface ChangingObjectDescriber<T> extends ObjectDescriber<T> {

    Component describeModification(ObjectModification<T> modification);
    Component describeSimply(T object);

    boolean sameIdentity(T first, T second);

    default ObjectDifferences<T> findDifferences(List<? extends T> previous, List<? extends T> current) {
        List<T> unmatchedPrevious = new ArrayList<>(previous);
        List<T> unmatchedCurrent = new ArrayList<>(current);

        for (Iterator<T> iterator = unmatchedPrevious.iterator(); iterator.hasNext();) {
            T oldObject = iterator.next();
            int matchingIndex = unmatchedCurrent.indexOf(oldObject);

            if (matchingIndex >= 0) {
                iterator.remove();
                unmatchedCurrent.remove(matchingIndex);
            }
        }

        List<ObjectModification<T>> modified = new ArrayList<>();

        for (Iterator<T> iterator = unmatchedPrevious.iterator();
             iterator.hasNext();) {
            T oldObject = iterator.next();
            int matchingIndex = findSame(unmatchedCurrent, oldObject);

            if (matchingIndex >= 0) {
                T newObject = unmatchedCurrent.remove(matchingIndex);

                modified.add(new ObjectModification<>(oldObject, newObject));
                iterator.remove();
            }
        }

        return new ObjectDifferences<>(unmatchedCurrent, unmatchedPrevious, modified);
    }

    default int findSame(List<? extends T> objects, T target) {
        for (int i = 0; i < objects.size(); i++) {
            if (sameIdentity(objects.get(i), target)) {
                return i;
            }
        }

        return -1;
    }

    record ObjectModification<T>(T previous, T current) {
        public ObjectModification {
            Objects.requireNonNull(previous);
            Objects.requireNonNull(current);
        }
    }

    record ObjectDifferences<T>(List<T> added, List<T> removed, List<ObjectModification<T>> modified) {
        public ObjectDifferences {
            added = List.copyOf(added);
            removed = List.copyOf(removed);
            modified = List.copyOf(modified);
        }
    }

    static <T> Optional<Component> describeObjectChanges(
            ChangingObjectDescriber<T> describer,
            Function<T, Component> describeSimply,
            List<T> previous,
            List<T> current,
            String action)
    {
        var differences = describer.findDifferences(previous, current);

        List<Component> changes = new ArrayList<>();

        // Modified objects
        for (var modification : differences.modified()) {
            changes.add(
                    describeSimply.apply(modification.previous())
                            .copy()
                            .append(Component.literal(" changes "))
                            .append(describer.describeModification(modification))
            );
        }

        // Removed objects
        if (!differences.removed().isEmpty()) {
            changes.add(
                    Component.literal("No longer " + action + " ")
                            .append(ObjectDescriber.joinClauses(
                                    differences.removed().stream()
                                            .map(describeSimply)
                                            .toList(),
                                    "or"))
            );
        }

        // Added objects
        if (!differences.added().isEmpty()) {
            changes.add(
                    Component.literal("Now also " + action + " ")
                            .append(ObjectDescriber.joinClauses(
                                    differences.added().stream()
                                            .map(describer::describe)
                                            .toList()
                            ))
            );
        }

        return changes.isEmpty()
                ? Optional.empty()
                : Optional.of(ObjectDescriber.multiLine(changes));
    }
}