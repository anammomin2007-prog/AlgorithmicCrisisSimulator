package acs.analytics;
public class AnalyticsTest {
    public static void main(String[] args) {

        DecisionAnalyticsEngine engine =
                new DecisionAnalyticsEngine();
        engine.evaluateDecisions();
        AnalyticsReport report = engine.generateReport(
                20,      // Total incidents
                15,       // Resolved incidents
                225.0,   // Total response time in minutes
                15,       // Incidents with response-time data
                80.0     // Vehicle utilization percentage
        );

        report.displayReport();

        System.out.println();

        StrategyComparison strategy = engine.compareStrategies(
                "Priority-Based Response",
                20.0,
                7,
                75.0
        );

        strategy.displayComparison();
    }
}
