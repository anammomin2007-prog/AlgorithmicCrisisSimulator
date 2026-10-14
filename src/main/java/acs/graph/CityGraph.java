
package acs.graph;

import acs.model.Road;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CityGraph {

    private final Map<String, List<Road>> adjacencyList = new HashMap<>();
    private final Map<String, Road> roadsById = new HashMap<>();


    public void addLocation(String locationId) {
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Location ID cannot be empty."
            );
        }

        adjacencyList.putIfAbsent(locationId, new ArrayList<>());
    }



    public void addRoad(Road road) {
        if (road == null) {
            throw new IllegalArgumentException("Road cannot be null.");
        }

        String roadId = road.getRoadId();

        if (roadsById.containsKey(roadId)) {
            throw new IllegalArgumentException(
                    "Duplicate road ID: " + roadId
            );
        }

        String from = road.getFromLocationId();
        String to = road.getToLocationId();

        addLocation(from);
        addLocation(to);

        adjacencyList.get(from).add(road);
        adjacencyList.get(to).add(road);

        roadsById.put(roadId, road);
    }

    public List<Road> getRoadsFrom(String locationId) {
        return List.copyOf(
                adjacencyList.getOrDefault(locationId, List.of())
        );
    }

    public boolean containsLocation(String locationId) {
        return locationId != null
                && adjacencyList.containsKey(locationId);
    }

    public Set<String> getLocationIds() {
        return Set.copyOf(adjacencyList.keySet());
    }
}
