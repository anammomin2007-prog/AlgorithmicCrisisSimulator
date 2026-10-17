package acs.graph;

import acs.model.Location;
import acs.model.Road;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CityGraph {

    private final Map<String, Location> locationsById = new HashMap<>();
    private final Map<String, List<Road>> adjacencyList = new HashMap<>();
    private final Map<String, Road> roadsById = new HashMap<>();

    public void addLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                    "Location cannot be null."
            );
        }

        String locationId = location.getLocationId();

        if (locationsById.containsKey(locationId)) {
            throw new IllegalArgumentException(
                    "Duplicate location ID: " + locationId
            );
        }

        locationsById.put(locationId, location);
        adjacencyList.put(locationId, new ArrayList<>());
    }

    public void addRoad(Road road) {
        if (road == null) {
            throw new IllegalArgumentException(
                    "Road cannot be null."
            );
        }

        String roadId = road.getRoadId();

        if (roadsById.containsKey(roadId)) {
            throw new IllegalArgumentException(
                    "Duplicate road ID: " + roadId
            );
        }

        String from = road.getFromLocationId();
        String to = road.getToLocationId();

        ensureLocationExists(from);
        ensureLocationExists(to);

        adjacencyList.get(from).add(road);
        adjacencyList.get(to).add(road);

        roadsById.put(roadId, road);
    }

    private void ensureLocationExists(String locationId) {
        if (!locationsById.containsKey(locationId)) {
            throw new IllegalArgumentException(
                    "Unknown location ID: " + locationId
            );
        }
    }

    public Location getLocation(String locationId) {
        return locationsById.get(locationId);
    }

    public List<Road> getOutgoingRoads(String locationId) {
        return List.copyOf(
                adjacencyList.getOrDefault(locationId, List.of())
        );
    }

    public boolean containsLocation(String locationId) {
        return locationId != null
                && locationsById.containsKey(locationId);
    }

    public Set<String> getLocationIds() {
        return Set.copyOf(locationsById.keySet());
    }
}
