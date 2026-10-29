package acs.analytics;

public class VehicleUtilization {

    private final String vehicleId;
    private final double activeMinutes;
    private final double totalAvailableMinutes;

    public VehicleUtilization(String vehicleId,
                              double activeMinutes,
                              double totalAvailableMinutes) {

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID cannot be empty.");
        }

        if (!Double.isFinite(activeMinutes)
                || !Double.isFinite(totalAvailableMinutes)
                || activeMinutes < 0
                || totalAvailableMinutes < 0) {
            throw new IllegalArgumentException("Minutes cannot be negative.");
        }

        if (activeMinutes > totalAvailableMinutes) {
            throw new IllegalArgumentException(
                    "Active minutes cannot exceed total available minutes.");
        }

        this.vehicleId = vehicleId;
        this.activeMinutes = activeMinutes;
        this.totalAvailableMinutes = totalAvailableMinutes;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public double getActiveMinutes() {
        return activeMinutes;
    }

    public double getTotalAvailableMinutes() {
        return totalAvailableMinutes;
    }

    public double getUtilizationPercentage() {
        if (totalAvailableMinutes == 0) {
            return 0.0;
        }

        return (activeMinutes / totalAvailableMinutes) * 100.0;
    }
}
