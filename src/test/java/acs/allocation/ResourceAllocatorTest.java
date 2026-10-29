package acs.allocation;

import acs.graph.CityGraph;
import acs.model.Emergency;
import acs.model.EmergencyVehicle;
import acs.model.Location;
import acs.model.Road;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;
import acs.model.enums.TrafficLevel;
import acs.model.enums.VehicleType;
import acs.routing.RouteOptimizer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAllocatorTest {

    private final ResourceAllocator allocator = new ResourceAllocator();
    private final RouteOptimizer routeOptimizer = new RouteOptimizer();

    private CityGraph createConnectedGraph() {
        CityGraph graph = new CityGraph();

        graph.addLocation(new Location("A", "Station"));
        graph.addLocation(new Location("B", "Hospital"));
        graph.addLocation(new Location("C", "Fire Site"));

        graph.addRoad(new Road(
                "R1", "A", "B", 10, TrafficLevel.LOW));

        graph.addRoad(new Road(
                "R2", "A", "C", 5, TrafficLevel.LOW));

        graph.addRoad(new Road(
                "R3", "B", "C", 3, TrafficLevel.LOW));

        return graph;
    }

    @Test
    void shouldAssignFireEmergencyToFireEngine() {
        CityGraph graph = createConnectedGraph();

        Emergency emergency = new Emergency(
                CrisisType.FIRE, "C",
                CrisisSeverity.HIGH, 0);

        EmergencyVehicle fireEngine = new EmergencyVehicle(
                "VEH_01", VehicleType.FIRE_ENGINE, "A");

        EmergencyVehicle ambulance = new EmergencyVehicle(
                "VEH_02", VehicleType.AMBULANCE, "A");

        AssignmentResult result = allocator.allocate(
                List.of(emergency),
                List.of(fireEngine, ambulance),
                graph,
                routeOptimizer,
                5);

        assertEquals(1, result.assignments().size());
        assertEquals(0, result.pending().size());
        assertEquals("VEH_01",
                result.assignments().get(0).vehicleId());
        assertEquals("ASSIGNED", emergency.getStatus().name());
    }

    @Test
    void shouldChooseVehicleWithLowestEta() {
        CityGraph graph = createConnectedGraph();

        Emergency emergency = new Emergency(
                CrisisType.FIRE, "C",
                CrisisSeverity.HIGH, 0);

        EmergencyVehicle fartherVehicle = new EmergencyVehicle(
                "VEH_01", VehicleType.FIRE_ENGINE, "A");

        EmergencyVehicle nearerVehicle = new EmergencyVehicle(
                "VEH_02", VehicleType.FIRE_ENGINE, "B");

        AssignmentResult result = allocator.allocate(
                List.of(emergency),
                List.of(fartherVehicle, nearerVehicle),
                graph,
                routeOptimizer,
                5);

        assertEquals(1, result.assignments().size());
        assertEquals("VEH_02",
                result.assignments().get(0).vehicleId());
    }

    @Test
    void shouldChooseSmallerVehicleIdWhenEtaIsEqual() {
        CityGraph graph = createConnectedGraph();

        Emergency emergency = new Emergency(
                CrisisType.FIRE, "C",
                CrisisSeverity.HIGH, 0);

        EmergencyVehicle vehicleZ = new EmergencyVehicle(
                "VEH_Z", VehicleType.FIRE_ENGINE, "A");

        EmergencyVehicle vehicleA = new EmergencyVehicle(
                "VEH_A", VehicleType.FIRE_ENGINE, "A");

        AssignmentResult result = allocator.allocate(
                List.of(emergency),
                List.of(vehicleZ, vehicleA),
                graph,
                routeOptimizer,
                5);

        assertEquals("VEH_A",
                result.assignments().get(0).vehicleId());
    }

    @Test
    void shouldKeepEmergencyPendingWhenNoCompatibleVehicleExists() {
        CityGraph graph = createConnectedGraph();

        Emergency emergency = new Emergency(
                CrisisType.FIRE, "C",
                CrisisSeverity.HIGH, 0);

        EmergencyVehicle ambulance = new EmergencyVehicle(
                "VEH_02", VehicleType.AMBULANCE, "A");

        AssignmentResult result = allocator.allocate(
                List.of(emergency),
                List.of(ambulance),
                graph,
                routeOptimizer,
                5);

        assertTrue(result.assignments().isEmpty());
        assertEquals(1, result.pending().size());
        assertEquals("PENDING", emergency.getStatus().name());
        assertEquals(
                "No suitable vehicle exists.",
                result.pending().get(0).reason());
    }

    @Test
    void shouldKeepEmergencyPendingWhenNoRouteExists() {
        CityGraph graph = new CityGraph();

        graph.addLocation(new Location("A", "Station"));
        graph.addLocation(new Location("B", "Emergency Site"));

        Emergency emergency = new Emergency(
                CrisisType.FIRE, "B",
                CrisisSeverity.HIGH, 0);

        EmergencyVehicle vehicle = new EmergencyVehicle(
                "VEH_01", VehicleType.FIRE_ENGINE, "A");

        AssignmentResult result = allocator.allocate(
                List.of(emergency),
                List.of(vehicle),
                graph,
                routeOptimizer,
                5);

        assertTrue(result.assignments().isEmpty());
        assertEquals(1, result.pending().size());
        assertEquals(
                "No valid route available.",
                result.pending().get(0).reason());
    }

    @Test
    void shouldNotAssignOneVehicleToTwoEmergencies() {
        CityGraph graph = createConnectedGraph();

        Emergency firstEmergency = new Emergency(
                CrisisType.FIRE, "B",
                CrisisSeverity.HIGH, 0);

        Emergency secondEmergency = new Emergency(
                CrisisType.FIRE, "C",
                CrisisSeverity.MEDIUM, 0);

        EmergencyVehicle vehicle = new EmergencyVehicle(
                "VEH_01", VehicleType.FIRE_ENGINE, "A");

        AssignmentResult result = allocator.allocate(
                List.of(firstEmergency, secondEmergency),
                List.of(vehicle),
                graph,
                routeOptimizer,
                5);

        assertEquals(1, result.assignments().size());
        assertEquals(1, result.pending().size());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void shouldPrioritizeHigherSeverityEmergency() {
        CityGraph graph = createConnectedGraph();

        Emergency lowSeverity = new Emergency(
                CrisisType.FIRE, "B",
                CrisisSeverity.LOW, 0);

        Emergency highSeverity = new Emergency(
                CrisisType.FIRE, "C",
                CrisisSeverity.CRITICAL, 0);

        EmergencyVehicle firstVehicle = new EmergencyVehicle(
                "VEH_01", VehicleType.FIRE_ENGINE, "A");

        EmergencyVehicle secondVehicle = new EmergencyVehicle(
                "VEH_02", VehicleType.FIRE_ENGINE, "A");

        AssignmentResult result = allocator.allocate(
                List.of(lowSeverity, highSeverity),
                List.of(firstVehicle, secondVehicle),
                graph,
                routeOptimizer,
                5);

        assertEquals(2, result.assignments().size());

        assertEquals(
                highSeverity.getId(),
                result.assignments().get(0).emergencyId());

        assertEquals(
                lowSeverity.getId(),
                result.assignments().get(1).emergencyId());
    }

    @Test
    void shouldIncludeWaitingTimeInPriority() {
        CityGraph graph = createConnectedGraph();

        Emergency olderEmergency = new Emergency(
                CrisisType.FIRE, "B",
                CrisisSeverity.MEDIUM, 0);

        Emergency newerEmergency = new Emergency(
                CrisisType.FIRE, "C",
                CrisisSeverity.LOW, 20);

        EmergencyVehicle firstVehicle = new EmergencyVehicle(
                "VEH_01", VehicleType.FIRE_ENGINE, "A");

        EmergencyVehicle secondVehicle = new EmergencyVehicle(
                "VEH_02", VehicleType.FIRE_ENGINE, "A");

        AssignmentResult result = allocator.allocate(
                List.of(newerEmergency, olderEmergency),
                List.of(firstVehicle, secondVehicle),
                graph,
                routeOptimizer,
                30);

        assertEquals(2, result.assignments().size());

        assertEquals(
                olderEmergency.getId(),
                result.assignments().get(0).emergencyId());
    }
}