
package acs.model;

import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyTest {

    @Test
    void newEmergencyHasCorrectDetailsAndPendingStatus() {
        Emergency emergency = new Emergency(
                CrisisType.FIRE, "LOC_01", CrisisSeverity.HIGH, 10.0);

        assertNotNull(emergency.getId());
        assertEquals(CrisisType.FIRE, emergency.getType());
        assertEquals("LOC_01", emergency.getLocation());
        assertEquals(CrisisSeverity.HIGH, emergency.getSeverity());
        assertEquals(10.0, emergency.getCreationTime());
        assertEquals(EmergencyStatus.PENDING, emergency.getStatus());

        assertNull(emergency.getAssignmentTime());
        assertNull(emergency.getArrivalTime());
        assertNull(emergency.getResolutionTime());
        assertNull(emergency.getAssignedVehicleId());
        assertNull(emergency.getPendingReason());
    }

    @Test
    void emergencyCanCompleteFullLifecycle() {
        Emergency emergency = new Emergency(
                CrisisType.FIRE, "LOC_01", CrisisSeverity.HIGH, 10.0);

        emergency.assignTo("VEHICLE_01", 12.0);
        assertEquals(EmergencyStatus.ASSIGNED, emergency.getStatus());
        assertEquals(12.0, emergency.getAssignmentTime());
        assertEquals("VEHICLE_01", emergency.getAssignedVehicleId());

        emergency.markResponding(13.0);
        assertEquals(EmergencyStatus.RESPONDING, emergency.getStatus());

        emergency.markArrived(17.0);
        assertEquals(EmergencyStatus.ON_SCENE, emergency.getStatus());
        assertEquals(17.0, emergency.getArrivalTime());

        emergency.resolve(22.0);
        assertEquals(EmergencyStatus.RESOLVED, emergency.getStatus());
        assertEquals(22.0, emergency.getResolutionTime());
    }

    @Test
    void pendingEmergencyCanRecordPendingReason() {
        Emergency emergency = new Emergency(
                CrisisType.ACCIDENT, "LOC_02", CrisisSeverity.MEDIUM, 5.0);

        emergency.setPendingReason("No available vehicle");

        assertEquals("No available vehicle", emergency.getPendingReason());
        assertEquals(EmergencyStatus.PENDING, emergency.getStatus());
    }

    @Test
    void pendingEmergencyCanBeCancelled() {
        Emergency emergency = new Emergency(
                CrisisType.FLOOD, "LOC_03", CrisisSeverity.LOW, 4.0);

        emergency.cancel(8.0);

        assertEquals(EmergencyStatus.CANCELLED, emergency.getStatus());
    }

    @Test
    void cannotAssignEmergencyTwice() {
        Emergency emergency = new Emergency(
                CrisisType.FIRE, "LOC_01", CrisisSeverity.HIGH, 10.0);

        emergency.assignTo("VEHICLE_01", 12.0);

        assertThrows(IllegalStateException.class,
                () -> emergency.assignTo("VEHICLE_02", 13.0));
    }

    @Test
    void rejectsBlankLocation() {
        assertThrows(IllegalArgumentException.class,
                () -> new Emergency(
                        CrisisType.FIRE, " ", CrisisSeverity.HIGH, 10.0));
    }

    @Test
    void rejectsNegativeCreationTime() {
        assertThrows(IllegalArgumentException.class,
                () -> new Emergency(
                        CrisisType.FIRE, "LOC_01", CrisisSeverity.HIGH, -1.0));
    }

    @Test
    void cannotResolveBeforeArriving() {
        Emergency emergency = new Emergency(
                CrisisType.FIRE, "LOC_01", CrisisSeverity.HIGH, 10.0);

        assertThrows(IllegalStateException.class,
                () -> emergency.resolve(15.0));
    }
}