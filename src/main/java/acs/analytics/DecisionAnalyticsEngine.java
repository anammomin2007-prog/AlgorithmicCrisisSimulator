package acs.analytics;

public class DecisionAnalyticsEngine {

    public DecisionAnalyticsEngine() {
        // Initializes the Decision Analytics Engine
    }

    public void evaluateDecisions() {
        System.out.println("\n===== Decision Evaluation =====");
        System.out.println(
                "Evaluating emergency response performance...");
        System.out.println(
                "Use generateReport() to calculate report metrics.");
        System.out.println(
                "Use compareTwoStrategies() to compare strategies.");
        System.out.println("Decision evaluation completed.");
    }

    public AnalyticsReport generateReport(
            int totalIncidents,
            int resolvedIncidents,
            double totalResponseTime,
            int responseCount,
            double vehicleUtilizationPercentage) {

        ResponseTimeMetrics metrics = new ResponseTimeMetrics(
                totalIncidents,
                resolvedIncidents,
                totalResponseTime,
                responseCount
        );

        return new AnalyticsReport(
                totalIncidents,
                resolvedIncidents,
                metrics.getAverageResponseTime(),
                vehicleUtilizationPercentage
        );
    }

    public StrategyComparison compareStrategies(
            String strategyName,
            double averageResponseTime,
            int incidentsResolved,
            double vehicleUtilizationPercentage) {

        return new StrategyComparison(
                strategyName,
                averageResponseTime,
                incidentsResolved,
                vehicleUtilizationPercentage
        );
    }

    public String compareTwoStrategies(
            StrategyComparison strategyA,
            StrategyComparison strategyB) {

        if (strategyA == null || strategyB == null) {
            throw new IllegalArgumentException(
                    "Strategies cannot be null.");
        }

        int scoreA = 0;
        int scoreB = 0;

        if (strategyA.getAverageResponseTime()
                < strategyB.getAverageResponseTime()) {
            scoreA++;
        } else if (strategyB.getAverageResponseTime()
                < strategyA.getAverageResponseTime()) {
            scoreB++;
        }

        if (strategyA.getIncidentsResolved()
                > strategyB.getIncidentsResolved()) {
            scoreA++;
        } else if (strategyB.getIncidentsResolved()
                > strategyA.getIncidentsResolved()) {
            scoreB++;
        }

        if (scoreA > scoreB) {
            return strategyA.getStrategyName();
        } else if (scoreB > scoreA) {
            return strategyB.getStrategyName();
        }

        return "Tie";
    }
}
