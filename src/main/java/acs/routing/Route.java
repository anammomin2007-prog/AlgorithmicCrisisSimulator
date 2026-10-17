package acs.routing;

import java.util.List;

public class Route {

    private final List<String> locationIds;
    private final List<String> roadIds;
    private final double totalEstimatedTravelTimeMinutes;

    public Route(
            List<String> locationIds,
            List<String> roadIds,
            double totalEstimatedTravelTimeMinutes) {

        if (locationIds == null || locationIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "A route must contain at least one location."
            );
        }

        if (roadIds == null) {
            throw new IllegalArgumentException(
                    "Road IDs cannot be null."
            );
        }

        if (roadIds.size() != locationIds.size() - 1) {
            throw new IllegalArgumentException(
                    "A route must have one fewer road than locations."
            );
        }

        if (!Double.isFinite(totalEstimatedTravelTimeMinutes)
                || totalEstimatedTravelTimeMinutes < 0) {
            throw new IllegalArgumentException(
                    "Travel time must be a non-negative finite number."
            );
        }

        this.locationIds = List.copyOf(locationIds);
        this.roadIds = List.copyOf(roadIds);
        this.totalEstimatedTravelTimeMinutes =
                totalEstimatedTravelTimeMinutes;
    }

    public List<String> getLocationIds() {
        return locationIds;
    }

    public List<String> getRoadIds() {
        return roadIds;
    }

    public double getTotalEstimatedTravelTimeMinutes() {
        return totalEstimatedTravelTimeMinutes;
    }
}
