import acs.analytics.ResponseTimeMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseTimeMetricsTest {

    @Test
    void shouldCalculateAverageResponseTime() {
        ResponseTimeMetrics metrics =
                new ResponseTimeMetrics(20, 15, 225.0, 10);

        assertEquals(22.5, metrics.getAverageResponseTime(), 0.001);
    }

    @Test
    void shouldReturnZeroWhenNoResponsesAreRecorded() {
        ResponseTimeMetrics metrics =
                new ResponseTimeMetrics(5, 0, 0.0, 0);

        assertEquals(0.0, metrics.getAverageResponseTime(), 0.001);
    }


    @Test
    void shouldRejectNegativeIncidentCounts() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ResponseTimeMetrics(-1, 0, 0.0, 0)
        );
    }

    @Test
    void shouldRejectResolvedIncidentsAboveTotal() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ResponseTimeMetrics(5, 6, 10.0, 5)
        );
    }

    @Test
    void shouldRejectNegativeResponseTime() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ResponseTimeMetrics(5, 2, -10.0, 2)
        );
    }
}
