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
    private static final double SIMULATION_DURATION_MINUTES = 30.0;

    private enum ResponsePhase {
        TO_INCIDENT, ON_SCENE_SERVICE, TO_HOSPITAL, RETURNING_TO_BASE
    }

    private static class ActiveResponse {
        private final String emergencyId;
        private final String vehicleId;
        private final String homeLocationId;
        private final double dispatchTime;
        private final double plannedDurationMinutes;
        private Route route;
        private ResponsePhase phase;
        private double arrivalTime;
        private double serviceCompletionTime;
        private double routeStartTime;
        private double segmentStartTime;
        private int nextRoadIndex;
        private double nextRoadCompletionTime;

        private ActiveResponse(String emergencyId, String vehicleId,
                               String homeLocationId, Route route,
                               double dispatchTime, double plannedDurationMinutes) {
            this.emergencyId = emergencyId;
            this.vehicleId = vehicleId;
            this.homeLocationId = homeLocationId;
            this.dispatchTime = dispatchTime;
            this.plannedDurationMinutes = plannedDurationMinutes;
            this.phase = ResponsePhase.TO_INCIDENT;
            startRoute(route, dispatchTime);
        }

        private void startRoute(Route nextRoute, double startTime) {
            route = nextRoute;
            nextRoadIndex = 0;
            routeStartTime = startTime;
            segmentStartTime = startTime;
            arrivalTime = startTime + route.getTotalEstimatedTravelTimeMinutes();
            nextRoadCompletionTime = route.getRoadIds().isEmpty()
                    ? arrivalTime
                    : startTime + 0.0;
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
    private double lastCompletedResponseElapsedMinutes;
    private double lastCompletedResponsePlannedMinutes;

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

    /** Restores the city simulation to its initial state. */
    public void reset() {
        emergencies.clear();
        activeResponses.clear();
        vehicles.clear();
        initializeVehicles();
        for (Road road : cityGraph.getRoads()) {
            road.setClosed(false);
            road.setTrafficLevel(acs.model.enums.TrafficLevel.LOW);
        }
        simulationTime = 0.0;
        lastCompletedResponseElapsedMinutes = 0.0;
        lastCompletedResponsePlannedMinutes = 0.0;
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

        lastCompletedResponseElapsedMinutes = 0.0;
        lastCompletedResponsePlannedMinutes = 0.0;
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

            String homeLocation = vehicle.getCurrentLocation();
            double plannedDuration = estimateFullJourneyMinutes(
                    emergency, homeLocation, route);
            ActiveResponse response = new ActiveResponse(
                    emergency.getId(), assignment.vehicleId(),
                    homeLocation, route, simulationTime, plannedDuration);
            if (!route.getRoadIds().isEmpty()) {
                response.nextRoadCompletionTime = simulationTime
                        + getRoadById(route.getRoadIds().get(0)).getEffectiveTravelTimeMinutes();
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

            allocatePendingEmergencies();
        }

        simulationTime = targetTime;
        allocatePendingEmergencies();
    }

    private double findNextEventTime(double targetTime) {
        double nextEventTime = Double.POSITIVE_INFINITY;
        for (ActiveResponse response : activeResponses.values()) {
            double eventTime;
            switch (response.phase) {
                case TO_INCIDENT, TO_HOSPITAL, RETURNING_TO_BASE ->
                        eventTime = response.nextRoadIndex < response.route.getRoadIds().size()
                                ? response.nextRoadCompletionTime : response.arrivalTime;
                case ON_SCENE_SERVICE -> eventTime = response.serviceCompletionTime;
                default -> throw new IllegalStateException("Unknown response phase.");
            }
            if (eventTime <= targetTime && eventTime < nextEventTime) nextEventTime = eventTime;
        }
        return Double.isInfinite(nextEventTime) ? Double.NaN : nextEventTime;
    }

    private void processEventsAtCurrentTime() {
        for (ActiveResponse response : new ArrayList<>(activeResponses.values())) {
            Emergency emergency = findEmergency(response.emergencyId);
            EmergencyVehicle vehicle = findVehicle(response.vehicleId);
            if (emergency == null || vehicle == null) {
                throw new IllegalStateException("Active response has a missing emergency or vehicle.");
            }

            if (response.phase == ResponsePhase.ON_SCENE_SERVICE) {
                if (response.serviceCompletionTime > simulationTime) continue;
                if (emergency.getType() == CrisisType.ACCIDENT) {
                    String hospital = findNearestHospital(emergency.getLocation());
                    if (hospital != null) {
                        Route hospitalRoute = routeOptimizer.findRoute(cityGraph,
                                vehicle.getCurrentLocation(), hospital).orElse(null);
                        if (hospitalRoute != null) {
                            emergency.beginTransport();
                            vehicle.beginTravel();
                            response.phase = ResponsePhase.TO_HOSPITAL;
                            startResponseRoute(response, hospitalRoute);
                            continue;
                        }
                    }
                }
                emergency.resolve(response.serviceCompletionTime);
                beginReturnToBase(response, vehicle);
                continue;
            }

            // Advance the vehicle over every road segment whose completion time has arrived.
            while (response.nextRoadIndex < response.route.getRoadIds().size()
                    && response.nextRoadCompletionTime <= simulationTime) {
                String nextLocation = response.route.getLocationIds().get(response.nextRoadIndex + 1);
                vehicle.updateLocation(nextLocation);
                response.nextRoadIndex++;
                response.segmentStartTime = response.nextRoadCompletionTime;
                if (response.nextRoadIndex < response.route.getRoadIds().size()) {
                    Road nextRoad = getRoadById(response.route.getRoadIds().get(response.nextRoadIndex));
                    response.nextRoadCompletionTime += nextRoad.getEffectiveTravelTimeMinutes();
                } else {
                    // If traffic changed while this route was active, the actual final
                    // road completion is authoritative over the original ETA.
                    response.arrivalTime = simulationTime;
                    response.nextRoadCompletionTime = simulationTime;
                }
            }

            if (response.arrivalTime <= simulationTime
                    && response.nextRoadIndex >= response.route.getRoadIds().size()) {
                switch (response.phase) {
                    case TO_INCIDENT -> {
                        vehicle.updateLocation(emergency.getLocation());
                        emergency.markArrived(response.arrivalTime);
                        vehicle.markOnScene();
                        response.phase = ResponsePhase.ON_SCENE_SERVICE;
                        response.serviceCompletionTime = response.arrivalTime + DEFAULT_SERVICE_DURATION;
                    }
                    case TO_HOSPITAL -> {
                        vehicle.updateLocation(response.route.getLocationIds().get(response.route.getLocationIds().size() - 1));
                        emergency.resolve(simulationTime);
                        beginReturnToBase(response, vehicle);
                    }
                    case RETURNING_TO_BASE -> {
                        vehicle.updateLocation(response.homeLocationId);
                        vehicle.release();
                        lastCompletedResponseElapsedMinutes = Math.max(0.0, simulationTime - response.dispatchTime);
                        lastCompletedResponsePlannedMinutes = response.plannedDurationMinutes;
                        activeResponses.remove(response.emergencyId);
                    }
                    default -> { }
                }
            }
        }
    }

    private void startResponseRoute(ActiveResponse response, Route route) {
        response.startRoute(route, simulationTime);
        if (!route.getRoadIds().isEmpty()) {
            response.nextRoadCompletionTime = simulationTime
                    + getRoadById(route.getRoadIds().get(0)).getEffectiveTravelTimeMinutes();
        }
    }

    private void beginReturnToBase(ActiveResponse response, EmergencyVehicle vehicle) {
        Route returnRoute = routeOptimizer.findRoute(cityGraph,
                vehicle.getCurrentLocation(), response.homeLocationId).orElse(null);
        if (returnRoute == null) {
            // Keep the model honest: if the base is unreachable, don't strand a busy vehicle forever.
            vehicle.release();
            activeResponses.remove(response.emergencyId);
            return;
        }
        vehicle.beginTravel();
        response.phase = ResponsePhase.RETURNING_TO_BASE;
        startResponseRoute(response, returnRoute);
    }

    private String findNearestHospital(String fromLocation) {
        return List.of("LOC_04", "LOC_11").stream()
                .filter(cityGraph::containsLocation)
                .map(id -> new java.util.AbstractMap.SimpleEntry<>(id,
                        routeOptimizer.findRoute(cityGraph, fromLocation, id).orElse(null)))
                .filter(entry -> entry.getValue() != null)
                .min(java.util.Comparator.comparingDouble(entry ->
                        entry.getValue().getTotalEstimatedTravelTimeMinutes()))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
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

    private double estimateFullJourneyMinutes(Emergency emergency,
                                              String homeLocation,
                                              Route outboundRoute) {
        double estimate = outboundRoute.getTotalEstimatedTravelTimeMinutes()
                + DEFAULT_SERVICE_DURATION;
        String fromAfterService = emergency.getLocation();
        if (emergency.getType() == CrisisType.ACCIDENT) {
            String hospital = findNearestHospital(emergency.getLocation());
            if (hospital != null) {
                Route toHospital = routeOptimizer.findRoute(cityGraph,
                        emergency.getLocation(), hospital).orElse(null);
                Route toBase = routeOptimizer.findRoute(cityGraph,
                        hospital, homeLocation).orElse(null);
                if (toHospital != null && toBase != null) {
                    return estimate + toHospital.getTotalEstimatedTravelTimeMinutes()
                            + toBase.getTotalEstimatedTravelTimeMinutes();
                }
            }
        }
        Route returnRoute = routeOptimizer.findRoute(cityGraph,
                fromAfterService, homeLocation).orElse(null);
        if (returnRoute != null) estimate += returnRoute.getTotalEstimatedTravelTimeMinutes();
        return estimate;
    }

    /** Elapsed simulated minutes since the longest currently active full response began. */
    public double getActiveResponseElapsedMinutes() {
        return activeResponses.values().stream()
                .mapToDouble(response -> Math.max(0.0, simulationTime - response.dispatchTime))
                .max().orElse(lastCompletedResponseElapsedMinutes);
    }

    /** Planned full journey duration (outbound, service, any hospital trip, and return). */
    public double getEstimatedFullResponseDurationMinutes() {
        return activeResponses.values().stream()
                .mapToDouble(response -> response.plannedDurationMinutes)
                .max().orElse(lastCompletedResponsePlannedMinutes);
    }

    public record VehicleJourney(String fromLocationId, String toLocationId,
                                 double progress, String activity) { }

    /** Returns the active route for each emergency, including the current leg. */
    public Map<String, List<String>> getActiveRouteLocations() {
        Map<String, List<String>> routes = new HashMap<>();
        activeResponses.forEach((emergencyId, response) ->
                routes.put(emergencyId, response.route.getLocationIds()));
        return Map.copyOf(routes);
    }

    /** Position along the current road segment, used to animate the map marker. */
    public Map<String, VehicleJourney> getVehicleJourneys() {
        Map<String, VehicleJourney> journeys = new HashMap<>();
        for (ActiveResponse response : activeResponses.values()) {
            if (response.phase == ResponsePhase.ON_SCENE_SERVICE
                    || response.route.getRoadIds().isEmpty()
                    || response.nextRoadIndex >= response.route.getRoadIds().size()) continue;
            double segmentEnd = response.nextRoadCompletionTime;
            double span = segmentEnd - response.segmentStartTime;
            double progress = span <= 0 ? 1.0
                    : (simulationTime - response.segmentStartTime) / span;
            progress = Math.max(0.0, Math.min(1.0, progress));
            String from = response.route.getLocationIds().get(response.nextRoadIndex);
            String to = response.route.getLocationIds().get(response.nextRoadIndex + 1);
            String activity = switch (response.phase) {
                case TO_INCIDENT -> "En route to incident";
                case TO_HOSPITAL -> "Transporting patient";
                case RETURNING_TO_BASE -> "Returning to base";
                case ON_SCENE_SERVICE -> "On scene";
            };
            journeys.put(response.vehicleId, new VehicleJourney(from, to, progress, activity));
        }
        return Map.copyOf(journeys);
    }

    /** Human-readable activity for a vehicle currently assigned to a response. */
    public String getVehicleActivity(String vehicleId) {
        return activeResponses.values().stream()
                .filter(response -> response.vehicleId.equals(vehicleId))
                .findFirst()
                .map(response -> switch (response.phase) {
                    case TO_INCIDENT -> "En route to incident";
                    case ON_SCENE_SERVICE -> "On scene";
                    case TO_HOSPITAL -> "Transporting patient";
                    case RETURNING_TO_BASE -> "Returning to base";
                })
                .orElse("Available");
    }

    public double getSimulationTime() {
        return simulationTime;
    }
    public double getSimulationDurationMinutes() {
        return SIMULATION_DURATION_MINUTES;
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

    public double getAverageResponseTimeMinutes() {
        return emergencies.stream()
                .filter(e -> e.getAssignmentTime() != null && e.getArrivalTime() != null)
                .mapToDouble(e -> e.getArrivalTime() - e.getAssignmentTime())
                .average().orElse(Double.NaN);
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
                .filter(e -> e.getStatus() == EmergencyStatus.RESPONDING
                        || e.getStatus() == EmergencyStatus.TRANSPORTING)
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