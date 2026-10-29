
package acs.model;

import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;

public class EmergencyService {

    public Emergency createEmergency(
            CrisisType type,
            String location,
            CrisisSeverity severity,
            double creationTime) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Emergency type is required.");
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Location ID is required.");
        }

        if (severity == null) {
            throw new IllegalArgumentException(
                    "Emergency severity is required.");
        }

        if (!Double.isFinite(creationTime) || creationTime < 0) {
            throw new IllegalArgumentException(
                    "Creation time must be finite and non-negative.");
        }

        return new Emergency(type, location, severity, creationTime);
    }
}
