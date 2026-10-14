
package acs.graph;

import acs.model.Road;
import acs.model.enums.TrafficLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CityGraphTest {

    @Test
    void shouldStoreTwoWayRoadAtBothLocations() {
        CityGraph graph = new CityGraph();

        Road road = new Road(
                "RD_01",
                "LOC_01",
                "LOC_02",
                10.0,
                TrafficLevel.HIGH
        );

        graph.addRoad(road);

        assertEquals(
                1,
                graph.getRoadsFrom("LOC_01").size()
        );

        assertEquals(
                1,
                graph.getRoadsFrom("LOC_02").size()
        );

        assertEquals(
                "RD_01",
                graph.getRoadsFrom("LOC_01").get(0).getRoadId()
        );

        assertEquals(
                "RD_01",
                graph.getRoadsFrom("LOC_02").get(0).getRoadId()
        );
    }

    @Test
    void shouldRejectDuplicateRoadIds() {
        CityGraph graph = new CityGraph();

        Road firstRoad = new Road(
                "RD_01", "LOC_01", "LOC_02",
                10.0, TrafficLevel.LOW
        );

        Road duplicateRoad = new Road(
                "RD_01", "LOC_02", "LOC_03",
                5.0, TrafficLevel.MEDIUM
        );

        graph.addRoad(firstRoad);

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addRoad(duplicateRoad)
        );
    }

    @Test
    void shouldRejectNullRoad() {
        CityGraph graph = new CityGraph();

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addRoad(null)
        );
    }

    @Test
    void shouldRejectBlankLocationId() {
        CityGraph graph = new CityGraph();

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addLocation("   ")
        );
    }

    @Test
    void shouldPreserveExistingRoadsWhenLocationIsAddedAgain() {
        CityGraph graph = new CityGraph();

        Road road = new Road(
                "RD_01",
                "LOC_01",
                "LOC_02",
                10.0,
                TrafficLevel.LOW
        );

        graph.addRoad(road);
        graph.addLocation("LOC_01");

        assertEquals(1, graph.getRoadsFrom("LOC_01").size());
        assertTrue(graph.getRoadsFrom("LOC_01").contains(road));
    }

    @Test
    void shouldPreventExternalModificationOfRoadList() {
        CityGraph graph = new CityGraph();

        Road road = new Road(
                "RD_01",
                "LOC_01",
                "LOC_02",
                10.0,
                TrafficLevel.LOW
        );

        graph.addRoad(road);

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getRoadsFrom("LOC_01").clear()
        );

        assertEquals(1, graph.getRoadsFrom("LOC_01").size());
    }

    @Test
    void shouldIdentifyExistingAndUnknownLocations() {
        CityGraph graph = new CityGraph();

        graph.addLocation("LOC_01");

        assertTrue(graph.containsLocation("LOC_01"));
        assertFalse(graph.containsLocation("LOC_99"));
        assertFalse(graph.containsLocation(null));
    }

}
