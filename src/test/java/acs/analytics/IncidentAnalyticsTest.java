package acs.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentAnalyticsTest {

    @Test
    void shouldStoreIncidentCounts() {
        IncidentAnalytics analytics =
                new IncidentAnalytics(20, 5, 10, 2);

        assertEquals(20, analytics.getTotalIncidents());
        assertEquals(5, analytics.getPendingIncidents());
        assertEquals(10, analytics.getResolvedIncidents());
        assertEquals(2, analytics.getCancelledIncidents());
        assertEquals(8, analytics.getActiveIncidents());
    }

    @Test
    void shouldRejectNegativeCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new IncidentAnalytics(-1, 0, 0, 0));
    }

    @Test
    void shouldRejectCountsGreaterThanTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> new IncidentAnalytics(10, 5, 4, 2));
    }

    @Test
    void shouldAllowZeroIncidentCounts() {
        IncidentAnalytics analytics =
                new IncidentAnalytics(0, 0, 0, 0);

        assertEquals(0, analytics.getTotalIncidents());
        assertEquals(0, analytics.getActiveIncidents());
    }

    @Test
    void shouldAllowAllIncidentsToBeResolved() {
        IncidentAnalytics analytics =
                new IncidentAnalytics(10, 0, 10, 0);

        assertEquals(10, analytics.getResolvedIncidents());
        assertEquals(0, analytics.getActiveIncidents());
    }
}
