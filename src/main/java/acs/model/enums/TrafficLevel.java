
package acs.model.enums;

public enum TrafficLevel {

    LOW(1.0),
    MEDIUM(1.5),
    HIGH(2.0);

    private final double multiplier;

    TrafficLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
