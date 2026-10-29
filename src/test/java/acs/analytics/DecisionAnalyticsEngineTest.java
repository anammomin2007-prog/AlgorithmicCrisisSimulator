package acs.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DecisionAnalyticsEngineTest {

    @Test
    void shouldGenerateReportWithCorrectAverage() {
        DecisionAnalyticsEngine engine =
                new DecisionAnalyticsEngine();

        AnalyticsReport report = engine.generateReport(
                20, 15, 225.0, 10, 75.0
        );

        assertNotNull(report);
        assertEquals(20, report.getTotalIncidents());
        assertEquals(15, report.getResolvedIncidents());
        assertEquals(22.5,
                report.getAverageResponseTime(), 0.001);
        assertEquals(75.0,
                report.getVehicleUtilizationPercentage(), 0.001);
    }

    @Test
    void shouldReturnZeroAverageWhenResponseCountIsZero() {
        DecisionAnalyticsEngine engine =
                new DecisionAnalyticsEngine();

        AnalyticsReport report = engine.generateReport(
                10, 5, 0.0, 0, 60.0
        );

        assertEquals(0.0,
                report.getAverageResponseTime(), 0.001);
    }

    @Test
    void shouldCompareStrategiesCorrectly() {
        DecisionAnalyticsEngine engine =
                new DecisionAnalyticsEngine();

        StrategyComparison strategy = engine.compareStrategies(
                "Priority-Based Response", 15.0, 12, 75.0
        );

        assertNotNull(strategy);
        assertEquals("Priority-Based Response",
                strategy.getStrategyName());
        assertEquals(15.0,
                strategy.getAverageResponseTime(), 0.001);
        assertEquals(12, strategy.getIncidentsResolved());
        assertEquals(75.0,
                strategy.getVehicleUtilizationPercentage(), 0.001);
    }


    @Test
    void shouldSelectStrategyWithFasterResponseAndMoreResolvedIncidents() {
        DecisionAnalyticsEngine engine =
                new DecisionAnalyticsEngine();

        StrategyComparison strategyA = engine.compareStrategies(
                "Strategy A", 15.0, 12, 70.0
        );

        StrategyComparison strategyB = engine.compareStrategies(
                "Strategy B", 8.0, 18, 80.0
        );

        String winner =
                engine.compareTwoStrategies(strategyA, strategyB);

        assertEquals("Strategy B", winner);
    }
}
