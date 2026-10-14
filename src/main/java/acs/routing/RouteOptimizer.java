package acs.routing;

import acs.graph.CityGraph;
import acs.model.Road;

import java.util.*;

public class RouteOptimizer {

    public RouteResult findRoute(
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
        PriorityQueue<String> locationsToVisit =
                new PriorityQueue<>(
                        (first, second) ->
                                Double.compare(
                                        distances.get(first),
                                        distances.get(second)
                                )
                );

        locationsToVisit.add(startLocationId);

        while (!locationsToVisit.isEmpty()) {
            String currentLocation = locationsToVisit.poll();

            if (currentLocation.equals(destinationLocationId)) {
                break;
            }

            for (Road road : graph.getRoadsFrom(currentLocation)) {
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
                    locationsToVisit.add(neighbor);
                }

            }
        }

        if (distances.get(destinationLocationId)
                == Double.POSITIVE_INFINITY) {
            return new RouteResult(
                    List.of(),
                    List.of(),
                    Double.POSITIVE_INFINITY,
                    false
            );
        }

        List<String> routeLocations = new java.util.ArrayList<>();
        String current = destinationLocationId;

        while (current != null) {
            routeLocations.add(current);
            current = previous.get(current);
        }

        java.util.Collections.reverse(routeLocations);

        List<Road> routeRoads = new ArrayList<>();
        double totalTravelTime = 0.0;

        for (int i = 1; i < routeLocations.size(); i++) {
            String locationId = routeLocations.get(i);

            Road road = previousRoad.get(locationId);

            routeRoads.add(road);
            totalTravelTime += road.getEffectiveTravelTimeMinutes();
        }


        return new RouteResult(
                routeLocations,
                routeRoads,
                totalTravelTime,
                true
        );
    }
}
