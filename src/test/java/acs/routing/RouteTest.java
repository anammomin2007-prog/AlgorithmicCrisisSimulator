
package acs.routing;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    @Test
    void shouldStoreRouteDetails() {
        Route route = new Route(
                List.of("LOC_01", "LOC_02", "LOC_03"),
                List.of("RD_01", "RD_02"),
                7.5
        );

        assertEquals(
                List.of("LOC_01", "LOC_02", "LOC_03"),
                route.getLocationIds()
        );
        assertEquals(
                List.of("RD_01", "RD_02"),
                route.getRoadIds()
        );
        assertEquals(
                7.5,
                route.getTotalEstimatedTravelTimeMinutes(),
                0.001
        );
    }

    @Test
    void shouldRejectNullOrEmptyLocations() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Route(null, List.of(), 0.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Route(List.of(), List.of(), 0.0)
        );
    }

    @Test
    void shouldRejectNullRoadList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Route(List.of("LOC_01"), null, 0.0)
        );
    }

    @Test
    void shouldRejectIncorrectNumberOfRoads() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Route(
                        List.of("LOC_01", "LOC_02", "LOC_03"),
                        List.of("RD_01"),
                        5.0
                )
        );
    }

    @Test
    void shouldRejectNegativeOrNonFiniteTravelTime() {
        List<String> locations = List.of("LOC_01");
        List<String> roads = List.of();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Route(locations, roads, -1.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Route(locations, roads, Double.NaN)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Route(locations, roads, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void shouldPreventModificationOfRouteLists() {
        List<String> locations =
                new ArrayList<>(List.of("LOC_01", "LOC_02"));
        List<String> roads =
                new ArrayList<>(List.of("RD_01"));

        Route route = new Route(locations, roads, 4.0);

        // Changing the original lists must not change the route.
        locations.add("LOC_03");
        roads.clear();

        assertEquals(
                List.of("LOC_01", "LOC_02"),
                route.getLocationIds()
        );
        assertEquals(
                List.of("RD_01"),
                route.getRoadIds()
        );

        // Callers must not be able to modify the returned lists.
        assertThrows(
                UnsupportedOperationException.class,
                () -> route.getLocationIds().add("LOC_04")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> route.getRoadIds().clear()
        );
    }
}
