package acs.analytics;

public class AnalyticsReport {

    private final int totalIncidents;
    private final int resolvedIncidents;
    private final double averageResponseTime;
    private final double vehicleUtilizationPercentage;

    public AnalyticsReport(
            int totalIncidents,
            int resolvedIncidents,
            double averageResponseTime,
            double vehicleUtilizationPercentage) {

        if (totalIncidents < 0 || resolvedIncidents < 0) {
            throw new IllegalArgumentException(
                    "Incident counts cannot be negative.");
        }

        if (resolvedIncidents > totalIncidents) {
            throw new IllegalArgumentException(
                    "Resolved incidents cannot exceed total incidents.");
        }

        if (!Double.isFinite(averageResponseTime)
                || averageResponseTime < 0) {
            throw new IllegalArgumentException(
                    "Average response time must be finite and non-negative.");
        }

        if (!Double.isFinite(vehicleUtilizationPercentage)
                || vehicleUtilizationPercentage < 0
                || vehicleUtilizationPercentage > 100) {
            throw new IllegalArgumentException(
                    "Vehicle utilization must be between 0 and 100.");
        }

        this.totalIncidents = totalIncidents;
        this.resolvedIncidents = resolvedIncidents;
        this.averageResponseTime = averageResponseTime;
        this.vehicleUtilizationPercentage =
                vehicleUtilizationPercentage;
    }

    public int getTotalIncidents() {
        return totalIncidents;
    }

    public int getResolvedIncidents() {
        return resolvedIncidents;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public double getVehicleUtilizationPercentage() {
        return vehicleUtilizationPercentage;
    }

    public void displayReport() {
        System.out.println("===== Nexora Analytics Report =====");
        System.out.println("Total incidents: " + totalIncidents);
        System.out.println("Resolved incidents: " + resolvedIncidents);
        System.out.printf("Average response time: %.2f minutes%n",
                averageResponseTime);
        System.out.printf("Vehicle utilization: %.2f%%%n",
                vehicleUtilizationPercentage);
    }
}
