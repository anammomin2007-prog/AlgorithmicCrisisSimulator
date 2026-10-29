package acs.analytics;

public class ResponseTimeMetrics {

    private final int totalIncidents;
    private final int resolvedIncidents;
    private final double totalResponseTime;
    private final int responseCount;

    // Existing constructor: preserved for compatibility
    public ResponseTimeMetrics(
            int totalIncidents,
            int resolvedIncidents,
            double totalResponseTime) {

        this(totalIncidents, resolvedIncidents,
                totalResponseTime, resolvedIncidents);
    }

    // New constructor: uses the actual number of incidents
    // with recorded response times
    public ResponseTimeMetrics(
            int totalIncidents,
            int resolvedIncidents,
            double totalResponseTime,
            int responseCount) {

        if (totalIncidents < 0 || resolvedIncidents < 0
                || responseCount < 0) {
            throw new IllegalArgumentException(
                    "Incident counts cannot be negative.");
        }

        if (resolvedIncidents > totalIncidents
                || responseCount > totalIncidents) {
            throw new IllegalArgumentException(
                    "Incident counts cannot exceed total incidents.");
        }

        if (!Double.isFinite(totalResponseTime)
                || totalResponseTime < 0) {
            throw new IllegalArgumentException(
                    "Response time must be finite and non-negative.");
        }

        this.totalIncidents = totalIncidents;
        this.resolvedIncidents = resolvedIncidents;
        this.totalResponseTime = totalResponseTime;
        this.responseCount = responseCount;
    }

    public int getTotalIncidents() {
        return totalIncidents;
    }

    public int getResolvedIncidents() {
        return resolvedIncidents;
    }

    public double getTotalResponseTime() {
        return totalResponseTime;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public double getAverageResponseTime() {
        if (responseCount == 0) {
            return 0.0;
        }

        return totalResponseTime / responseCount;
    }
}
