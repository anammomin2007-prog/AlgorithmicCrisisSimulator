package acs.model;

import acs.model.enums.VehicleStatus;
import acs.model.enums.VehicleType;

public class EmergencyVehicle {

    private final String id;
    private final VehicleType type;
    private String currentLocation;
    private VehicleStatus status;
    private String assignedEmergencyId;

    public EmergencyVehicle(
            String id,
            VehicleType type,
            String currentLocation) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID cannot be blank.");
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Vehicle type is required.");
        }

        if (currentLocation == null || currentLocation.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle location cannot be blank.");
        }

        this.id = id;
        this.type = type;
        this.currentLocation = currentLocation;
        this.status = VehicleStatus.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public String getAssignedEmergencyId() {
        return assignedEmergencyId;
    }

    public boolean isAvailable() {
        return status == VehicleStatus.AVAILABLE;
    }

    public void assignTo(String emergencyId) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "Vehicle is not available.");
        }

        if (emergencyId == null || emergencyId.isBlank()) {
            throw new IllegalArgumentException(
                    "Emergency ID cannot be blank.");
        }

        assignedEmergencyId = emergencyId;
        status = VehicleStatus.RESPONDING;
    }

    public void updateLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle location cannot be blank.");
        }

        currentLocation = location;
    }

    public void markOnScene() {
        if (status != VehicleStatus.RESPONDING) {
            throw new IllegalStateException(
                    "Only responding vehicles can arrive on scene.");
        }

        status = VehicleStatus.ON_SCENE;
    }

    /** Marks a vehicle as travelling again after on-scene service. */
    public void beginTravel() {
        if (status == VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("An available vehicle cannot begin an assigned journey.");
        }
        status = VehicleStatus.RESPONDING;
    }

    public void release() {
        if (status == VehicleStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Vehicle is already available.");
        }

        assignedEmergencyId = null;
        status = VehicleStatus.AVAILABLE;
    }
}