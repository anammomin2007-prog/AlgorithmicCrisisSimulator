package acs.model;
import acs.model.enums.TrafficLevel;
public class Road {

    private final String roadId;
    private final String fromLocationId;
    private final String toLocationId;
    private final double baseTravelTimeMinutes;
    private boolean closed;
    private TrafficLevel trafficLevel;


    public Road(String roadId,
                String fromLocationId,
                String toLocationId,
                double baseTravelTimeMinutes,
                TrafficLevel trafficLevel) {

        if (roadId == null || roadId.isBlank()) {
            throw new IllegalArgumentException("Road ID cannot be empty.");
        }

        if (fromLocationId == null || fromLocationId.isBlank()
                || toLocationId == null || toLocationId.isBlank()) {
            throw new IllegalArgumentException("Location IDs cannot be empty.");
        }

        if (fromLocationId.equals(toLocationId)) {
            throw new IllegalArgumentException("A road must connect two different locations.");
        }

        if (!Double.isFinite(baseTravelTimeMinutes)
                || baseTravelTimeMinutes <= 0) {
            throw new IllegalArgumentException("Travel time must be a positive finite number.");
        }

        if (trafficLevel == null) {
            throw new IllegalArgumentException("Traffic level cannot be null.");
        }

        this.roadId = roadId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.baseTravelTimeMinutes = baseTravelTimeMinutes;
        this.trafficLevel = trafficLevel;
        this.closed = false;
    }


    public String getRoadId() {
        return roadId;
    }

    public String getFromLocationId() {
        return fromLocationId;
    }

    public String getToLocationId() {
        return toLocationId;
    }

    public double getBaseTravelTimeMinutes() {
        return baseTravelTimeMinutes;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public TrafficLevel getTrafficLevel() {
        return trafficLevel;
    }

    public void setTrafficLevel(TrafficLevel trafficLevel) {
        if (trafficLevel == null) {
            throw new IllegalArgumentException(
                    "Traffic level cannot be null."
            );
        }

        this.trafficLevel = trafficLevel;
    }

    public double getEffectiveTravelTimeMinutes() {
        return baseTravelTimeMinutes * trafficLevel.getMultiplier();
    }
}
