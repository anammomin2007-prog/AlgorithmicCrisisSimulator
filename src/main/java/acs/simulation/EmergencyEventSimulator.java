
package acs.simulation;

import acs.model.Emergency;
import acs.model.EmergencyService;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;

import java.util.Random;

public class EmergencyEventSimulator {

    private final EmergencyService emergencyService;
    private final Random random;

    private int generationIntervalSeconds = 10;
    private double generationProbability = 0.5;
    private Double lastGenerationCheckTime;

    public EmergencyEventSimulator(long seed) {
        this.emergencyService = new EmergencyService();
        this.random = new Random(seed);
    }

    public Emergency generateEmergency(
            String locationId,
            double simulationTime) {

        CrisisType[] types = CrisisType.values();
        CrisisSeverity[] severities = CrisisSeverity.values();

        CrisisType type = types[random.nextInt(types.length)];
        CrisisSeverity severity =
                severities[random.nextInt(severities.length)];

        return emergencyService.createEmergency(
                type, locationId, severity, simulationTime);
    }

    public Emergency generateManualEmergency(
            CrisisType type,
            String locationId,
            CrisisSeverity severity,
            double simulationTime) {

        return emergencyService.createEmergency(
                type, locationId, severity, simulationTime);
    }

    public void setGenerationIntervalSeconds(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException(
                    "Generation interval must be positive.");
        }
        this.generationIntervalSeconds = seconds;
    }

    public int getGenerationIntervalSeconds() {
        return generationIntervalSeconds;
    }

    public void setGenerationProbability(double probability) {
        if (!Double.isFinite(probability)
                || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(
                    "Probability must be between 0.0 and 1.0.");
        }
        this.generationProbability = probability;
    }

    public double getGenerationProbability() {
        return generationProbability;
    }

    public Emergency generateAutomaticEmergency(
            String locationId,
            double simulationTime) {

        if (random.nextDouble() >= generationProbability) {
            return null;
        }

        return generateEmergency(locationId, simulationTime);
    }

    public Emergency checkAutomaticGeneration(
            String locationId,
            double simulationTime) {

        if (!Double.isFinite(simulationTime) || simulationTime < 0) {
            throw new IllegalArgumentException(
                    "Simulation time must be finite and non-negative.");
        }

        if (lastGenerationCheckTime != null
                && simulationTime < lastGenerationCheckTime) {
            throw new IllegalArgumentException(
                    "Simulation time cannot move backwards.");
        }

        if (lastGenerationCheckTime == null) {
            lastGenerationCheckTime = simulationTime;
            return generateAutomaticEmergency(locationId, simulationTime);
        }

        if (simulationTime - lastGenerationCheckTime
                < generationIntervalSeconds) {
            return null;
        }

        lastGenerationCheckTime = simulationTime;
        return generateAutomaticEmergency(locationId, simulationTime);
    }
}
