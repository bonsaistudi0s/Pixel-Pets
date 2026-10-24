package com.bonsai.pixelpets.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Doesn't test 'requireAll = false'
class LayeredGraphTest {

    @Test
    void unknownElements_warnLog() {
        Map<Character, IDependencyData<Character>> map = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true)
        ));

        assertDoesNotThrow(() -> {
            GraphProcessor.process(map);
        });
    }

    @Test
    void unknownElements_Remove() {
        Map<Character, IDependencyData<Character>> map = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true)
        ));

        Map<Character, IDependencyData<Character>> expected = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of(), true)
        ));

        Map<Character, ? extends IDependencyData<Character>> processed = GraphProcessor.process(map);

        assertEquals(expected, processed);
    }

    @Test
    void cycle_throwException() {
        Map<Character, IDependencyData<Character>> map = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true),
                'B', new DependencyData<>(Set.of('C'), true),
                'C', new DependencyData<>(Set.of('A'), true)
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> GraphProcessor.process(map)
        );

        assertEquals("Dependency cycle: A -> B -> C -> A", exception.getMessage());
    }

    @Test
    void doubleLink_throwException() {
        Map<Character, IDependencyData<Character>> map = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true),
                'B', new DependencyData<>(Set.of('A', 'C'), true),
                'C', new DependencyData<>(Set.of('B'), true)
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> GraphProcessor.process(map)
        );

        assertEquals("Mutual dependencies: A <-> B, B <-> C", exception.getMessage());
    }

    @Test
    void transitivity_reduced1() {
        Map<Character, IDependencyData<Character>> map = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true),
                'B', new DependencyData<>(Set.of('C', 'D'), true),
                'C', new DependencyData<>(Set.of('D'), true),
                'D', new DependencyData<>(Set.of(), true)
        ));

        Map<Character, IDependencyData<Character>> expected = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true),
                'B', new DependencyData<>(Set.of('C'), true),
                'C', new DependencyData<>(Set.of('D'), true),
                'D', new DependencyData<>(Set.of(), true)
        ));

        Map<Character, ? extends IDependencyData<Character>> processed = GraphProcessor.process(map);

        assertEquals(expected, processed);
    }

    @Test
    void disconnected_passes() {
        Map<Character, IDependencyData<Character>> map = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true),
                'C', new DependencyData<>(Set.of('D'), true),
                'B', new DependencyData<>(Set.of(), true),
                'D', new DependencyData<>(Set.of(), true)
        ));

        Map<Character, IDependencyData<Character>> expected = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of('B'), true),
                'C', new DependencyData<>(Set.of('D'), true),
                'B', new DependencyData<>(Set.of(), true),
                'D', new DependencyData<>(Set.of(), true)
        ));

        Map<Character, ? extends IDependencyData<Character>> processed = GraphProcessor.process(map);

        assertEquals(expected, processed);
    }

    @Test
    void transitivity_reduced2() {
        Map<Character, IDependencyData<Character>> map = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of(), true),
                'B', new DependencyData<>(Set.of('A'), true),
                'C', new DependencyData<>(Set.of('A','B'), true),
                'D', new DependencyData<>(Set.of('A','B','C'), true),
                'E', new DependencyData<>(Set.of('A','B','C','D'), true)
        ));

        Map<Character, IDependencyData<Character>> expected = new HashMap<>(Map.of(
                'A', new DependencyData<>(Set.of(), true),
                'B', new DependencyData<>(Set.of('A'), true),
                'C', new DependencyData<>(Set.of('B'), true),
                'D', new DependencyData<>(Set.of('C'), true),
                'E', new DependencyData<>(Set.of('D'), true)
        ));

        Map<Character, ? extends IDependencyData<Character>> processed = GraphProcessor.process(map);

        assertEquals(expected, processed);
    }


}
