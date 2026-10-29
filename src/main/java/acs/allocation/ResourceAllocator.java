package acs.allocation;

import acs.graph.CityGraph;
import acs.model.Emergency;
import acs.model.EmergencyVehicle;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;
import acs.model.enums.VehicleType;
import acs.routing.Route;
import acs.routing.RouteOptimizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ResourceAllocator {

    private record Candidate(
            Emergency emergency,
            int priority
    ) {
    }

    private record VehicleRoute(
            EmergencyVehicle vehicle,
            double eta
    ) {
    }

    public AssignmentResult allocate(
            List<Emergency> emergencies,
            List<EmergencyVehicle> vehicles,
            CityGraph graph,
            RouteOptimizer routeOptimizer,
            double currentSimulationTime) {

        if (emergencies == null || vehicles == null
                || graph == null || routeOptimizer == null) {
            throw new IllegalArgumentException(
                    "Allocation inputs cannot be null.");
        }

        if (!Double.isFinite(currentSimulationTime)
                || currentSimulationTime < 0) {
            throw new IllegalArgumentException(
                    "Simulation time must be finite and non-negative.");
        }

        List<Candidate> candidates = new ArrayList<>();

        for (Emergency emergency : emergencies) {
            if (emergency == null) {
                throw new IllegalArgumentException(
                        "Emergency list cannot contain null.");
            }

            if (emergency.getCreationTime() > currentSimulationTime) {
                throw new IllegalArgumentException(
                        "An emergency cannot be created in the future.");
            }

            if (emergency.getStatus()
                    != acs.model.EmergencyStatus.PENDING) {
                continue;
            }

            int severityScore =
                    SeverityScorer.scoreOf(
                            emergency.getSeverity().name());

            double waitingMinutes =
                    currentSimulationTime - emergency.getCreationTime();

            int priority = PriorityCalculator.calculatePriority(
                    severityScore,
                    waitingMinutes
            );

            candidates.add(new Candidate(emergency, priority));
        }

        candidates.sort(
                Comparator.comparingInt(Candidate::priority)
                        .reversed()
                        .thenComparingDouble(
                                candidate -> candidate.emergency()
                                        .getCreationTime())
                        .thenComparing(
                                candidate -> candidate.emergency().getId())
        );

        List<AssignmentResult.Assignment> assignments =
                new ArrayList<>();

        List<AssignmentResult.PendingEmergency> pending =
                new ArrayList<>();

        for (Candidate candidate : candidates) {
            Emergency emergency = candidate.emergency();

            VehicleType requiredType =
                    getRequiredVehicleType(emergency.getType());

            VehicleRoute bestRoute = null;

            if (requiredType != null) {
                for (EmergencyVehicle vehicle : vehicles) {
                    if (vehicle == null) {
                        throw new IllegalArgumentException(
                                "Vehicle list cannot contain null.");
                    }

                    if (!vehicle.isAvailable()
                            || vehicle.getType() != requiredType) {
                        continue;
                    }

                    if (!graph.containsLocation(
                            vehicle.getCurrentLocation())
                            || !graph.containsLocation(
                            emergency.getLocation())) {
                        continue;
                    }

                    Optional<Route> route = routeOptimizer.findRoute(
                            graph,
                            vehicle.getCurrentLocation(),
                            emergency.getLocation()
                    );

                    if (route.isEmpty()) {
                        continue;
                    }

                    double eta =
                            route.get()
                                    .getTotalEstimatedTravelTimeMinutes();

                    VehicleRoute vehicleRoute =
                            new VehicleRoute(vehicle, eta);

                    if (bestRoute == null
                            || eta < bestRoute.eta()
                            || (Double.compare(eta, bestRoute.eta()) == 0
                            && vehicle.getId().compareTo(
                            bestRoute.vehicle().getId()) < 0)) {
                        bestRoute = vehicleRoute;
                    }
                }
            }

            if (bestRoute == null) {
                String reason = requiredType == null
                        ? "Unsupported emergency type."
                        : findPendingReason(
                        emergency, vehicles, requiredType, graph, routeOptimizer);

                emergency.setPendingReason(reason);

                pending.add(
                        new AssignmentResult.PendingEmergency(
                                emergency.getId(),
                                reason,
                                candidate.priority()
                        )
                );

                continue;
            }

            EmergencyVehicle vehicle = bestRoute.vehicle();

            vehicle.assignTo(emergency.getId());
            emergency.assignTo(
                    vehicle.getId(),
                    currentSimulationTime
            );

            assignments.add(
                    new AssignmentResult.Assignment(
                            emergency.getId(),
                            vehicle.getId(),
                            bestRoute.eta(),
                            candidate.priority()
                    )
            );
        }

        return new AssignmentResult(assignments, pending);
    }

    private VehicleType getRequiredVehicleType(CrisisType crisisType) {
        return switch (crisisType) {
            case FIRE -> VehicleType.FIRE_ENGINE;
            case ACCIDENT -> VehicleType.AMBULANCE;
            case FLOOD -> VehicleType.RESCUE_VEHICLE;
        };
    }

    private String findPendingReason(
            Emergency emergency,
            List<EmergencyVehicle> vehicles,
            VehicleType requiredType,
            CityGraph graph,
            RouteOptimizer routeOptimizer) {

        boolean suitableVehicleExists = false;
        boolean availableSuitableVehicleExists = false;

        for (EmergencyVehicle vehicle : vehicles) {
            if (vehicle == null) {
                continue;
            }

            if (vehicle.getType() != requiredType) {
                continue;
            }

            suitableVehicleExists = true;

            if (!vehicle.isAvailable()) {
                continue;
            }

            availableSuitableVehicleExists = true;

            if (!graph.containsLocation(vehicle.getCurrentLocation())
                    || !graph.containsLocation(emergency.getLocation())) {
                continue;
            }

            if (routeOptimizer.findRoute(
                    graph,
                    vehicle.getCurrentLocation(),
                    emergency.getLocation()
            ).isPresent()) {
                return "No suitable vehicle could be assigned.";
            }
        }

        if (!suitableVehicleExists) {
            return "No suitable vehicle exists.";
        }

        if (!availableSuitableVehicleExists) {
            return "No available suitable vehicle.";
        }

        return "No valid route available.";
    }
}