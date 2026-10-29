package acs.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleUtilizationTest {

    @Test
    void shouldCalculateUtilizationPercentage() {
        VehicleUtilization vehicle =
                new VehicleUtilization("V1", 75.0, 100.0);

        assertEquals(75.0, vehicle.getUtilizationPercentage(), 0.001);
    }

    @Test
    void shouldReturnZeroWhenTotalAvailableTimeIsZero() {
        VehicleUtilization vehicle =
                new VehicleUtilization("V2", 0.0, 0.0);

        assertEquals(0.0, vehicle.getUtilizationPercentage(), 0.001);
    }

    @Test
    void shouldRejectActiveTimeGreaterThanAvailableTime() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleUtilization("V3", 120.0, 100.0));
    }

    @Test
    void shouldRejectEmptyVehicleId() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleUtilization("", 20.0, 100.0));
    }

    @Test
    void shouldRejectNegativeActiveMinutes() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleUtilization("V4", -10.0, 100.0));
    }

    @Test
    void shouldRejectNegativeAvailableMinutes() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleUtilization("V5", 0.0, -100.0));
    }

    @Test
    void shouldRejectInfiniteActiveMinutes() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleUtilization(
                        "V6", Double.POSITIVE_INFINITY, 100.0));
    }

    @Test
    void shouldRejectNaNAvailableMinutes() {
        assertThrows(IllegalArgumentException.class, () ->
                new VehicleUtilization("V7", 0.0, Double.NaN));
    }
}
