package acs.routing;

import acs.graph.CityGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteOptimizerTest {

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
        CityGraph graph = new CityGraph();
        graph.addLocation("LOC_01");

        RouteOptimizer optimizer = new RouteOptimizer();

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizer.findRoute(graph, "LOC_99", "LOC_01")
        );
    }

    @Test
    void shouldRejectUnknownDestinationLocation() {
        CityGraph graph = new CityGraph();
        graph.addLocation("LOC_01");

        RouteOptimizer optimizer = new RouteOptimizer();

        assertThrows(
                IllegalArgumentException.class,
                () -> optimizer.findRoute(graph, "LOC_01", "LOC_99")
        );
    }

    @Test
    void shouldFindFastestRoute() {
        CityGraph graph = new CityGraph();

        graph.addRoad(new acs.model.Road(
                "RD_01", "LOC_01", "LOC_02",
                3.0, acs.model.enums.TrafficLevel.LOW
        ));

        graph.addRoad(new acs.model.Road(
                "RD_02", "LOC_02", "LOC_03",
                4.0, acs.model.enums.TrafficLevel.LOW
        ));

        graph.addRoad(new acs.model.Road(
                "RD_03", "LOC_01", "LOC_03",
                10.0, acs.model.enums.TrafficLevel.LOW
        ));

        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.findRoute(
                graph, "LOC_01", "LOC_03"
        );

        assertTrue(result.isReachable());
        assertEquals(
                java.util.List.of("LOC_01", "LOC_02", "LOC_03"),
                result.getLocationIds()
        );
        assertEquals(7.0, result.getTotalTravelTimeMinutes());
        assertEquals(2, result.getRoads().size());
    }

    @Test
    void shouldChooseFasterRoadWhenParallelRoadsExist() {
        CityGraph graph = new CityGraph();

        graph.addRoad(new acs.model.Road(
                "RD_01", "LOC_01", "LOC_02", 10.0,
                acs.model.enums.TrafficLevel.LOW));

        graph.addRoad(new acs.model.Road(
                "RD_02", "LOC_01", "LOC_02", 3.0,
                acs.model.enums.TrafficLevel.LOW));

        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.findRoute(
                graph, "LOC_01", "LOC_02");

        assertTrue(result.isReachable());
        assertEquals(3.0, result.getTotalTravelTimeMinutes());
        assertEquals(
                "RD_02",
                result.getRoads().get(0).getRoadId());
    }

    @Test
    void shouldChooseFasterRouteWhenTrafficChanges() {
        CityGraph graph = new CityGraph();

        acs.model.Road road1 = new acs.model.Road(
                "RD_01", "LOC_01", "LOC_02", 4.0,
                acs.model.enums.TrafficLevel.LOW);

        acs.model.Road road2 = new acs.model.Road(
                "RD_02", "LOC_02", "LOC_03", 4.0,
                acs.model.enums.TrafficLevel.LOW);

        acs.model.Road road3 = new acs.model.Road(
                "RD_03", "LOC_01", "LOC_03", 10.0,
                acs.model.enums.TrafficLevel.LOW);

        graph.addRoad(road1);
        graph.addRoad(road2);
        graph.addRoad(road3);

        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult firstResult = optimizer.findRoute(
                graph, "LOC_01", "LOC_03");

        assertEquals(8.0, firstResult.getTotalTravelTimeMinutes());
        assertEquals(
                java.util.List.of("LOC_01", "LOC_02", "LOC_03"),
                firstResult.getLocationIds());

        road1.setTrafficLevel(acs.model.enums.TrafficLevel.HIGH);
        road2.setTrafficLevel(acs.model.enums.TrafficLevel.HIGH);

        RouteResult secondResult = optimizer.findRoute(
                graph, "LOC_01", "LOC_03");

        assertEquals(10.0, secondResult.getTotalTravelTimeMinutes());
        assertEquals(
                java.util.List.of("LOC_01", "LOC_03"),
                secondResult.getLocationIds());
    }

    @Test
    void shouldAvoidClosedRoadAndFindAlternative() {
        CityGraph graph = new CityGraph();

        acs.model.Road road1 = new acs.model.Road(
                "RD_01", "LOC_01", "LOC_02", 3.0,
                acs.model.enums.TrafficLevel.LOW);

        acs.model.Road road2 = new acs.model.Road(
                "RD_02", "LOC_02", "LOC_03", 3.0,
                acs.model.enums.TrafficLevel.LOW);

        acs.model.Road road3 = new acs.model.Road(
                "RD_03", "LOC_01", "LOC_03", 8.0,
                acs.model.enums.TrafficLevel.LOW);

        graph.addRoad(road1);
        graph.addRoad(road2);
        graph.addRoad(road3);

        RouteOptimizer optimizer = new RouteOptimizer();

        road1.setClosed(true);

        RouteResult result = optimizer.findRoute(
                graph, "LOC_01", "LOC_03");

        assertTrue(result.isReachable());
        assertEquals(8.0, result.getTotalTravelTimeMinutes());
        assertEquals(
                java.util.List.of("LOC_01", "LOC_03"),
                result.getLocationIds());
        assertEquals(
                "RD_03",
                result.getRoads().get(0).getRoadId());
    }

    @Test
    void shouldReportUnreachableDestination() {
        CityGraph graph = new CityGraph();

        acs.model.Road road = new acs.model.Road(
                "RD_01", "LOC_01", "LOC_02", 5.0,
                acs.model.enums.TrafficLevel.LOW);

        graph.addRoad(road);
        graph.addLocation("LOC_03");

        road.setClosed(true);

        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.findRoute(
                graph, "LOC_01", "LOC_03");

        assertTrue(!result.isReachable());
        assertTrue(result.getLocationIds().isEmpty());
        assertTrue(result.getRoads().isEmpty());
        assertTrue(Double.isInfinite(
                result.getTotalTravelTimeMinutes()));
    }

    @Test
    void shouldReturnZeroTimeWhenStartEqualsDestination() {
        CityGraph graph = new CityGraph();
        graph.addLocation("LOC_01");

        RouteOptimizer optimizer = new RouteOptimizer();

        RouteResult result = optimizer.findRoute(
                graph, "LOC_01", "LOC_01");

        assertTrue(result.isReachable());
        assertEquals(
                java.util.List.of("LOC_01"),
                result.getLocationIds());
        assertTrue(result.getRoads().isEmpty());
        assertEquals(0.0, result.getTotalTravelTimeMinutes());
    }
}
