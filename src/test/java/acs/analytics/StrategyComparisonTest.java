package acs.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StrategyComparisonTest {

    @Test
    void shouldStoreStrategyDetails() {
        StrategyComparison strategy =
                new StrategyComparison(
                        "Priority-Based Response",
                        15.0,
                        12,
                        75.0
                );

        assertEquals("Priority-Based Response",
                strategy.getStrategyName());
        assertEquals(15.0,
                strategy.getAverageResponseTime(), 0.001);
        assertEquals(12,
                strategy.getIncidentsResolved());
        assertEquals(75.0,
                strategy.getVehicleUtilizationPercentage(), 0.001);
    }

    @Test
    void shouldCreateStrategyComparison() {
        StrategyComparison strategy =
                new StrategyComparison("Fastest Response", 10.0, 8, 60.0);

        assertNotNull(strategy);
    }

    @Test
    void shouldRejectEmptyStrategyName() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyComparison("", 10.0, 5, 60.0)
        );
    }

    @Test
    void shouldRejectNegativeResponseTime() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyComparison("Strategy A", -1.0, 5, 60.0)
        );
    }

    @Test
    void shouldRejectNegativeResolvedIncidents() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyComparison("Strategy A", 10.0, -1, 60.0)
        );
    }

    @Test
    void shouldRejectUtilizationAbove100Percent() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new StrategyComparison("Strategy A", 10.0, 5, 120.0)
        );
    }
}
