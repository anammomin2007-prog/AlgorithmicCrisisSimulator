package acs.analytics;

public class IncidentAnalytics {

    private final int totalIncidents;
    private final int pendingIncidents;
    private final int resolvedIncidents;
    private final int cancelledIncidents;

    public IncidentAnalytics(
            int totalIncidents,
            int pendingIncidents,
            int resolvedIncidents,
            int cancelledIncidents) {

        if (totalIncidents < 0 || pendingIncidents < 0
                || resolvedIncidents < 0 || cancelledIncidents < 0) {
            throw new IllegalArgumentException(
                    "Incident counts cannot be negative.");
        }

        if (pendingIncidents + resolvedIncidents + cancelledIncidents
                > totalIncidents) {
            throw new IllegalArgumentException(
                    "Incident status counts exceed total incidents.");
        }

        this.totalIncidents = totalIncidents;
        this.pendingIncidents = pendingIncidents;
        this.resolvedIncidents = resolvedIncidents;
        this.cancelledIncidents = cancelledIncidents;
    }

    public int getTotalIncidents() {
        return totalIncidents;
    }

    public int getPendingIncidents() {
        return pendingIncidents;
    }

    public int getResolvedIncidents() {
        return resolvedIncidents;
    }

    public int getCancelledIncidents() {
        return cancelledIncidents;
    }

    public int getActiveIncidents() {
        return totalIncidents - resolvedIncidents - cancelledIncidents;
    }
}
