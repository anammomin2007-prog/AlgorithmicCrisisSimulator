package acs.simulation;

import acs.allocation.AssignmentResult;
import acs.model.Emergency;
import acs.model.EmergencyVehicle;
import acs.model.Road;
import acs.routing.Route;
import acs.routing.RouteOptimizer;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    @Test
    void engineInitializesWithSixVehicles() {
        SimulationEngine engine = new SimulationEngine();

        assertEquals(6, engine.getVehicles().size());
    }

    @Test
    void engineStartsAtZeroSimulationTime() {
        SimulationEngine engine = new SimulationEngine();

        assertEquals(0.0, engine.getSimulationTime());
    }

    @Test
    void engineCreatesEmergency() {
        SimulationEngine engine = new SimulationEngine();

        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        assertNotNull(emergency);
        assertEquals(1, engine.getEmergencies().size());
        assertEquals(1, engine.getPendingEmergencyCount());
    }

    @Test
    void engineAdvancesSimulationTime() {
        SimulationEngine engine = new SimulationEngine();

        engine.advanceTime(5.0);

        assertEquals(5.0, engine.getSimulationTime());
    }

    @Test
    void engineRejectsInvalidTimeAdvancement() {
        SimulationEngine engine = new SimulationEngine();

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.advanceTime(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.advanceTime(-1)
        );
    }

    @Test
    void engineRejectsUnknownLocation() {
        SimulationEngine engine = new SimulationEngine();

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.createEmergency(
                        CrisisType.FIRE,
                        "INVALID_LOCATION",
                        CrisisSeverity.CRITICAL
                )
        );
    }
    @Test
    void allocationStartsEmergencyResponse() {
        SimulationEngine engine = new SimulationEngine();

        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        engine.allocatePendingEmergencies();

        assertEquals(
                acs.model.EmergencyStatus.RESPONDING,
                emergency.getStatus()
        );
    }

    @Test
    void emergencyEventuallyGetsResolved() {
        SimulationEngine engine = new SimulationEngine();

        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        engine.allocatePendingEmergencies();

        // Advance far enough for travel and service to complete.
        engine.advanceTime(100);

        assertEquals(
                acs.model.EmergencyStatus.RESOLVED,
                emergency.getStatus()
        );

        assertEquals(1, engine.getResolvedEmergencyCount());
    }

    @Test
    void vehicleBecomesAvailableAfterResolution() {
        SimulationEngine engine = new SimulationEngine();

        engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        engine.allocatePendingEmergencies();

        engine.advanceTime(100);

        assertEquals(6, engine.getAvailableVehicleCount());
    }
    @Test
    void advancingTimeAutomaticallyAllocatesPendingEmergencies() {
        SimulationEngine engine = new SimulationEngine();

        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        assertEquals(
                acs.model.EmergencyStatus.PENDING,
                emergency.getStatus()
        );

        engine.advanceTime(0.1);

        assertEquals(
                acs.model.EmergencyStatus.RESPONDING,
                emergency.getStatus()
        );
    }

    @Test
    void largeTimeAdvanceProcessesArrivalAtCorrectTime() {
        SimulationEngine engine = new SimulationEngine();

        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        AssignmentResult result = engine.allocatePendingEmergencies();

        double estimatedArrival = result.assignments().stream()
                .filter(a -> a.emergencyId().equals(emergency.getId()))
                .findFirst()
                .orElseThrow()
                .estimatedArrivalMinutes();

        engine.advanceTime(estimatedArrival + 1.0);

        assertEquals(
                acs.model.EmergencyStatus.ON_SCENE,
                emergency.getStatus()
        );

        assertEquals(
                estimatedArrival,
                emergency.getArrivalTime(),
                0.000001
        );
    }

    @Test
    void largeTimeAdvanceProcessesResolutionAtCorrectTime() {
        SimulationEngine engine = new SimulationEngine();

        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        AssignmentResult result = engine.allocatePendingEmergencies();

        double estimatedArrival = result.assignments().stream()
                .filter(a -> a.emergencyId().equals(emergency.getId()))
                .findFirst()
                .orElseThrow()
                .estimatedArrivalMinutes();

        engine.advanceTime(estimatedArrival + 20.0);

        assertEquals(
                acs.model.EmergencyStatus.RESOLVED,
                emergency.getStatus()
        );

        assertEquals(
                estimatedArrival + 5.0,
                emergency.getResolutionTime(),
                0.000001
        );
    }

    @Test
    void multipleEmergenciesUseOnlyAvailableCompatibleVehicles() {
        SimulationEngine engine = new SimulationEngine();

        Emergency first = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        Emergency second = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_12",
                CrisisSeverity.HIGH
        );

        Emergency third = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_20",
                CrisisSeverity.MEDIUM
        );

        acs.allocation.AssignmentResult result =
                engine.allocatePendingEmergencies();

        // Nexora has two fire engines.
        assertEquals(2, result.assignments().size());
        assertEquals(1, result.pending().size());

        assertEquals(2, engine.getRespondingEmergencyCount());
        assertEquals(1, engine.getPendingEmergencyCount());

        assertNotEquals(
                acs.model.EmergencyStatus.PENDING,
                first.getStatus()
        );

        assertNotEquals(
                acs.model.EmergencyStatus.PENDING,
                second.getStatus()
        );

        assertEquals(
                acs.model.EmergencyStatus.PENDING,
                third.getStatus()
        );
    }

    @Test
    void vehicleIsReusedAfterEmergencyResolution() {
        SimulationEngine engine = new SimulationEngine();

        engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        engine.createEmergency(
                CrisisType.FIRE,
                "LOC_12",
                CrisisSeverity.HIGH
        );

        Emergency third = engine.createEmergency(
                CrisisType.FIRE,
                "LOC_20",
                CrisisSeverity.MEDIUM
        );

        engine.allocatePendingEmergencies();

        // Both fire engines are occupied, so the third incident waits.
        assertEquals(1, engine.getPendingEmergencyCount());

        // The engine should process arrivals, resolutions and reassignment.
        engine.advanceTime(100.0);

        assertEquals(
                acs.model.EmergencyStatus.RESOLVED,
                third.getStatus()
        );

        assertEquals(3, engine.getResolvedEmergencyCount());
        assertEquals(0, engine.getPendingEmergencyCount());
    }

    @Test
    void allVehiclesBecomeAvailableAfterAllEmergenciesResolve() {
        SimulationEngine engine = new SimulationEngine();

        engine.createEmergency(
                CrisisType.FIRE,
                "LOC_08",
                CrisisSeverity.CRITICAL
        );

        engine.createEmergency(
                CrisisType.ACCIDENT,
                "LOC_15",
                CrisisSeverity.HIGH
        );

        engine.createEmergency(
                CrisisType.FLOOD,
                "LOC_20",
                CrisisSeverity.MEDIUM
        );

        engine.advanceTime(100.0);

        assertEquals(3, engine.getResolvedEmergencyCount());
        assertEquals(0, engine.getPendingEmergencyCount());
        assertEquals(6, engine.getAvailableVehicleCount());
    }

    @Test
    void vehicleMovesToIntermediateLocationWhenRoadLegCompletes() {
        SimulationEngine engine = new SimulationEngine();
        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE, "LOC_08", CrisisSeverity.CRITICAL);

        AssignmentResult result = engine.allocatePendingEmergencies();
        AssignmentResult.Assignment assignment = result.assignments().stream()
                .filter(a -> a.emergencyId().equals(emergency.getId()))
                .findFirst().orElseThrow();
        EmergencyVehicle vehicle = engine.getVehicles().stream()
                .filter(v -> v.getId().equals(assignment.vehicleId()))
                .findFirst().orElseThrow();
        Route route = new RouteOptimizer().findRoute(
                engine.getCityGraph(), vehicle.getCurrentLocation(),
                emergency.getLocation()).orElseThrow();

        assertTrue(route.getRoadIds().size() >= 2,
                "Test requires a route with an intermediate location");
        Road firstRoad = engine.getCityGraph().getRoads().stream()
                .filter(road -> road.getRoadId().equals(route.getRoadIds().get(0)))
                .findFirst().orElseThrow();

        engine.advanceTime(firstRoad.getEffectiveTravelTimeMinutes());

        assertEquals(route.getLocationIds().get(1), vehicle.getCurrentLocation());
        assertEquals(acs.model.EmergencyStatus.RESPONDING, emergency.getStatus());
    }

    @Test
    void vehicleReachesEmergencyOnlyAfterAllRoadLegsComplete() {
        SimulationEngine engine = new SimulationEngine();
        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE, "LOC_08", CrisisSeverity.CRITICAL);
        AssignmentResult result = engine.allocatePendingEmergencies();
        double eta = result.assignments().stream()
                .filter(a -> a.emergencyId().equals(emergency.getId()))
                .findFirst().orElseThrow().estimatedArrivalMinutes();

        engine.advanceTime(eta - 0.001);

        assertEquals(acs.model.EmergencyStatus.RESPONDING, emergency.getStatus());
        assertNotEquals(emergency.getLocation(), engine.getVehicles().stream()
                .filter(v -> emergency.getId().equals(v.getAssignedEmergencyId()))
                .findFirst().orElseThrow().getCurrentLocation());

        engine.advanceTime(0.001);

        assertEquals(acs.model.EmergencyStatus.ON_SCENE, emergency.getStatus());
        assertEquals(emergency.getLocation(), engine.getVehicles().stream()
                .filter(v -> emergency.getId().equals(v.getAssignedEmergencyId()))
                .findFirst().orElseThrow().getCurrentLocation());
    }

    @Test
    void assignedRouteDoesNotContainClosedRoads() {
        SimulationEngine engine = new SimulationEngine();
        Road closedRoad = engine.getCityGraph().getRoads().get(0);
        closedRoad.setClosed(true);

        Emergency emergency = engine.createEmergency(
                CrisisType.FIRE, "LOC_08", CrisisSeverity.CRITICAL);
        AssignmentResult result = engine.allocatePendingEmergencies();
        AssignmentResult.Assignment assignment = result.assignments().stream()
                .filter(a -> a.emergencyId().equals(emergency.getId()))
                .findFirst().orElseThrow();
        EmergencyVehicle vehicle = engine.getVehicles().stream()
                .filter(v -> v.getId().equals(assignment.vehicleId()))
                .findFirst().orElseThrow();
        Route route = new RouteOptimizer().findRoute(
                engine.getCityGraph(), vehicle.getCurrentLocation(),
                emergency.getLocation()).orElseThrow();

        assertFalse(route.getRoadIds().contains(closedRoad.getRoadId()));
    }

}
