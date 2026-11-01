package com.bonsai.pixelpets.util;

import java.util.*;

// There are edge cases with loops/mutual dependencies with mixed edge types that could be resolved algorithmically if we cared
// They are weird when thought of in the context of pet unlock order, so I don't think they're important to address
/*
    A AND> B
    B AND> C
    C  OR> A, D
    This graph has a cycle, but can be resolved by breaking C->A without really losing anything

    B OR> A,C
    C OR> A,D
    D OR> A,B
    This graph has a cycle (BCD), but can be resolved by breaking all the cycle edges and leaving those connecting to A
 */
public class GraphProcessor {

    public static <T> Map<T, ? extends IDependencyData<T>> process(Map<T, ? extends IDependencyData<T>> raw) {
        Map<T, IDependencyData<T>> graph = new LinkedHashMap<>();
        for (var entry : raw.entrySet()) {
            IDependencyData<T> data = Objects.requireNonNull(entry.getValue(), "dependency data for " + entry.getKey());
            Set<T> prerequisites = new LinkedHashSet<>();
            for (T prerequisite : data.prerequisites()) {
                if (raw.containsKey(prerequisite)) {
                    prerequisites.add(prerequisite);
                } else {
                    System.out.println("[PixelPets] WARNING: " + entry.getKey() + " requires unknown object " + prerequisite + "; ignoring that prerequisite");
                }
            }
            graph.put(entry.getKey(), new DependencyData<>(prerequisites, data.requireAll()));
        }

        checkMutual(graph);
        checkCycles(graph);
        return reduce(graph);
    }

    private static <T> void checkMutual(Map<T, IDependencyData<T>> graph) {
        List<String> pairs = new ArrayList<>();
        Set<T> checked = new HashSet<>();
        for (var entry : graph.entrySet()) {
            T node = entry.getKey();
            for (T prerequisite : entry.getValue().prerequisites()) {
                if (!Objects.equals(node, prerequisite) && !checked.contains(prerequisite) && graph.containsKey(prerequisite) && graph.get(prerequisite).prerequisites().contains(node)) {
                    pairs.add(node + " <-> " + prerequisite);
                }
            }
            checked.add(node);
        }
        if (!pairs.isEmpty()) {
            throw new IllegalStateException("Mutual dependencies: " + String.join(", ", pairs));
        }
    }

    private static <T> void checkCycles(Map<T, IDependencyData<T>> graph) {
        Map<T, Visit> state = new HashMap<>();
        List<T> stack = new ArrayList<>();
        for (T node : graph.keySet()) {
            visit(node, graph, state, stack);
        }
    }

    private static <T> void visit(T node, Map<T, IDependencyData<T>> graph, Map<T, Visit> state, List<T> stack) {
        Visit current = state.get(node);
        if (current == Visit.DONE) {
            return;
        }

        if (current == Visit.ACTIVE) {
            int start = stack.indexOf(node);
            List<String> cycle = new ArrayList<>();
            for (int i = Math.max(0, start); i < stack.size(); i++) {
                cycle.add(String.valueOf(stack.get(i)));
            }
            cycle.add(String.valueOf(node));
            throw new IllegalStateException("Dependency cycle: " + String.join(" -> ", cycle));
        }

        state.put(node, Visit.ACTIVE);
        stack.add(node);

        for (T prerequisite : graph.get(node).prerequisites()) {
            visit(prerequisite, graph, state, stack);
        }

        stack.removeLast();
        state.put(node, Visit.DONE);
    }

    // Assumes the above checks have been done, for simplicity
    private static <T> Map<T, IDependencyData<T>> reduce(Map<T, IDependencyData<T>> graph) {
        Map<T, Integer> index = new HashMap<>();
        for (T node : graph.keySet()) {
            index.put(node, index.size());
        }

        Map<T, BitSet> guaranteed = new HashMap<>();
        for (T node : graph.keySet()) {
            guaranteed(node, graph, index, guaranteed);
        }

        Map<T, IDependencyData<T>> reduced = new LinkedHashMap<>();

        for (var entry : graph.entrySet()) {
            Set<T> prerequisites = entry.getValue().prerequisites();
            boolean requireAll = entry.getValue().requireAll();

            Set<T> kept = new LinkedHashSet<>();

            if (requireAll) {
                // AND: Remove prerequisites guaranteed by another prerequisite
                BitSet indirect = new BitSet();

                for (T prerequisite : prerequisites) {
                    indirect.or(guaranteed.get(prerequisite));
                }

                for (T prerequisite : prerequisites) {
                    if (!indirect.get(index.get(prerequisite))) {
                        kept.add(prerequisite);
                    }
                }
            } else {
                // OR: Remove prerequisites that guarantee another prerequisite
                for (T prerequisite : prerequisites) {
                    Set<T> others = new HashSet<>(prerequisites);
                    others.remove(prerequisite);

                    if (!guaranteesAny(prerequisite, others, graph, new HashMap<>())) {
                        kept.add(prerequisite);
                    }
                }
            }

            reduced.put(entry.getKey(), new DependencyData<>(kept, requireAll));
        }

        return reduced;
    }

    private static <T> BitSet guaranteed(T node, Map<T, IDependencyData<T>> graph, Map<T, Integer> index, Map<T, BitSet> memo) {
        BitSet known = memo.get(node);
        if (known != null) {
            return known;
        }

        Set<T> prerequisites = graph.get(node).prerequisites();
        BitSet result = new BitSet();

        boolean requireAll = graph.get(node).requireAll();
        boolean first = true;

        for (T prerequisite : prerequisites) {
            BitSet branch = (BitSet) guaranteed(prerequisite, graph, index, memo).clone();
            branch.set(index.get(prerequisite));

            if (requireAll) {
                result.or(branch);
            } else if (first) {
                result.or(branch);
            } else {
                result.and(branch);
            }

            first = false;
        }

        memo.put(node, result);
        return result;
    }

    private static <T> boolean guaranteesAny(T node, Set<T> targets, Map<T, IDependencyData<T>> graph, Map<T, Boolean> memo) {
        if (targets.contains(node)) {
            return true;
        }

        Boolean known = memo.get(node);
        if (known != null) {
            return known;
        }

        IDependencyData<T> data = graph.get(node);
        Set<T> prerequisites = data.prerequisites();

        if (prerequisites.isEmpty()) {
            memo.put(node, false);
            return false;
        }

        boolean result;

        if (data.requireAll()) {
            // AND: At least one prerequisite must guarantee a target
            result = prerequisites.stream().anyMatch(p -> guaranteesAny(p, targets, graph, memo));
        } else {
            // OR: Every possible prerequisite must guarantee a target
            result = prerequisites.stream().allMatch(p -> guaranteesAny(p, targets, graph, memo));
        }

        memo.put(node, result);
        return result;
    }

    private enum Visit {
        ACTIVE, DONE
    }

}
