
package acs.model;

import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;

import java.util.UUID;

public class Emergency {

    private final String id;
    private final CrisisType type;
    private final String location;
    private final CrisisSeverity severity;
    private final double creationTime;

    private EmergencyStatus status;
    private Double assignmentTime;
    private Double arrivalTime;
    private Double resolutionTime;
    private String assignedVehicleId;
    private String pendingReason;

    public Emergency(
            CrisisType type,
            String location,
            CrisisSeverity severity,
            double creationTime) {

        if (type == null) {
            throw new IllegalArgumentException("Emergency type is required.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location ID is required.");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Emergency severity is required.");
        }
        validateTime(creationTime);

        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.location = location;
        this.severity = severity;
        this.creationTime = creationTime;
        this.status = EmergencyStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public CrisisType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public CrisisSeverity getSeverity() {
        return severity;
    }

    public double getCreationTime() {
        return creationTime;
    }

    public EmergencyStatus getStatus() {
        return status;
    }

    public Double getAssignmentTime() {
        return assignmentTime;
    }

    public Double getArrivalTime() {
        return arrivalTime;
    }

    public Double getResolutionTime() {
        return resolutionTime;
    }

    public String getAssignedVehicleId() {
        return assignedVehicleId;
    }

    public String getPendingReason() {
        return pendingReason;
    }

    public void assignTo(String vehicleId, double time) {
        requireStatus(EmergencyStatus.PENDING,
                "Only pending emergencies can be assigned.");

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID is required.");
        }
        validateTime(time);
        if (time < creationTime) {
            throw new IllegalArgumentException(
                    "Assignment cannot precede creation.");
        }

        assignedVehicleId = vehicleId;
        assignmentTime = time;
        pendingReason = null;
        status = EmergencyStatus.ASSIGNED;
    }

    public void markResponding(double time) {
        requireStatus(EmergencyStatus.ASSIGNED,
                "Only assigned emergencies can start responding.");
        validateTime(time);
        if (time < assignmentTime) {
            throw new IllegalArgumentException(
                    "Response cannot precede assignment.");
        }

        status = EmergencyStatus.RESPONDING;
    }

    public void markArrived(double time) {
        requireStatus(EmergencyStatus.RESPONDING,
                "Only responding emergencies can arrive.");
        validateTime(time);
        if (time < assignmentTime) {
            throw new IllegalArgumentException(
                    "Arrival cannot precede assignment.");
        }

        arrivalTime = time;
        status = EmergencyStatus.ON_SCENE;
    }

    public void resolve(double time) {
        requireStatus(EmergencyStatus.ON_SCENE,
                "Emergency must arrive before it can be resolved.");
        validateTime(time);
        if (time < arrivalTime) {
            throw new IllegalArgumentException(
                    "Resolution cannot precede arrival.");
        }

        resolutionTime = time;
        status = EmergencyStatus.RESOLVED;
    }

    public void cancel(double time) {
        if (status == EmergencyStatus.RESOLVED
                || status == EmergencyStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Resolved or cancelled emergencies cannot be cancelled.");
        }

        validateTime(time);
        if (time < creationTime) {
            throw new IllegalArgumentException(
                    "Cancellation cannot precede creation.");
        }

        status = EmergencyStatus.CANCELLED;
    }

    public void setPendingReason(String reason) {
        requireStatus(EmergencyStatus.PENDING,
                "Pending reason can only be set while pending.");

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Pending reason cannot be blank.");
        }

        pendingReason = reason;
    }

    private void requireStatus(
            EmergencyStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    private static void validateTime(double time) {
        if (!Double.isFinite(time) || time < 0) {
            throw new IllegalArgumentException(
                    "Time must be finite and non-negative.");
        }
    }
}