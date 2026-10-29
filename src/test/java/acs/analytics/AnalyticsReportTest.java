package acs.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsReportTest {

    @Test
    void shouldStoreReportDetails() {
        AnalyticsReport report =
                new AnalyticsReport(20, 15, 22.5, 75.0);

        assertEquals(20, report.getTotalIncidents());
        assertEquals(15, report.getResolvedIncidents());
        assertEquals(22.5, report.getAverageResponseTime(), 0.001);
        assertEquals(75.0,
                report.getVehicleUtilizationPercentage(), 0.001);
    }

    @Test
    void shouldAllowZeroValues() {
        AnalyticsReport report =
                new AnalyticsReport(0, 0, 0.0, 0.0);

        assertEquals(0, report.getTotalIncidents());
        assertEquals(0, report.getResolvedIncidents());
        assertEquals(0.0, report.getAverageResponseTime(), 0.001);
        assertEquals(0.0,
                report.getVehicleUtilizationPercentage(), 0.001);
    }


    @Test
    void shouldRejectNegativeIncidentCounts() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new AnalyticsReport(-1, 0, 0.0, 0.0)
        );
    }

    @Test
    void shouldRejectResolvedIncidentsAboveTotal() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new AnalyticsReport(5, 6, 10.0, 50.0)
        );
    }

    @Test
    void shouldRejectInvalidVehicleUtilization() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new AnalyticsReport(10, 5, 10.0, 120.0)
        );
    }
}
