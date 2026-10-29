package acs.analytics;

public class StrategyComparison {

    private final String strategyName;
    private final double averageResponseTime;
    private final int incidentsResolved;
    private final double vehicleUtilizationPercentage;

    public StrategyComparison(
            String strategyName,
            double averageResponseTime,
            int incidentsResolved,
            double vehicleUtilizationPercentage) {

        if (strategyName == null || strategyName.isBlank()) {
            throw new IllegalArgumentException(
                    "Strategy name cannot be empty.");
        }

        if (!Double.isFinite(averageResponseTime)
                || averageResponseTime < 0) {
            throw new IllegalArgumentException(
                    "Average response time must be finite and non-negative.");
        }

        if (incidentsResolved < 0) {
            throw new IllegalArgumentException(
                    "Resolved incidents cannot be negative.");
        }

        if (!Double.isFinite(vehicleUtilizationPercentage)
                || vehicleUtilizationPercentage < 0
                || vehicleUtilizationPercentage > 100) {
            throw new IllegalArgumentException(
                    "Vehicle utilization must be between 0 and 100.");
        }

        this.strategyName = strategyName;
        this.averageResponseTime = averageResponseTime;
        this.incidentsResolved = incidentsResolved;
        this.vehicleUtilizationPercentage =
                vehicleUtilizationPercentage;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public int getIncidentsResolved() {
        return incidentsResolved;
    }

    public double getVehicleUtilizationPercentage() {
        return vehicleUtilizationPercentage;
    }

    public void displayComparison() {
        System.out.println("Strategy: " + strategyName);
        System.out.printf("Average response time: %.2f minutes%n",
                averageResponseTime);
        System.out.println("Incidents resolved: " + incidentsResolved);
        System.out.printf("Vehicle utilization: %.2f%%%n",
                vehicleUtilizationPercentage);
    }
}
