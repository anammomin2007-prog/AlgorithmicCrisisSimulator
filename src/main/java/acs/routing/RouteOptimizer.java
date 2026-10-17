package acs.routing;

import acs.graph.CityGraph;
import acs.model.Road;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

public class RouteOptimizer {

    private record QueueEntry(String locationId, double distance) {
    }

    public Optional<Route> findRoute(
            CityGraph graph,
            String startLocationId,
            String destinationLocationId) {

        if (graph == null) {
            throw new IllegalArgumentException(
                    "City graph cannot be null."
            );
        }

        if (!graph.containsLocation(startLocationId)) {
            throw new IllegalArgumentException(
                    "Unknown starting location: " + startLocationId
            );
        }

        if (!graph.containsLocation(destinationLocationId)) {
            throw new IllegalArgumentException(
                    "Unknown destination location: " + destinationLocationId
            );
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Map<String, Road> previousRoad = new HashMap<>();

        for (String locationId : graph.getLocationIds()) {
            distances.put(locationId, Double.POSITIVE_INFINITY);
        }

        distances.put(startLocationId, 0.0);

        PriorityQueue<QueueEntry> locationsToVisit =
                new PriorityQueue<>(
                        (first, second) ->
                                Double.compare(
                                        first.distance(),
                                        second.distance()
                                )
                );

        locationsToVisit.add(new QueueEntry(startLocationId, 0.0));

        while (!locationsToVisit.isEmpty()) {
            QueueEntry entry = locationsToVisit.poll();
            String currentLocation = entry.locationId();

            if (Double.compare(
                    entry.distance(),
                    distances.get(currentLocation)
            ) != 0) {
                continue;
            }

            if (currentLocation.equals(destinationLocationId)) {
                break;
            }

            for (Road road : graph.getOutgoingRoads(currentLocation)) {
                if (road.isClosed()) {
                    continue;
                }

                String neighbor;

                if (road.getFromLocationId().equals(currentLocation)) {
                    neighbor = road.getToLocationId();
                } else {
                    neighbor = road.getFromLocationId();
                }

                double newDistance =
                        distances.get(currentLocation)
                                + road.getEffectiveTravelTimeMinutes();

                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentLocation);
                    previousRoad.put(neighbor, road);

                    locationsToVisit.add(
                            new QueueEntry(neighbor, newDistance)
                    );
                }
            }
        }

        if (Double.isInfinite(distances.get(destinationLocationId))) {
            return Optional.empty();
        }

        List<String> routeLocations = new ArrayList<>();
        List<String> routeRoadIds = new ArrayList<>();

        String current = destinationLocationId;

        while (current != null) {
            routeLocations.add(current);

            Road road = previousRoad.get(current);
            if (road != null) {
                routeRoadIds.add(road.getRoadId());
            }

            current = previous.get(current);
        }

        Collections.reverse(routeLocations);
        Collections.reverse(routeRoadIds);

        Route route = new Route(
                routeLocations,
                routeRoadIds,
                distances.get(destinationLocationId)
        );

        return Optional.of(route);
    }
}
