package acs.model;

public class Location {

    private final String locationId;
    private final String name;

    public Location(String locationId, String name) {
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Location ID cannot be empty."
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Location name cannot be empty."
            );
        }

        this.locationId = locationId;
        this.name = name;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }
}