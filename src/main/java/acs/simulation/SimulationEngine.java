package acs.simulation;

import acs.allocation.AssignmentResult;
import acs.allocation.ResourceAllocator;
import acs.config.NexoraCity;
import acs.graph.CityGraph;
import acs.model.Emergency;
import acs.model.EmergencyService;
import acs.model.EmergencyStatus;
import acs.model.EmergencyVehicle;
import acs.model.Road;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;
import acs.model.enums.VehicleType;
import acs.routing.Route;
import acs.routing.RouteOptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationEngine {

    private static final double DEFAULT_SERVICE_DURATION = 5.0;

    private static class ActiveResponse {
        private final String emergencyId;
        private final String vehicleId;
        private final Route route;
        private final double arrivalTime;
        private final double resolutionTime;
        private int nextRoadIndex;
        private double nextRoadCompletionTime;

        private ActiveResponse(
                String emergencyId,
                String vehicleId,
                Route route,
                double dispatchTime,
                double arrivalTime,
                double resolutionTime) {

            this.emergencyId = emergencyId;
            this.vehicleId = vehicleId;
            this.route = route;
            this.arrivalTime = arrivalTime;
            this.resolutionTime = resolutionTime;
            this.nextRoadIndex = 0;
            this.nextRoadCompletionTime = route.getRoadIds().isEmpty()
                    ? arrivalTime
                    : dispatchTime;
        }
    }

    private final CityGraph cityGraph;
    private final List<Emergency> emergencies;
    private final List<EmergencyVehicle> vehicles;
    private final Map<String, ActiveResponse> activeResponses;

    private final EmergencyService emergencyService;
    private final ResourceAllocator resourceAllocator;
    private final RouteOptimizer routeOptimizer;

    private double simulationTime;

    public SimulationEngine() {
        cityGraph = NexoraCity.createCity();

        emergencies = new ArrayList<>();
        vehicles = new ArrayList<>();
        activeResponses = new HashMap<>();

        emergencyService = new EmergencyService();
        resourceAllocator = new ResourceAllocator();
        routeOptimizer = new RouteOptimizer();

        simulationTime = 0.0;

        initializeVehicles();
    }

    private void initializeVehicles() {
        vehicles.add(new EmergencyVehicle(
                "VEH_01", VehicleType.FIRE_ENGINE, "LOC_01"));

        vehicles.add(new EmergencyVehicle(
                "VEH_02", VehicleType.FIRE_ENGINE, "LOC_10"));

        vehicles.add(new EmergencyVehicle(
                "VEH_03", VehicleType.AMBULANCE, "LOC_05"));

        vehicles.add(new EmergencyVehicle(
                "VEH_04", VehicleType.AMBULANCE, "LOC_15"));

        vehicles.add(new EmergencyVehicle(
                "VEH_05", VehicleType.RESCUE_VEHICLE, "LOC_12"));

        vehicles.add(new EmergencyVehicle(
                "VEH_06", VehicleType.RESCUE_VEHICLE, "LOC_20"));
    }

    public Emergency createEmergency(
            CrisisType type,
            String locationId,
            CrisisSeverity severity) {

        if (!cityGraph.containsLocation(locationId)) {
            throw new IllegalArgumentException(
                    "Unknown emergency location: " + locationId);
        }

        Emergency emergency = emergencyService.createEmergency(
                type,
                locationId,
                severity,
                simulationTime
        );

        emergencies.add(emergency);

        return emergency;
    }

    public AssignmentResult allocatePendingEmergencies() {

        AssignmentResult result = resourceAllocator.allocate(
                emergencies,
                vehicles,
                cityGraph,
                routeOptimizer,
                simulationTime
        );

        for (AssignmentResult.Assignment assignment
                : result.assignments()) {

            Emergency emergency = findEmergency(
                    assignment.emergencyId());

            if (emergency == null) {
                throw new IllegalStateException(
                        "Assigned emergency could not be found.");
            }

            emergency.markResponding(simulationTime);

            EmergencyVehicle vehicle = findVehicle(assignment.vehicleId());
            if (vehicle == null) {
                throw new IllegalStateException(
                        "Assigned vehicle could not be found.");
            }

            Route route = routeOptimizer.findRoute(
                    cityGraph,
                    vehicle.getCurrentLocation(),
                    emergency.getLocation()
            ).orElseThrow(() -> new IllegalStateException(
                    "No route exists for an allocated emergency."));

            double arrivalTime = simulationTime
                    + route.getTotalEstimatedTravelTimeMinutes();
            double resolutionTime = arrivalTime + DEFAULT_SERVICE_DURATION;

            ActiveResponse response = new ActiveResponse(
                    emergency.getId(),
                    assignment.vehicleId(),
                    route,
                    simulationTime,
                    arrivalTime,
                    resolutionTime
            );
            if (!route.getRoadIds().isEmpty()) {
                response.nextRoadCompletionTime = simulationTime
                        + getRoadById(route.getRoadIds().get(0))
                        .getEffectiveTravelTimeMinutes();
            }

            activeResponses.put(emergency.getId(), response);
        }

        return result;
    }

    public void advanceTime(double minutes) {

        if (!Double.isFinite(minutes) || minutes <= 0) {
            throw new IllegalArgumentException(
                    "Time advancement must be positive and finite.");
        }

        double targetTime = simulationTime + minutes;

        if (!Double.isFinite(targetTime)) {
            throw new IllegalArgumentException(
                    "Resulting simulation time must be finite.");
        }

        // Automatically assign pending emergencies before advancing.
        allocatePendingEmergencies();

        while (true) {
            double nextEventTime = findNextEventTime(targetTime);

            if (Double.isNaN(nextEventTime)) {
                break;
            }

            // Move the clock to the exact event time, not the final target.
            simulationTime = nextEventTime;

            processEventsAtCurrentTime();

            // Newly available vehicles can now take pending emergencies.
            allocatePendingEmergencies();
        }

        simulationTime = targetTime;

        // Handle any emergencies still waiting at the end of this step.
        allocatePendingEmergencies();
    }

    private double findNextEventTime(double targetTime) {

        double nextEventTime = Double.POSITIVE_INFINITY;

        for (ActiveResponse response : activeResponses.values()) {

            Emergency emergency = findEmergency(
                    response.emergencyId);

            if (emergency == null) {
                throw new IllegalStateException(
                        "Active response has no matching emergency.");
            }

            double eventTime;

            if (emergency.getStatus() == EmergencyStatus.RESPONDING) {
                eventTime = Math.min(
                        response.arrivalTime,
                        response.nextRoadCompletionTime
                );
            } else if (emergency.getStatus() == EmergencyStatus.ON_SCENE) {
                eventTime = response.resolutionTime;
            } else {
                continue;
            }

            if (eventTime <= targetTime
                    && eventTime < nextEventTime) {
                nextEventTime = eventTime;
            }
        }

        return Double.isInfinite(nextEventTime)
                ? Double.NaN
                : nextEventTime;
    }

    private void processEventsAtCurrentTime() {

        for (ActiveResponse response
                : new ArrayList<>(activeResponses.values())) {

            Emergency emergency = findEmergency(
                    response.emergencyId);

            EmergencyVehicle vehicle = findVehicle(
                    response.vehicleId);

            if (emergency == null || vehicle == null) {
                throw new IllegalStateException(
                        "Active response has a missing emergency or vehicle.");
            }

            if (emergency.getStatus() == EmergencyStatus.RESPONDING) {
                while (response.nextRoadIndex < response.route.getRoadIds().size()
                        && response.nextRoadCompletionTime <= simulationTime) {

                    String nextLocation = response.route.getLocationIds()
                            .get(response.nextRoadIndex + 1);
                    vehicle.updateLocation(nextLocation);
                    response.nextRoadIndex++;

                    if (response.nextRoadIndex < response.route.getRoadIds().size()) {
                        Road nextRoad = getRoadById(
                                response.route.getRoadIds()
                                        .get(response.nextRoadIndex));
                        response.nextRoadCompletionTime +=
                                nextRoad.getEffectiveTravelTimeMinutes();
                    } else {
                        response.nextRoadCompletionTime = response.arrivalTime;
                    }
                }

                if (response.arrivalTime <= simulationTime) {
                    // Keep the vehicle and emergency location synchronized at arrival.
                    vehicle.updateLocation(emergency.getLocation());
                    emergency.markArrived(response.arrivalTime);
                    vehicle.markOnScene();
                }
            }

            if (emergency.getStatus() == EmergencyStatus.ON_SCENE
                    && response.resolutionTime <= simulationTime) {

                emergency.resolve(response.resolutionTime);

                vehicle.release();

                activeResponses.remove(emergency.getId());
            }
        }
    }

    private Road getRoadById(String roadId) {
        return cityGraph.getRoads().stream()
                .filter(road -> road.getRoadId().equals(roadId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Route references unknown road: " + roadId));
    }

    private Emergency findEmergency(String emergencyId) {
        for (Emergency emergency : emergencies) {
            if (emergency.getId().equals(emergencyId)) {
                return emergency;
            }
        }

        return null;
    }

    private EmergencyVehicle findVehicle(String vehicleId) {
        for (EmergencyVehicle vehicle : vehicles) {
            if (vehicle.getId().equals(vehicleId)) {
                return vehicle;
            }
        }

        return null;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public CityGraph getCityGraph() {
        return cityGraph;
    }

    public List<Emergency> getEmergencies() {
        return List.copyOf(emergencies);
    }

    public List<EmergencyVehicle> getVehicles() {
        return List.copyOf(vehicles);
    }

    public long getPendingEmergencyCount() {
        return emergencies.stream()
                .filter(e -> e.getStatus() == EmergencyStatus.PENDING)
                .count();
    }

    public long getResolvedEmergencyCount() {
        return emergencies.stream()
                .filter(e -> e.getStatus() == EmergencyStatus.RESOLVED)
                .count();
    }

    public long getRespondingEmergencyCount() {
        return emergencies.stream()
                .filter(e -> e.getStatus() == EmergencyStatus.RESPONDING)
                .count();
    }

    public long getOnSceneEmergencyCount() {
        return emergencies.stream()
                .filter(e -> e.getStatus() == EmergencyStatus.ON_SCENE)
                .count();
    }

    public long getAvailableVehicleCount() {
        return vehicles.stream()
                .filter(EmergencyVehicle::isAvailable)
                .count();
    }
}