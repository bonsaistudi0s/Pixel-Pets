package com.bonsai.pixelpets.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

// Based HEAVILY on fast-sugiyama (which itself is mostly just an API wrapper for rust-sugiyama)
// TODO straighten edges with <2degrees?
public final class LayeredGraph<T> implements IGraphLayout<T> {
    private static final double EPSILON = 1e-9;

    private final Map<T, Point> positions;
    private final List<Edge<T>> edges;
    private final Map<T, Boolean> requirements;
    private final Map<T, ? extends IDependencyData<T>> dependencies;

    private LayeredGraph(Map<T, Point> positions, List<Edge<T>> edges, Map<T, Boolean> requirements, Map<T, ? extends IDependencyData<T>> dependencies) {
        this.positions = Collections.unmodifiableMap(new LinkedHashMap<>(positions));
        this.edges = List.copyOf(edges);
        this.requirements = Collections.unmodifiableMap(new LinkedHashMap<>(requirements));
        this.dependencies = Collections.unmodifiableMap(new LinkedHashMap<>(dependencies));
    }

    public static <T> LayeredGraph<T> generate(Map<T, ? extends IDependencyData<T>> raw, Config config) {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(config, "config");

        Map<T, ? extends IDependencyData<T>> graph = GraphProcessor.process(raw);
        if (graph.isEmpty()) {
            return new LayeredGraph<>(Map.of(), List.of(), Map.of(), Map.of());
        }

        List<ComponentLayout<T>> layouts = new ArrayList<>();
        List<T> isolated = new ArrayList<>();

        for (Map<T, ? extends IDependencyData<T>> component : components(graph)) {
            if (component.size() == 1 && component.values().iterator().next().prerequisites().isEmpty()) {
                isolated.add(component.keySet().iterator().next());
            } else {
                layouts.add(layoutComponent(component, config));
            }
        }

        if (!isolated.isEmpty()) {
            layouts.add(layoutIsolatedNodes(isolated, config));
        }

        packComponents(layouts, config);

        Map<T, Point> positions = new LinkedHashMap<>();
        List<Edge<T>> edges = new ArrayList<>();
        for (ComponentLayout<T> layout : layouts) {
            positions.putAll(layout.positions);
            edges.addAll(layout.edges);
        }

        Map<T, Boolean> requirements = new LinkedHashMap<>();
        for (var entry : graph.entrySet()) {
            requirements.put(entry.getKey(), entry.getValue().requireAll());
        }

        return new LayeredGraph<>(positions, edges, requirements, graph);
    }

    private static <T> ComponentLayout<T> layoutComponent(Map<T, ? extends IDependencyData<T>> component, Config config) {
        LayoutGraph<T> graph = buildGraph(component, config);

        initRank(graph);
        minimizeEdgeLength(graph);
        normalizeRanks(graph);

        insertDummyVertices(graph, config);
        List<List<Vertex<T>>> layers = reduceCrossings(initialOrder(graph));

        List<Map<Vertex<T>, Double>> layouts = createLayouts(graph, layers);
        alignToSmallestWidth(graph, layouts);
        Map<Vertex<T>, Double> cross = calculateRelativeCoords(layouts);

        double minCross = cross.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
        cross.replaceAll((v, x) -> x - minCross);

        Map<Integer, Double> rank = rankCoordinates(graph, layers, config);
        Map<T, Point> positions = new LinkedHashMap<>();
        for (Vertex<T> vertex : graph.vertices) {
            if (!vertex.dummy) {
                positions.put(vertex.value, new Point(rank.get(vertex.rank), cross.get(vertex)));
            }
        }

        List<Edge<T>> edges = new ArrayList<>(graph.originalEdges.size());
        for (OriginalEdge<T> original : graph.originalEdges) {
            List<Point> path = new ArrayList<>(original.path.size());
            for (Vertex<T> vertex : original.path) {
                path.add(new Point(rank.get(vertex.rank), cross.get(vertex)));
            }
            removeDuplicatePoints(path);
            if (path.size() >= 2) {
                edges.add(new Edge<>(original.source, original.target, path));
            }
        }

        double half = config.nodeSize() * 0.5;
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point p : positions.values()) {
            minX = Math.min(minX, p.x() - half);
            minY = Math.min(minY, p.y() - half);
            maxX = Math.max(maxX, p.x() + half);
            maxY = Math.max(maxY, p.y() + half);
        }
        for (Edge<T> edge : edges) {
            for (Point p : edge.points()) {
                minX = Math.min(minX, p.x());
                minY = Math.min(minY, p.y());
                maxX = Math.max(maxX, p.x());
                maxY = Math.max(maxY, p.y());
            }
        }

