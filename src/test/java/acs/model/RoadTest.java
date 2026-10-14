
package acs.model;

import acs.model.enums.TrafficLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoadTest {

    @Test
    void shouldCalculateEffectiveTravelTime() {
        Road road = new Road(
                "RD_01",
                "LOC_01",
                "LOC_02",
                10.0,
                TrafficLevel.HIGH
        );

        assertEquals(20.0, road.getEffectiveTravelTimeMinutes());
    }

    @Test
    void shouldAllowRoadToBeClosedAndReopened() {
        Road road = new Road(
                "RD_02",
                "LOC_02",
                "LOC_03",
                5.0,
                TrafficLevel.LOW
        );

        assertFalse(road.isClosed());

        road.setClosed(true);
        assertTrue(road.isClosed());

        road.setClosed(false);
        assertFalse(road.isClosed());
    }

    @Test
    void shouldRejectNegativeTravelTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Road(
                        "RD_03",
                        "LOC_01",
                        "LOC_02",
                        -5.0,
                        TrafficLevel.LOW
                )
        );
    }

    @Test
    void shouldRejectRoadConnectingLocationToItself() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Road(
                        "RD_04",
                        "LOC_01",
                        "LOC_01",
                        5.0,
                        TrafficLevel.LOW
                )
        );
    }

    @Test
    void shouldRejectNullTrafficLevel() {
        Road road = new Road(
                "RD_01",
                "LOC_01",
                "LOC_02",
                10.0,
                TrafficLevel.LOW
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> road.setTrafficLevel(null)
        );
    }

}
