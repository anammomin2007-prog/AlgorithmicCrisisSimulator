
package acs.graph;

import acs.model.Location;
import acs.model.Road;
import acs.model.enums.TrafficLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CityGraphTest {

    private Location location(String id) {
        return new Location(id, "Location " + id);
    }

    private CityGraph createGraphWithTwoLocations() {
        CityGraph graph = new CityGraph();
        graph.addLocation(location("A"));
        graph.addLocation(location("B"));
        return graph;
    }

    @Test
    void shouldAddAndRetrieveLocation() {
        CityGraph graph = new CityGraph();
        Location a = location("A");

        graph.addLocation(a);

        assertEquals(a, graph.getLocation("A"));
        assertTrue(graph.containsLocation("A"));
        assertFalse(graph.containsLocation("UNKNOWN"));
    }

    @Test
    void shouldRejectNullLocation() {
        CityGraph graph = new CityGraph();

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addLocation(null)
        );
    }

    @Test
    void shouldRejectDuplicateLocationId() {
        CityGraph graph = new CityGraph();
        graph.addLocation(location("A"));

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addLocation(location("A"))
        );
    }

    @Test
    void shouldAddRoadToBothLocations() {
        CityGraph graph = createGraphWithTwoLocations();
        Road road = new Road(
                "RD_01", "A", "B", 5.0, TrafficLevel.LOW
        );

        graph.addRoad(road);

        assertEquals(List.of(road), graph.getOutgoingRoads("A"));
        assertEquals(List.of(road), graph.getOutgoingRoads("B"));
    }

    @Test
    void shouldRejectDuplicateRoadId() {
        CityGraph graph = createGraphWithTwoLocations();
        graph.addRoad(new Road(
                "RD_01", "A", "B", 5.0, TrafficLevel.LOW
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addRoad(new Road(
                        "RD_01", "A", "B", 7.0, TrafficLevel.MEDIUM
                ))
        );
    }

    @Test
    void shouldRejectNullRoad() {
        CityGraph graph = createGraphWithTwoLocations();

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addRoad(null)
        );
    }

    @Test
    void shouldRejectRoadWithUnknownLocation() {
        CityGraph graph = new CityGraph();
        graph.addLocation(location("A"));

        Road road = new Road(
                "RD_01", "A", "B", 5.0, TrafficLevel.LOW
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addRoad(road)
        );
    }

    @Test
    void shouldReturnEmptyRoadListForUnknownLocation() {
        CityGraph graph = new CityGraph();

        assertTrue(graph.getOutgoingRoads("UNKNOWN").isEmpty());
    }

    @Test
    void shouldReturnImmutableOutgoingRoadList() {
        CityGraph graph = createGraphWithTwoLocations();
        Road road = new Road(
                "RD_01", "A", "B", 5.0, TrafficLevel.LOW
        );
        graph.addRoad(road);

        List<Road> roads = graph.getOutgoingRoads("A");

        assertThrows(
                UnsupportedOperationException.class,
                () -> roads.add(new Road(
                        "RD_02", "A", "B", 3.0, TrafficLevel.LOW
                ))
        );
    }

    @Test
    void shouldReturnAllLocationIds() {
        CityGraph graph = createGraphWithTwoLocations();

        assertEquals(
                java.util.Set.of("A", "B"),
                graph.getLocationIds()
        );
    }

    @Test
    void shouldReturnImmutableLocationIds() {
        CityGraph graph = createGraphWithTwoLocations();

        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getLocationIds().add("C")
        );
    }

    @Test
    void shouldNotModifyGraphWhenRoadHasUnknownLocation() {
        CityGraph graph = new CityGraph();
        graph.addLocation(location("A"));

        Road road = new Road(
                "RD_01", "A", "B", 5.0, TrafficLevel.LOW
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> graph.addRoad(road)
        );

        assertTrue(graph.getOutgoingRoads("A").isEmpty());
        assertFalse(graph.getLocationIds().contains("B"));
    }

}
