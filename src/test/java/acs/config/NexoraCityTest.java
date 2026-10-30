package acs.config;

import acs.graph.CityGraph;
import acs.model.Road;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NexoraCityTest {

    @Test
    void createsExpectedNumberOfLocationsAndRoads() {
        CityGraph graph = NexoraCity.createCity();

        assertEquals(24, graph.getLocationIds().size());
        assertEquals(38, graph.getRoads().size());
    }

    @Test
    void everyLocationIsReachableFromLoc01() {
        CityGraph graph = NexoraCity.createCity();

        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();

        queue.add("LOC_01");
        visited.add("LOC_01");

        while (!queue.isEmpty()) {
            String current = queue.remove();

            for (Road road : graph.getOutgoingRoads(current)) {
                if (road.isClosed()) {
                    continue;
                }

                String next = road.getToLocationId();

                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }

        assertEquals(
                24,
                visited.size(),
                "Every location should be reachable from LOC_01"
        );
    }
}
