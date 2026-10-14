package acs.routing;

import acs.model.Road;
import acs.model.enums.TrafficLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteResultTest {

    @Test
    void shouldStoreRouteDetails() {
        Road road = new Road(
                "RD_01",
                "LOC_01",
                "LOC_02",
                10.0,
                TrafficLevel.LOW
        );

        RouteResult result = new RouteResult(
                List.of("LOC_01", "LOC_02"),
                List.of(road),
                10.0,
                true
        );

        assertEquals(
                List.of("LOC_01", "LOC_02"),
                result.getLocationIds()
        );
        assertEquals(List.of(road), result.getRoads());
        assertEquals(10.0, result.getTotalTravelTimeMinutes());
        assertTrue(result.isReachable());
    }

    @Test
    void shouldPreventModificationOfRouteLists() {
        RouteResult result = new RouteResult(
                List.of("LOC_01", "LOC_02"),
                List.of(),
                10.0,
                true
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.getLocationIds().clear()
        );
    }
}
