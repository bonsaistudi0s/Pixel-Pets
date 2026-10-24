package com.bonsai.pixelpets.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGraphLayout<T> {

    Set<T> nodes();

    List<Edge<T>> edges();

    Map<T, ? extends IDependencyData<T>> dependencies();

    Point position(T node);

    boolean requireAll(T node);

    Bounds bounds();

    record Point(double x, double y) {}

    record Edge<T>(T source, T target, List<Point> points) {
        public Edge {
            points = List.copyOf(points);
            if (points.size() < 2) {
                throw new IllegalArgumentException(
                        "An edge path must contain at least two points"
                );
            }
        }
    }

    record Bounds(double minX, double minY, double maxX, double maxY) {
        public double width() {
            return maxX - minX;
        }
        public double height() {
            return maxY - minY;
        }
    }

    // Referenced but missing pets shouldn't be an issue since they are processed out
    default boolean dependenciesMet(T node, Set<T> current) {
        IDependencyData<T> data = dependencies().get(node);

        if (data == null || data.prerequisites().isEmpty()) {
            return true;
        }

        Set<T> prerequisites = data.prerequisites();
        return data.requireAll() ? current.containsAll(prerequisites) : prerequisites.stream().anyMatch(current::contains);
    }
}