        return new ComponentLayout<>(positions, edges, minX, minY, maxX, maxY);
    }

    private static <T> ComponentLayout<T> layoutIsolatedNodes(List<T> nodes, Config config) {
        int columns = (int) Math.ceil(Math.sqrt(nodes.size()));
        int rows = (nodes.size() + columns - 1) / columns;
        double cell = (config.nodeSize() + config.laneSpacing()) / 2.0;

        Map<T, Point> positions = new LinkedHashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            positions.put(nodes.get(i), new Point(config.nodeSize() * 0.5 + (i % columns) * cell, config.nodeSize() * 0.5 + (i / columns) * cell));
        }

        return new ComponentLayout<>(positions, List.of(), 0, 0, columns * cell - config.laneSpacing(), rows * cell - config.laneSpacing());
    }

    private static <T> void packComponents(List<ComponentLayout<T>> layouts, Config config) {
        if (layouts.size() < 2) {
            if (layouts.size() == 1) {
                ComponentLayout<T> l = layouts.getFirst();
                translate(l, -l.minX, -l.minY);
            }
            return;
        }

        double gap = Math.max(config.laneSpacing(), config.nodeSize() * 0.25);
        List<ComponentLayout<T>> ordered = new ArrayList<>(layouts);
        ordered.sort(Comparator.comparingDouble((ComponentLayout<T> l) -> Math.max(l.width(), l.height())).reversed().thenComparing(Comparator.comparingDouble(ComponentLayout<T>::width).reversed()).thenComparing(Comparator.comparingDouble(ComponentLayout<T>::height).reversed()));

        List<Double> candidates = getDoubles(ordered, gap);

        Packing<T> best = null;
        for (double width : candidates) {
            Packing<T> packing = packIntoShelves(ordered, width, gap);
            if (best == null || betterPacking(packing, best)) best = packing;
        }

        for (Placement<T> placement : best.placements()) {
            ComponentLayout<T> l = placement.layout();
            translate(l, placement.x() - l.minX, placement.y() - l.minY);
        }

        layouts.clear();
        layouts.addAll(ordered);
    }

    private static <T> @NotNull List<Double> getDoubles(List<ComponentLayout<T>> ordered, double gap) {
        double area = 0;
        double totalWidth = 0;
        double maxWidth = 0;
        double maxHeight = 0;
        for (ComponentLayout<T> l : ordered) {
            area += l.width() * l.height();
            totalWidth += l.width();
            maxWidth = Math.max(maxWidth, l.width());
            maxHeight = Math.max(maxHeight, l.height());
        }

        double target = Math.sqrt(area + gap * gap * ordered.size());
        double minWidth = maxWidth;
        double maxCandidate = Math.max(target * 2, Math.min(totalWidth + gap * (ordered.size() - 1), maxHeight + target));

        List<Double> candidates = new ArrayList<>();
        candidates.add(minWidth);
        candidates.add(target);
        candidates.add(maxCandidate);

        for (int i = 0; i <= 40; i++) {
            double t = i / 40.0;
            candidates.add(minWidth + (maxCandidate - minWidth) * t);
        }

        double cumulative = 0;
        for (ComponentLayout<T> l : ordered) {
            cumulative += l.width() + gap;
            if (cumulative >= minWidth && cumulative <= maxCandidate) {
                candidates.add(cumulative);
            }
        }
        return candidates;
    }

    private static <T> Packing<T> packIntoShelves(List<ComponentLayout<T>> layouts, double targetWidth, double gap) {
        List<Shelf> shelves = new ArrayList<>();
        List<Placement<T>> placements = new ArrayList<>(layouts.size());

        for (ComponentLayout<T> layout : layouts) {
            Shelf bestShelf = null;
            double bestRemaining = Double.POSITIVE_INFINITY;

            for (Shelf shelf : shelves) {
                double remaining = targetWidth - shelf.usedWidth;
                if (layout.width() <= remaining + EPSILON && remaining - layout.width() < bestRemaining) {
                    bestRemaining = remaining - layout.width();
                    bestShelf = shelf;
                }
            }

            if (bestShelf == null) {
                double y = shelves.isEmpty() ? 0 : shelves.getLast().y + shelves.getLast().height + gap;
                bestShelf = new Shelf(y);
                shelves.add(bestShelf);
            }

            placements.add(new Placement<>(layout, bestShelf.usedWidth, bestShelf.y));
            bestShelf.usedWidth += layout.width() + gap;
            bestShelf.height = Math.max(bestShelf.height, layout.height());
        }

        double width = 0, height = 0;
        for (Shelf shelf : shelves) {
            width = Math.max(width, shelf.usedWidth - gap);
            height = Math.max(height, shelf.y + shelf.height);
        }
        return new Packing<>(placements, width, height);
    }

    private static <T> boolean betterPacking(Packing<T> a, Packing<T> b) {
        double ar = aspectRatio(a.width(), a.height());
        double br = aspectRatio(b.width(), b.height());
        if (ar < br - EPSILON) return true;
        if (Math.abs(ar - br) > EPSILON) return false;
        return a.width() * a.height() < b.width() * b.height() - EPSILON;
    }

    private static double aspectRatio(double width, double height) {
        if (width <= EPSILON || height <= EPSILON) return Double.POSITIVE_INFINITY;
        return Math.max(width / height, height / width);
    }

    private static <T> void translate(ComponentLayout<T> layout, double dx, double dy) {
        layout.positions.replaceAll((k, p) -> new Point(p.x() + dx, p.y() + dy));
        for (int i = 0; i < layout.edges.size(); i++) {
            Edge<T> edge = layout.edges.get(i);
            List<Point> points = edge.points().stream().map(p -> new Point(p.x() + dx, p.y() + dy)).toList();
            layout.edges.set(i, new Edge<>(edge.source(), edge.target(), points));
        }
        layout.minX += dx;
        layout.maxX += dx;
        layout.minY += dy;
        layout.maxY += dy;
    }

    private static void removeDuplicatePoints(List<Point> points) {
        for (int i = points.size() - 1; i > 0; i--) {
            Point a = points.get(i - 1);
            Point b = points.get(i);
            if (Math.abs(a.x() - b.x()) <= EPSILON && Math.abs(a.y() - b.y()) <= EPSILON) {
                points.remove(i);
            }
        }
    }

    private static <T> List<Map<T, ? extends IDependencyData<T>>> components(Map<T, ? extends IDependencyData<T>> graph) {
        Map<T, Set<T>> neighbors = new LinkedHashMap<>();
        for (T node : graph.keySet()) neighbors.put(node, new LinkedHashSet<>());

        for (var entry : graph.entrySet()) {
            for (T prerequisite : entry.getValue().prerequisites()) {
                neighbors.get(entry.getKey()).add(prerequisite);
                neighbors.get(prerequisite).add(entry.getKey());
            }
        }

        Set<T> visited = new LinkedHashSet<>();
        List<Map<T, ? extends IDependencyData<T>>> result = new ArrayList<>();

        for (T start : graph.keySet()) {
            if (!visited.add(start)) continue;

            Set<T> componentNodes = new LinkedHashSet<>();
            ArrayDeque<T> queue = new ArrayDeque<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                T node = queue.removeFirst();
                componentNodes.add(node);
                for (T neighbor : neighbors.get(node)) {
                    if (visited.add(neighbor)) queue.addLast(neighbor);
                }
            }

            Map<T, IDependencyData<T>> component = new LinkedHashMap<>();
            for (T node : graph.keySet()) {
                if (componentNodes.contains(node)) component.put(node, graph.get(node));
            }
            result.add(component);
        }

        return result;
    }

    private static <T> LayoutGraph<T> buildGraph(Map<T, ? extends IDependencyData<T>> data, Config config) {
        LayoutGraph<T> graph = new LayoutGraph<>();
        int id = 0;

        for (T node : data.keySet()) {
            Vertex<T> vertex = new Vertex<>(id++, node, false, config.nodeSize(), config.nodeSize());
            graph.vertices.add(vertex);
            graph.real.put(node, vertex);
        }

        for (var entry : data.entrySet()) {
            Vertex<T> target = graph.real.get(entry.getKey());
            for (T prerequisite : entry.getValue().prerequisites()) {
                Vertex<T> source = graph.real.get(prerequisite);
                OriginalEdge<T> original = new OriginalEdge<>(prerequisite, entry.getKey());
                original.path.add(source);
                original.path.add(target);
                graph.originalEdges.add(original);
                addEdge(graph, source, target, original);
            }
        }

        return graph;
    }

    private static <T> void addEdge(LayoutGraph<T> graph, Vertex<T> tail, Vertex<T> head, OriginalEdge<T> original) {
        GraphEdge<T> edge = new GraphEdge<>(tail, head, original);
        graph.edges.add(edge);
        tail.outgoing.add(edge);
        head.incoming.add(edge);
    }

    private static <T> void initRank(LayoutGraph<T> graph) {
        for (Vertex<T> vertex : topologicalOrder(graph)) {
            for (GraphEdge<T> edge : vertex.incoming) {
                vertex.rank = Math.max(vertex.rank, edge.tail.rank + 1);
            }
        }
    }

    private static <T> List<Vertex<T>> topologicalOrder(LayoutGraph<T> graph) {
        Map<Vertex<T>, Integer> indegree = new IdentityHashMap<>();
        ArrayDeque<Vertex<T>> queue = new ArrayDeque<>();

        for (Vertex<T> vertex : graph.vertices) {
            int degree = vertex.incoming.size();
            indegree.put(vertex, degree);
            if (degree == 0) queue.addLast(vertex);
        }

        List<Vertex<T>> result = new ArrayList<>(graph.vertices.size());
        while (!queue.isEmpty()) {
            Vertex<T> vertex = queue.removeFirst();
            result.add(vertex);
            for (GraphEdge<T> edge : vertex.outgoing) {
                int degree = indegree.merge(edge.head, -1, Integer::sum);
                if (degree == 0) queue.addLast(edge.head);
            }
        }

        if (result.size() != graph.vertices.size()) {
            throw new IllegalStateException("Graph must be acyclic");
        }
        return result;
    }

    private static <T> void minimizeEdgeLength(LayoutGraph<T> graph) {
        if (graph.vertices.size() < 2 || graph.edges.isEmpty()) return;

        Set<GraphEdge<T>> tree = feasibleTree(graph);
        int limit = Math.max(64, graph.vertices.size() * Math.max(1, graph.edges.size()) * 16);

        for (int i = 0; i < limit; i++) {
            GraphEdge<T> leaving = leaveEdge(graph, tree);
            if (leaving == null) return;

            Set<Vertex<T>> shifted = componentWithout(tree, leaving, leaving.head);
            GraphEdge<T> entering = enterEdge(graph, tree, shifted);
            if (entering == null) {
                throw new IllegalStateException("Network simplex could not find an entering edge");
            }

            int amount = slack(entering);
            for (Vertex<T> vertex : shifted) vertex.rank += amount;

            tree.remove(leaving);
            tree.add(entering);
        }

        throw new IllegalStateException("Network simplex did not converge");
    }

    private static <T> LinkedHashSet<GraphEdge<T>> feasibleTree(LayoutGraph<T> graph) {
        LinkedHashSet<GraphEdge<T>> tree = new LinkedHashSet<>();
        Set<Vertex<T>> inTree = Collections.newSetFromMap(new IdentityHashMap<>());
        inTree.add(graph.vertices.getFirst());

        while (inTree.size() < graph.vertices.size()) {
            boolean added;
            do {
                added = false;
                for (GraphEdge<T> edge : graph.edges) {
                    if (tree.contains(edge)) continue;
                    boolean tail = inTree.contains(edge.tail);
                    boolean head = inTree.contains(edge.head);
                    if (tail ^ head && slack(edge) == 0) {
                        tree.add(edge);
                        inTree.add(tail ? edge.head : edge.tail);
                        added = true;
                    }
                }
            } while (added && inTree.size() < graph.vertices.size());

            if (inTree.size() == graph.vertices.size()) return tree;

            int shift = getShift(graph, tree, inTree);
            for (Vertex<T> vertex : inTree) vertex.rank += shift;
        }

        return tree;
    }

    private static <T> int getShift(LayoutGraph<T> graph, LinkedHashSet<GraphEdge<T>> tree, Set<Vertex<T>> inTree) {
        GraphEdge<T> best = null;
        int bestSlack = Integer.MAX_VALUE;
        boolean tailInTree = false;

        for (GraphEdge<T> edge : graph.edges) {
            if (tree.contains(edge)) continue;
            boolean tail = inTree.contains(edge.tail);
            boolean head = inTree.contains(edge.head);
            if (tail == head) continue;

            int edgeSlack = slack(edge);
            if (edgeSlack < bestSlack) {
                best = edge;
                bestSlack = edgeSlack;
                tailInTree = tail;
            }
        }

        if (best == null) {
            throw new IllegalStateException("Could not construct a feasible spanning tree");
        }

        return tailInTree ? bestSlack : -bestSlack;
    }

    private static <T> int cutValue(LayoutGraph<T> graph, Set<GraphEdge<T>> tree, GraphEdge<T> removed) {
        Set<Vertex<T>> side = componentWithout(tree, removed, removed.head);
        int value = 0;

        for (GraphEdge<T> edge : graph.edges) {
            boolean tail = side.contains(edge.tail);
            boolean head = side.contains(edge.head);
            if (tail == head) continue;
            value += tail ? -1 : 1;
        }

        return value;
    }

    private static <T> GraphEdge<T> leaveEdge(LayoutGraph<T> graph, Set<GraphEdge<T>> tree) {
        for (GraphEdge<T> edge : graph.edges) {
            if (tree.contains(edge) && cutValue(graph, tree, edge) < 0) return edge;
        }
        return null;
    }

    private static <T> GraphEdge<T> enterEdge(LayoutGraph<T> graph, Set<GraphEdge<T>> tree, Set<Vertex<T>> shiftedSide) {
        GraphEdge<T> best = null;
        int bestSlack = Integer.MAX_VALUE;

        for (GraphEdge<T> edge : graph.edges) {
            if (tree.contains(edge)) continue;

            boolean tail = shiftedSide.contains(edge.tail);
            boolean head = shiftedSide.contains(edge.head);
            if (!tail || head) continue;

            int edgeSlack = slack(edge);
            if (edgeSlack < bestSlack) {
                best = edge;
                bestSlack = edgeSlack;
            }
        }
        return best;
    }

    private static <T> Set<Vertex<T>> componentWithout(Set<GraphEdge<T>> tree, GraphEdge<T> removed, Vertex<T> start) {
        Set<Vertex<T>> side = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<Vertex<T>> queue = new ArrayDeque<>();
        side.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Vertex<T> vertex = queue.removeFirst();
            for (GraphEdge<T> edge : tree) {
                if (edge == removed) continue;
                Vertex<T> other = edge.tail == vertex ? edge.head : edge.head == vertex ? edge.tail : null;
                if (other != null && side.add(other)) queue.addLast(other);
            }
        }
        return side;
    }

    private static <T> int slack(GraphEdge<T> edge) {
        return edge.head.rank - edge.tail.rank - 1;
    }

    private static <T> void normalizeRanks(LayoutGraph<T> graph) {
        int min = graph.vertices.stream().mapToInt(v -> v.rank).min().orElse(0);
        for (Vertex<T> vertex : graph.vertices) vertex.rank -= min;
    }

    private static <T> void insertDummyVertices(LayoutGraph<T> graph, Config config) {
        List<GraphEdge<T>> originalEdges = List.copyOf(graph.edges);
        int nextId = graph.vertices.size();

        for (GraphEdge<T> edge : originalEdges) {
            int length = edge.head.rank - edge.tail.rank;
            if (length <= 1) continue;

            edge.tail.outgoing.remove(edge);
            edge.head.incoming.remove(edge);
            graph.edges.remove(edge);

            OriginalEdge<T> original = edge.original;
            original.path.clear();
            original.path.add(edge.tail);

            Vertex<T> previous = edge.tail;
            for (int rank = edge.tail.rank + 1; rank < edge.head.rank; rank++) {
                Vertex<T> dummy = new Vertex<>(nextId++, null, true, config.dummyMargin() * 2.0, 0);
                dummy.rank = rank;
                graph.vertices.add(dummy);
                addEdge(graph, previous, dummy, original);
                original.path.add(dummy);
                previous = dummy;
            }

            addEdge(graph, previous, edge.head, original);
            original.path.add(edge.head);
        }
    }

    private static <T> List<List<Vertex<T>>> initialOrder(LayoutGraph<T> graph) {
        int maxRank = graph.vertices.stream().mapToInt(v -> v.rank).max().orElse(0);
        List<List<Vertex<T>>> layers = new ArrayList<>(maxRank + 1);
        for (int i = 0; i <= maxRank; i++) layers.add(new ArrayList<>());

        Set<Vertex<T>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Vertex<T> vertex : graph.vertices) dfsOrder(vertex, layers, visited);
        reindex(layers);
        return layers;
    }

    private static <T> void dfsOrder(Vertex<T> vertex, List<List<Vertex<T>>> layers, Set<Vertex<T>> visited) {
        if (!visited.add(vertex)) return;
        layers.get(vertex.rank).add(vertex);
        for (GraphEdge<T> edge : vertex.outgoing) dfsOrder(edge.head, layers, visited);
    }

    private static <T> List<List<Vertex<T>>> reduceCrossings(List<List<Vertex<T>>> order) {
        int best = crossingCount(order);
        List<List<Vertex<T>>> bestOrder = snapshot(order);
        int stagnant = 0;

        for (int iteration = 0; stagnant < 4; iteration++) {
            boolean moveDown = (iteration & 1) == 0;
            order = orderLayer(order, moveDown);
            transpose(order, moveDown);

            int crossings = crossingCount(order);
            if (crossings < best) {
                best = crossings;
                bestOrder = snapshot(order);
                stagnant = 0;
            } else {
                stagnant++;
            }
        }

        return bestOrder;
    }

    private static <T> List<List<Vertex<T>>> orderLayer(List<List<Vertex<T>>> current, boolean moveDown) {
        List<List<Vertex<T>>> result = snapshot(current);
        Map<Vertex<T>, Integer> positions = positions(current);

        if (moveDown) {
            for (int rank = 1; rank < result.size(); rank++) {
                result.get(rank).sort(Comparator.comparingDouble(v -> barycenter(v, true, positions)));
                reindexLayer(result.get(rank));
            }
        } else {
            for (int rank = result.size() - 2; rank >= 0; rank--) {
                result.get(rank).sort(Comparator.comparingDouble(v -> barycenter(v, false, positions)));
                reindexLayer(result.get(rank));
            }
        }
        return result;
    }

    private static <T> double barycenter(Vertex<T> vertex, boolean moveDown, Map<Vertex<T>, Integer> positions) {
        List<GraphEdge<T>> edges = moveDown ? vertex.incoming : vertex.outgoing;
        if (edges.isEmpty()) return positions.get(vertex);

        double sum = 0;
        for (GraphEdge<T> edge : edges) {
            sum += positions.get(moveDown ? edge.tail : edge.head);
        }
        return sum / edges.size();
    }

    private static <T> void transpose(List<List<Vertex<T>>> layers, boolean moveDown) {
        boolean improved;
        do {
            improved = false;
            if (moveDown) {
                for (int rank = 0; rank < layers.size(); rank++) {
                    improved |= transposeLayer(layers, rank);
                }
            } else {
                for (int rank = layers.size() - 1; rank >= 0; rank--) {
                    improved |= transposeLayer(layers, rank);
                }
            }
        } while (improved);
    }

    private static <T> boolean transposeLayer(List<List<Vertex<T>>> layers, int rank) {
        List<Vertex<T>> layer = layers.get(rank);
        boolean improved = false;

        for (int i = 0; i + 1 < layer.size(); i++) {
            Vertex<T> left = layer.get(i);
            Vertex<T> right = layer.get(i + 1);
            if (crossCountTwoVertices(left, right, layers) > crossCountTwoVertices(right, left, layers)) {
                Collections.swap(layer, i, i + 1);
                reindexLayer(layer);
                improved = true;
            }
        }
        return improved;
    }

    private static <T> int crossCountTwoVertices(Vertex<T> left, Vertex<T> right, List<List<Vertex<T>>> layers) {
        Map<Vertex<T>, Integer> position = positions(layers);
        return pairCrossings(neighbors(left.incoming, true, position), neighbors(right.incoming, true, position)) + pairCrossings(neighbors(left.outgoing, false, position), neighbors(right.outgoing, false, position));
    }

    private static <T> List<Integer> neighbors(List<GraphEdge<T>> edges, boolean tail, Map<Vertex<T>, Integer> position) {
        List<Integer> result = new ArrayList<>(edges.size());
        for (GraphEdge<T> edge : edges) {
            result.add(position.get(tail ? edge.tail : edge.head));
        }
        return result;
    }

    private static int pairCrossings(List<Integer> left, List<Integer> right) {
        if (left.isEmpty() || right.isEmpty()) return 0;
        left.sort(Integer::compareTo);
        right.sort(Integer::compareTo);

        int total = 0;
        int j = 0;
        for (int a : left) {
            while (j < right.size() && right.get(j) < a) j++;
            total += j;
        }
        return total;
    }

    private static <T> int crossingCount(List<List<Vertex<T>>> layers) {
        Map<Vertex<T>, Integer> position = positions(layers);
        int total = 0;

        for (int rank = 0; rank + 1 < layers.size(); rank++) {
            List<int[]> edges = new ArrayList<>();
            for (Vertex<T> vertex : layers.get(rank)) {
                for (GraphEdge<T> edge : vertex.outgoing) {
                    if (edge.head.rank == rank + 1) {
                        edges.add(new int[]{position.get(edge.tail), position.get(edge.head)});
                    }
                }
            }

            for (int i = 0; i < edges.size(); i++) {
                int[] a = edges.get(i);
                for (int j = i + 1; j < edges.size(); j++) {
                    int[] b = edges.get(j);
                    if ((a[0] < b[0] && a[1] > b[1]) || (a[0] > b[0] && a[1] < b[1])) {
                        total++;
                    }
                }
            }
        }
        return total;
    }

    private static <T> List<Map<Vertex<T>, Double>> createLayouts(LayoutGraph<T> graph, List<List<Vertex<T>>> layers) {
        markType1Conflicts(layers);
        List<Map<Vertex<T>, Double>> layouts = new ArrayList<>(4);

        for (int vertical = 0; vertical < 2; vertical++) {
            for (int horizontal = 0; horizontal < 2; horizontal++) {
                resetCoordinates(layers);
                createVerticalAlignments(layers);
                Map<Vertex<T>, Double> layout = horizontalCompaction(layers);

                if (horizontal == 1) {
                    layout.replaceAll((v, x) -> -x);
                }
                layouts.add(layout);

                for (List<Vertex<T>> layer : layers) Collections.reverse(layer);
                reindex(layers);
            }

            reverseGraph(graph);
            Collections.reverse(layers);
            reindex(layers);
        }

        resetCoordinates(layers);
        return layouts;
    }

    private static <T> void resetCoordinates(List<List<Vertex<T>>> layers) {
        for (int rank = 0; rank < layers.size(); rank++) {
            List<Vertex<T>> layer = layers.get(rank);
            for (int pos = 0; pos < layer.size(); pos++) {
                Vertex<T> vertex = layer.get(pos);
                vertex.rank = rank;
                vertex.pos = pos;
                vertex.align = vertex;
                vertex.root = vertex;
                vertex.sink = vertex;
                vertex.shift = Double.POSITIVE_INFINITY;
            }
        }
    }

    private static <T> void markType1Conflicts(List<List<Vertex<T>>> layers) {
        for (int rank = 0; rank + 1 < layers.size(); rank++) {
            List<Vertex<T>> upper = layers.get(rank);
            List<Vertex<T>> lower = layers.get(rank + 1);
            int left = 0;
            int index = 0;

            for (int i = 0; i < lower.size(); i++) {
                Vertex<T> vertex = lower.get(i);
                Vertex<T> inner = innerSegmentUpperNeighbor(vertex);
                int right;
                if (inner != null) {
                    right = inner.pos;
                } else if (i == lower.size() - 1) {
                    right = upper.size();
                } else {
                    continue;
                }

                while (index < i) {
                    Vertex<T> current = lower.get(index++);
                    for (GraphEdge<T> edge : current.incoming) {
                        int p = edge.tail.pos;
                        if (p < left || p > right) edge.type1Conflict = true;
                    }
                }
                left = right;
            }
        }
    }

    private static <T> Vertex<T> innerSegmentUpperNeighbor(Vertex<T> vertex) {
        if (!vertex.dummy) return null;
        for (GraphEdge<T> edge : vertex.incoming) {
            if (edge.tail.dummy) return edge.tail;
        }
        return null;
    }

    private static <T> void createVerticalAlignments(List<List<Vertex<T>>> layers) {
        for (List<Vertex<T>> layer : layers) {
            int r = -1;

            for (Vertex<T> vertex : layer) {
                List<GraphEdge<T>> edges = vertex.incoming.stream().filter(edge -> slack(edge) == 0).sorted(Comparator.comparingInt(edge -> edge.tail.pos)).toList();
                if (edges.isEmpty()) continue;

                int left = (edges.size() - 1) / 2;
                int right = edges.size() / 2;

                if (left != right) {
                    Vertex<T> a = edges.get(left).tail;
                    Vertex<T> b = edges.get(right).tail;
                    if (a.dummy != b.dummy && b.dummy) {
                        int tmp = left;
                        left = right;
                        right = tmp;
                    }
                }

                if (alignWithMedian(vertex, edges.get(left), r)) {
                    r = edges.get(left).tail.pos;
                }
                if (right != left && alignWithMedian(vertex, edges.get(right), r)) {
                    r = edges.get(right).tail.pos;
                }
            }
        }
    }

    private static <T> boolean alignWithMedian(Vertex<T> vertex, GraphEdge<T> edge, int r) {
        Vertex<T> median = edge.tail;
        if (vertex.align != vertex || edge.type1Conflict || r >= median.pos) {
            return false;
        }
        median.align = vertex;
        vertex.root = median.root;
        vertex.align = vertex.root;
        return true;
    }

    private static <T> Map<Vertex<T>, Double> horizontalCompaction(List<List<Vertex<T>>> layers) {
        computeBlockWidths(layers);
        Map<Vertex<T>, Double> x = new IdentityHashMap<>();

        for (List<Vertex<T>> layer : layers) {
            for (Vertex<T> vertex : layer) {
                if (vertex.root == vertex) placeBlock(layers, vertex, x);
            }
        }

        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).isEmpty()) continue;

            Vertex<T> v = layers.get(i).getFirst();
            if (v.sink != v) continue;
            if (v.sink.shift == Double.POSITIVE_INFINITY) v.sink.shift = 0;

            int rank = i;
            int pos = 0;
            do {
                v = layers.get(rank).get(pos);
                while (v.align != v.root) {
                    v = v.align;
                    rank++;
                    if (v.pos > 0) {
                        Vertex<T> u = layers.get(rank).get(v.pos - 1);
                        double gap = (v.blockWidth + u.blockWidth) * 0.5;
                        double distance = x.get(v) - (x.get(u) + gap);
                        u.sink.shift = Math.min(u.sink.shift, v.sink.shift + distance);
                    }
                }

                pos = v.pos + 1;
            } while (pos != layers.get(rank).size() && v.sink == layers.get(rank).get(pos).sink);
        }

        for (Vertex<T> vertex : layers.stream().flatMap(List::stream).toList()) {
            x.put(vertex, x.get(vertex) + vertex.sink.shift);
        }
        return x;
    }

    private static <T> void computeBlockWidths(List<List<Vertex<T>>> layers) {
        Set<Vertex<T>> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        for (List<Vertex<T>> layer : layers) {
            for (Vertex<T> root : layer) {
                if (root.root != root || !seen.add(root)) continue;

                double width = root.width;
                Vertex<T> current = root.align;
                while (current != root) {
                    width = Math.max(width, current.width);
                    seen.add(current);
                    current = current.align;
                }

                do {
                    current.blockWidth = width;
                    current = current.align;
                } while (current != root);
            }
        }
    }

    private static <T> void placeBlock(List<List<Vertex<T>>> layers, Vertex<T> root, Map<Vertex<T>, Double> x) {
        if (x.containsKey(root)) return;

        x.put(root, 0.0);
        Vertex<T> current = root;
        do {
            if (current.pos > 0) {
                Vertex<T> previous = layers.get(current.rank).get(current.pos - 1).root;
                placeBlock(layers, previous, x);

                if (root.sink == root) root.sink = previous.sink;
                if (root.sink == previous.sink) {
                    double gap = (root.blockWidth + previous.blockWidth) * 0.5;
                    x.put(root, Math.max(x.get(root), x.get(previous) + gap));
                }
            }
            current = current.align;
        } while (current != root);

        while (current.align != root) {
            current = current.align;
            x.put(current, x.get(root));
            current.sink = root.sink;
        }
    }

    private static <T> void reverseGraph(LayoutGraph<T> graph) {
        for (Vertex<T> vertex : graph.vertices) {
            List<GraphEdge<T>> incoming = new ArrayList<>(vertex.incoming);
            vertex.incoming.clear();
            vertex.incoming.addAll(vertex.outgoing);
            vertex.outgoing.clear();
            vertex.outgoing.addAll(incoming);
        }

        for (GraphEdge<T> edge : graph.edges) {
            Vertex<T> tail = edge.tail;
            edge.tail = edge.head;
            edge.head = tail;
        }
    }

    private static <T> void alignToSmallestWidth(LayoutGraph<T> graph, List<Map<Vertex<T>, Double>> layouts) {
        int smallest = 0;
        double smallestWidth = Double.POSITIVE_INFINITY;
        double[] min = new double[layouts.size()];
        double[] max = new double[layouts.size()];

        for (int i = 0; i < layouts.size(); i++) {
            min[i] = Double.POSITIVE_INFINITY;
            max[i] = Double.NEGATIVE_INFINITY;
            for (Vertex<T> vertex : graph.vertices) {
                Double coordinate = layouts.get(i).get(vertex);
                if (coordinate == null) continue;
                min[i] = Math.min(min[i], coordinate - vertex.width * 0.5);
                max[i] = Math.max(max[i], coordinate + vertex.width * 0.5);
            }

            double width = max[i] - min[i];
            if (width < smallestWidth) {
                smallestWidth = width;
                smallest = i;
            }
        }

        for (int i = 0; i < layouts.size(); i++) {
            double shift = (i & 1) == 0 ? min[smallest] - min[i] : max[smallest] - max[i];
            for (Map.Entry<Vertex<T>, Double> entry : layouts.get(i).entrySet()) {
                entry.setValue(entry.getValue() + shift);
            }
        }
    }

    private static <T> Map<Vertex<T>, Double> calculateRelativeCoords(List<Map<Vertex<T>, Double>> layouts) {
        Map<Vertex<T>, Double> result = new IdentityHashMap<>();
        for (Vertex<T> vertex : layouts.getFirst().keySet()) {
            double[] values = new double[4];
            for (int i = 0; i < 4; i++) values[i] = layouts.get(i).get(vertex);
            Arrays.sort(values);
            result.put(vertex, (values[1] + values[2]) * 0.5);
        }
        return result;
    }

    private static <T> Map<Integer, Double> rankCoordinates(LayoutGraph<T> graph, List<List<Vertex<T>>> layers, Config config) {
        Map<Integer, Integer> crossingEdges = new HashMap<>();

        for (OriginalEdge<T> edge : graph.originalEdges) {
            int from = edge.path.getFirst().rank;
            int to = edge.path.getLast().rank;
            for (int rank = from; rank < to; rank++) {
                crossingEdges.merge(rank, 1, Integer::sum);
            }
        }

        Map<Integer, Double> result = new HashMap<>();
        double coordinate = 0;
        result.put(0, coordinate);

        for (int rank = 0; rank + 1 < layers.size(); rank++) {
            int edgeCount = crossingEdges.getOrDefault(rank, 0);
            coordinate += config.layerSpacing() * (1 + config.edgeLayerScale() * Math.sqrt(edgeCount));
            result.put(rank + 1, coordinate);
        }
        return result;
    }

    private static <T> Map<Vertex<T>, Integer> positions(List<List<Vertex<T>>> layers) {
        Map<Vertex<T>, Integer> result = new IdentityHashMap<>();
        for (List<Vertex<T>> layer : layers) {
            for (int i = 0; i < layer.size(); i++) {
                result.put(layer.get(i), i);
            }
        }
        return result;
    }

    private static <T> void reindex(List<List<Vertex<T>>> layers) {
        for (List<Vertex<T>> layer : layers) reindexLayer(layer);
    }

    private static <T> void reindexLayer(List<Vertex<T>> layer) {
        for (int i = 0; i < layer.size(); i++) layer.get(i).pos = i;
    }

    private static <T> List<List<Vertex<T>>> snapshot(List<List<Vertex<T>>> layers) {
        List<List<Vertex<T>>> result = new ArrayList<>(layers.size());
        for (List<Vertex<T>> layer : layers) result.add(new ArrayList<>(layer));
        return result;
    }

    @Override
    public Set<T> nodes() {
        return Collections.unmodifiableSet(positions.keySet());
    }

    @Override
    public List<Edge<T>> edges() {
        return edges;
    }

    @Override
    public Point position(T node) {
        return positions.get(node);
    }

    @Override
    public boolean requireAll(T node) {
        return requirements.getOrDefault(node, true);
    }

    @Override
    public Map<T, ? extends IDependencyData<T>> dependencies() {
        return dependencies;
    }

    @Override
    public Bounds bounds() {
        if (positions.isEmpty()) return new Bounds(0, 0, 0, 0);

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point point : positions.values()) {
            minX = Math.min(minX, point.x());
            minY = Math.min(minY, point.y());
            maxX = Math.max(maxX, point.x());
            maxY = Math.max(maxY, point.y());
        }
        for (Edge<T> edge : edges) {
            for (Point point : edge.points()) {
                minX = Math.min(minX, point.x());
                minY = Math.min(minY, point.y());
                maxX = Math.max(maxX, point.x());
                maxY = Math.max(maxY, point.y());
            }
        }

        return new Bounds(minX, minY, maxX, maxY);
    }

    public record Config(double layerSpacing, double nodeSize, double laneSpacing, double edgeLayerScale, double dummyMargin) {}

    private static final class ComponentLayout<T> {
        final Map<T, Point> positions;
        final List<Edge<T>> edges;
        double minX, minY, maxX, maxY;

        ComponentLayout(Map<T, Point> positions, List<Edge<T>> edges, double minX, double minY, double maxX, double maxY) {
            this.positions = new LinkedHashMap<>(positions);
            this.edges = new ArrayList<>(edges);
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        double width() {
            return maxX - minX;
        }

        double height() {
            return maxY - minY;
        }
    }

    private record Placement<T>(ComponentLayout<T> layout, double x, double y) {
    }

    private record Packing<T>(List<Placement<T>> placements, double width, double height) {
    }

    private static final class Shelf {
        final double y;
        double usedWidth;
        double height;

        Shelf(double y) {
            this.y = y;
        }
    }

    private static final class LayoutGraph<T> {
        final List<Vertex<T>> vertices = new ArrayList<>();
        final List<GraphEdge<T>> edges = new ArrayList<>();
        final List<OriginalEdge<T>> originalEdges = new ArrayList<>();
        final Map<T, Vertex<T>> real = new LinkedHashMap<>();
    }

    private static final class Vertex<T> {
        final int id;
        final T value;
        final boolean dummy;
        final double width;
        final double height;
        final List<GraphEdge<T>> incoming = new ArrayList<>();
        final List<GraphEdge<T>> outgoing = new ArrayList<>();
        int rank;
        int pos;
        Vertex<T> align = this;
        Vertex<T> root = this;
        Vertex<T> sink = this;
        double shift = Double.POSITIVE_INFINITY;
        double blockWidth;

        Vertex(int id, T value, boolean dummy, double width, double height) {
            this.id = id;
            this.value = value;
            this.dummy = dummy;
            this.width = width;
            this.height = height;
        }
    }

    private static final class GraphEdge<T> {
        final OriginalEdge<T> original;
        Vertex<T> tail;
        Vertex<T> head;
        boolean type1Conflict;

        GraphEdge(Vertex<T> tail, Vertex<T> head, OriginalEdge<T> original) {
            this.tail = tail;
            this.head = head;
            this.original = original;
        }
    }

    private static final class OriginalEdge<T> {
        final T source;
        final T target;
        final List<Vertex<T>> path = new ArrayList<>();

        OriginalEdge(T source, T target) {
            this.source = source;
            this.target = target;
        }
    }
}
