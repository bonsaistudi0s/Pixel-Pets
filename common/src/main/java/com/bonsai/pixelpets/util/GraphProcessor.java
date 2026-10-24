package com.bonsai.pixelpets.util;

import java.util.*;

// This processing treats `AND` and `OR` edges as `AND` edges
// The upside is that it's much simpler, and generally our input data should be/will be simple

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
                if (!Objects.equals(node, prerequisite)
                        && !checked.contains(prerequisite)
                        && graph.containsKey(prerequisite)
                        && graph.get(prerequisite).prerequisites().contains(node)) {
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

    private static <T> Map<T, IDependencyData<T>> reduce(Map<T, IDependencyData<T>> graph) {
        Map<T, Integer> index = new HashMap<>();
        for (T node : graph.keySet()) {
            index.put(node, index.size());
        }

        Map<T, BitSet> reach = new HashMap<>();
        for (T node : graph.keySet()) {
            reach(node, graph, index, reach);
        }

        Map<T, IDependencyData<T>> reduced = new LinkedHashMap<>();
        for (var entry : graph.entrySet()) {
            BitSet indirect = new BitSet();
            for (T prerequisite : entry.getValue().prerequisites()) {
                indirect.or(reach.get(prerequisite));
            }

            Set<T> kept = new LinkedHashSet<>();
            for (T prerequisite : entry.getValue().prerequisites()) {
                if (!indirect.get(index.get(prerequisite))) {
                    kept.add(prerequisite);
                }
            }

            reduced.put(entry.getKey(), new DependencyData<>(kept, entry.getValue().requireAll()));
        }

        return reduced;
    }

    private static <T> BitSet reach(T node, Map<T, IDependencyData<T>> graph, Map<T, Integer> index, Map<T, BitSet> memo) {
        BitSet known = memo.get(node);
        if (known != null) {
            return known;
        }

        BitSet result = new BitSet();
        for (T prerequisite : graph.get(node).prerequisites()) {
            result.set(index.get(prerequisite));
            result.or(reach(prerequisite, graph, index, memo));
        }

        memo.put(node, result);
        return result;
    }

    private enum Visit {
        ACTIVE, DONE
    }

}
