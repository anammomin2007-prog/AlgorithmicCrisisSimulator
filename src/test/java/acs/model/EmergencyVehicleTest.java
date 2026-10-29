package acs.model;

import acs.model.enums.VehicleStatus;
import acs.model.enums.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyVehicleTest {

    @Test
    void newVehicleShouldBeAvailable() {
        EmergencyVehicle vehicle = new EmergencyVehicle(
                "V01",
                VehicleType.AMBULANCE,
                "LOC_01"
        );

        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertNull(vehicle.getAssignedEmergencyId());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void vehicleShouldBecomeRespondingWhenAssigned() {
        EmergencyVehicle vehicle = new EmergencyVehicle(
                "V01",
                VehicleType.AMBULANCE,
                "LOC_01"
        );

        vehicle.assignTo("E01");

        assertEquals(VehicleStatus.RESPONDING, vehicle.getStatus());
        assertEquals("E01", vehicle.getAssignedEmergencyId());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void vehicleShouldRejectSecondAssignment() {
        EmergencyVehicle vehicle = new EmergencyVehicle(
                "V01",
                VehicleType.AMBULANCE,
                "LOC_01"
        );

        vehicle.assignTo("E01");

        assertThrows(
                IllegalStateException.class,
                () -> vehicle.assignTo("E02")
        );
    }

    @Test
    void respondingVehicleShouldBecomeOnScene() {
        EmergencyVehicle vehicle = new EmergencyVehicle(
                "V01",
                VehicleType.AMBULANCE,
                "LOC_01"
        );

        vehicle.assignTo("E01");
        vehicle.markOnScene();

        assertEquals(VehicleStatus.ON_SCENE, vehicle.getStatus());
    }

    @Test
    void releasingVehicleShouldClearAssignment() {
        EmergencyVehicle vehicle = new EmergencyVehicle(
                "V01",
                VehicleType.AMBULANCE,
                "LOC_01"
        );

        vehicle.assignTo("E01");
        vehicle.release();

        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertNull(vehicle.getAssignedEmergencyId());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void blankVehicleIdShouldBeRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EmergencyVehicle(
                        " ",
                        VehicleType.AMBULANCE,
                        "LOC_01"
                )
        );
    }
}