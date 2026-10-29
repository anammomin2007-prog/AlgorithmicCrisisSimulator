
package acs.simulation;

import acs.model.Emergency;
import acs.model.EmergencyStatus;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyEventSimulatorTest {

    @Test
    void manualGenerationUsesProvidedDetails() {
        EmergencyEventSimulator simulator =
                new EmergencyEventSimulator(123L);

        double time = 10.0;

        Emergency emergency = simulator.generateManualEmergency(
                CrisisType.FIRE,
                "LOC_01",
                CrisisSeverity.HIGH,
                time
        );

        assertEquals(CrisisType.FIRE, emergency.getType());
        assertEquals("LOC_01", emergency.getLocation());
        assertEquals(CrisisSeverity.HIGH, emergency.getSeverity());
        assertEquals(time, emergency.getCreationTime());
        assertEquals(EmergencyStatus.PENDING, emergency.getStatus());
    }

    @Test
    void automaticGenerationCreatesValidEmergency() {
        EmergencyEventSimulator simulator =
                new EmergencyEventSimulator(123L);

        Emergency emergency = simulator.generateEmergency(
                "LOC_02", 20.0);

        assertNotNull(emergency.getId());
        assertNotNull(emergency.getType());
        assertNotNull(emergency.getSeverity());
        assertEquals("LOC_02", emergency.getLocation());
        assertEquals(20.0, emergency.getCreationTime());
        assertEquals(EmergencyStatus.PENDING, emergency.getStatus());
    }

    @Test
    void zeroProbabilityNeverGeneratesEmergency() {
        EmergencyEventSimulator simulator =
                new EmergencyEventSimulator(123L);

        simulator.setGenerationProbability(0.0);

        Emergency emergency = simulator.generateAutomaticEmergency(
                "LOC_03", 30.0);

        assertNull(emergency);
    }

    @Test
    void automaticGenerationRespectsConfiguredInterval() {
        EmergencyEventSimulator simulator =
                new EmergencyEventSimulator(123L);

        simulator.setGenerationIntervalSeconds(10);
        simulator.setGenerationProbability(1.0);

        Emergency first = simulator.checkAutomaticGeneration(
                "LOC_01", 0.0);

        Emergency tooSoon = simulator.checkAutomaticGeneration(
                "LOC_01", 5.0);

        Emergency afterInterval = simulator.checkAutomaticGeneration(
                "LOC_01", 10.0);

        assertNotNull(first);
        assertNull(tooSoon);
        assertNotNull(afterInterval);
    }

    @Test
    void rejectsNullSimulationTime() {
        EmergencyEventSimulator simulator =
                new EmergencyEventSimulator(123L);

        assertThrows(IllegalArgumentException.class, () ->
                simulator.checkAutomaticGeneration("LOC_01", Double.NaN));
    }

    @Test
    void rejectsBackwardsSimulationTime() {
        EmergencyEventSimulator simulator =
                new EmergencyEventSimulator(123L);

        simulator.setGenerationProbability(1.0);
        simulator.checkAutomaticGeneration("LOC_01", 10.0);

        assertThrows(IllegalArgumentException.class, () ->
                simulator.checkAutomaticGeneration("LOC_01", 9.0));
    }
}
