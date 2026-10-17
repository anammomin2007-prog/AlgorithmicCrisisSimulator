package acs.routing;

import acs.graph.CityGraph;
import acs.model.Location;
import acs.model.Road;
import acs.model.enums.TrafficLevel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RouteOptimizerTest {

    private CityGraph createGraph(String... locationIds) {
        CityGraph graph = new CityGraph();

        for (String id : locationIds) {
            graph.addLocation(new Location(id, "Location " + id));
        }

        return graph;
    }

    @Test
    void shouldRejectNullGraph() {
        RouteOptimizer optimizer = new RouteOptimizer();

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizer.findRoute(null, "LOC_01", "LOC_02")
        );
    }

    @Test
    void shouldRejectUnknownStartingLocation() {
        CityGraph graph = createGraph("LOC_01");
        RouteOptimizer optimizer = new RouteOptimizer();

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizer.findRoute(graph, "LOC_99", "LOC_01")
        );
    }

    @Test
    void shouldRejectUnknownDestinationLocation() {
        CityGraph graph = createGraph("LOC_01");
        RouteOptimizer optimizer = new RouteOptimizer();

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizer.findRoute(graph, "LOC_01", "LOC_99")
        );
    }

    @Test
    void shouldFindFastestRoute() {
        CityGraph graph = createGraph("LOC_01", "LOC_02", "LOC_03");

        graph.addRoad(new Road("RD_01", "LOC_01", "LOC_02", 3.0, TrafficLevel.LOW));
        graph.addRoad(new Road("RD_02", "LOC_02", "LOC_03", 4.0, TrafficLevel.LOW));
        graph.addRoad(new Road("RD_03", "LOC_01", "LOC_03", 10.0, TrafficLevel.LOW));

        Optional<Route> result =
                new RouteOptimizer().findRoute(graph, "LOC_01", "LOC_03");

        assertTrue(result.isPresent());
        Route route = result.orElseThrow();

        assertEquals(List.of("LOC_01", "LOC_02", "LOC_03"), route.getLocationIds());
        assertEquals(List.of("RD_01", "RD_02"), route.getRoadIds());
        assertEquals(7.0, route.getTotalEstimatedTravelTimeMinutes(), 0.001);
    }

    @Test
    void shouldChooseFasterRoadWhenParallelRoadsExist() {
        CityGraph graph = createGraph("LOC_01", "LOC_02");

        graph.addRoad(new Road("RD_01", "LOC_01", "LOC_02", 10.0, TrafficLevel.LOW));
        graph.addRoad(new Road("RD_02", "LOC_01", "LOC_02", 3.0, TrafficLevel.LOW));

        Route route = new RouteOptimizer()
                .findRoute(graph, "LOC_01", "LOC_02")
                .orElseThrow();

        assertEquals(3.0, route.getTotalEstimatedTravelTimeMinutes(), 0.001);
        assertEquals(List.of("RD_02"), route.getRoadIds());
    }

    @Test
    void shouldChooseFasterRouteWhenTrafficChanges() {
        CityGraph graph = createGraph("LOC_01", "LOC_02", "LOC_03");

        Road road1 = new Road("RD_01", "LOC_01", "LOC_02", 4.0, TrafficLevel.LOW);
        Road road2 = new Road("RD_02", "LOC_02", "LOC_03", 4.0, TrafficLevel.LOW);
        Road road3 = new Road("RD_03", "LOC_01", "LOC_03", 10.0, TrafficLevel.LOW);

        graph.addRoad(road1);
        graph.addRoad(road2);
        graph.addRoad(road3);

        RouteOptimizer optimizer = new RouteOptimizer();

        Route firstRoute = optimizer.findRoute(graph, "LOC_01", "LOC_03")
                .orElseThrow();

        assertEquals(8.0, firstRoute.getTotalEstimatedTravelTimeMinutes(), 0.001);
        assertEquals(List.of("LOC_01", "LOC_02", "LOC_03"), firstRoute.getLocationIds());

        road1.setTrafficLevel(TrafficLevel.HIGH);
        road2.setTrafficLevel(TrafficLevel.HIGH);

        Route secondRoute = optimizer.findRoute(graph, "LOC_01", "LOC_03")
                .orElseThrow();

        assertEquals(10.0, secondRoute.getTotalEstimatedTravelTimeMinutes(), 0.001);
        assertEquals(List.of("LOC_01", "LOC_03"), secondRoute.getLocationIds());
    }

    @Test
    void shouldAvoidClosedRoadAndFindAlternative() {
        CityGraph graph = createGraph("LOC_01", "LOC_02", "LOC_03");

        Road road1 = new Road("RD_01", "LOC_01", "LOC_02", 3.0, TrafficLevel.LOW);
        Road road2 = new Road("RD_02", "LOC_02", "LOC_03", 3.0, TrafficLevel.LOW);
        Road road3 = new Road("RD_03", "LOC_01", "LOC_03", 8.0, TrafficLevel.LOW);

        graph.addRoad(road1);
        graph.addRoad(road2);
        graph.addRoad(road3);

        road1.setClosed(true);

        Route route = new RouteOptimizer()
                .findRoute(graph, "LOC_01", "LOC_03")
                .orElseThrow();

        assertEquals(8.0, route.getTotalEstimatedTravelTimeMinutes(), 0.001);
        assertEquals(List.of("LOC_01", "LOC_03"), route.getLocationIds());
        assertEquals(List.of("RD_03"), route.getRoadIds());
    }

    @Test
    void shouldReturnEmptyWhenDestinationIsUnreachable() {
        CityGraph graph = createGraph("LOC_01", "LOC_02", "LOC_03");

        Road road = new Road("RD_01", "LOC_01", "LOC_02", 5.0, TrafficLevel.LOW);
        graph.addRoad(road);
        road.setClosed(true);

        Optional<Route> result =
                new RouteOptimizer().findRoute(graph, "LOC_01", "LOC_03");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnZeroTimeWhenStartEqualsDestination() {
        CityGraph graph = createGraph("LOC_01");

        Route route = new RouteOptimizer()
                .findRoute(graph, "LOC_01", "LOC_01")
                .orElseThrow();

        assertEquals(List.of("LOC_01"), route.getLocationIds());
        assertTrue(route.getRoadIds().isEmpty());
        assertEquals(0.0, route.getTotalEstimatedTravelTimeMinutes(), 0.001);
    }

    @Test
    void shouldReorderQueueWhenAnExistingDistanceImproves() {
        CityGraph graph = createGraph("A", "X", "C", "D");

        graph.addRoad(new Road("RD_01", "A", "X", 1.0, TrafficLevel.LOW));
        graph.addRoad(new Road("RD_02", "A", "D", 5.0, TrafficLevel.LOW));
        graph.addRoad(new Road("RD_03", "A", "C", 8.0, TrafficLevel.LOW));
        graph.addRoad(new Road("RD_04", "X", "C", 1.0, TrafficLevel.LOW));
        graph.addRoad(new Road("RD_05", "C", "D", 1.0, TrafficLevel.LOW));

        Route route = new RouteOptimizer()
                .findRoute(graph, "A", "D")
                .orElseThrow();

        assertEquals(List.of("A", "X", "C", "D"), route.getLocationIds());
        assertEquals(List.of("RD_01", "RD_04", "RD_05"), route.getRoadIds());
        assertEquals(3.0, route.getTotalEstimatedTravelTimeMinutes(), 0.001);
    }

    @Test
    void shouldReturnEmptyWhenOnlyConnectingRoadIsClosed() {
        CityGraph graph = createGraph("A", "B");

        Road road = new Road(
                "RD_01", "A", "B", 5.0, TrafficLevel.LOW
        );
        graph.addRoad(road);
        road.setClosed(true);

        Optional<Route> result =
                new RouteOptimizer().findRoute(graph, "A", "B");

        assertTrue(result.isEmpty());
    }

}
