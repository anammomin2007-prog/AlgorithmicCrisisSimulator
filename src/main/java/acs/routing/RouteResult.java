package acs.routing;

import acs.model.Road;

import java.util.List;

public class RouteResult {

    private final List<String> locationIds;
    private final List<Road> roads;
    private final double totalTravelTimeMinutes;
    private final boolean reachable;

    public RouteResult(
            List<String> locationIds,
            List<Road> roads,
            double totalTravelTimeMinutes,
            boolean reachable) {

        this.locationIds = List.copyOf(locationIds);
        this.roads = List.copyOf(roads);
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
        this.reachable = reachable;
    }

    public List<String> getLocationIds() {
        return locationIds;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public double getTotalTravelTimeMinutes() {
        return totalTravelTimeMinutes;
    }

    public boolean isReachable() {
        return reachable;
    }
}
