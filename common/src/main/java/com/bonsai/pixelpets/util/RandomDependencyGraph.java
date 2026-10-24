package com.bonsai.pixelpets.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

// Used to generate a random graph for testing
// TODO consider making a version that is a random graph but uses real ResourceLocations to get complex graphs but retain other pet data relevant to the screen
public final class RandomDependencyGraph {

    public static <T, D extends IDependencyData<T>> Map<T, D> generate(int count, long seed, IntFunction<T> idFactory, BiFunction<Set<T>, Boolean, D> dataFactory) {
        return generate(count, seed, idFactory, dataFactory, Config.defaults());
    }

    public static <T, D extends IDependencyData<T>> Map<T, D> generate(int count, long seed, IntFunction<T> idFactory, BiFunction<Set<T>, Boolean, D> dataFactory, Config config) {
        Map<T, D> merged = new LinkedHashMap<>();
        for (Map<T, D> graph : generateGraphs(count, seed, idFactory, dataFactory, config)) {
            merged.putAll(graph);
        }
        return merged;
    }

    public static <T, D extends IDependencyData<T>> List<Map<T, D>> generateGraphs(int count, long seed, IntFunction<T> idFactory, BiFunction<Set<T>, Boolean, D> dataFactory, Config config) {
        Random random = new Random(seed);
        List<Map<T, D>> graphs = new ArrayList<>();

        int index = 0;
        while (index < count) {
            int size = config.minGraphSize() + random.nextInt(config.maxGraphSize() - config.minGraphSize() + 1);
            size = Math.min(size, count - index);

            graphs.add(generateGraph(index, size, random, idFactory, dataFactory, config));
            index += size;
        }

        return graphs;
    }

    private static <T, D extends IDependencyData<T>> Map<T, D> generateGraph(int startIndex, int size, Random random, IntFunction<T> idFactory, BiFunction<Set<T>, Boolean, D> dataFactory, Config config) {

        Map<T, D> graph = new LinkedHashMap<>();
        List<T> ids = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            T id = Objects.requireNonNull(idFactory.apply(startIndex + i), "idFactory returned null for index " + (startIndex + i));

            int maxPrerequisites = Math.min(config.maxPrerequisites(), i);
            int prerequisiteCount = maxPrerequisites == 0 ? 0 : random.nextInt(maxPrerequisites + 1);

            Set<T> prerequisites = new LinkedHashSet<>();
            while (prerequisites.size() < prerequisiteCount) {
                prerequisites.add(ids.get(random.nextInt(i)));
            }

            boolean requireAll = prerequisites.size() < 2 || random.nextDouble() < config.requireAllProbability();

            D data = Objects.requireNonNull(dataFactory.apply(Collections.unmodifiableSet(prerequisites), requireAll), "dataFactory returned null for " + id);

            ids.add(id);
            graph.put(id, data);
        }

        return graph;
    }

    public record Config(int minGraphSize, int maxGraphSize, int maxPrerequisites, double requireAllProbability) {
        public static Config defaults() {
            return new Config(1, 16, 8, 0.5);
        }
    }
}