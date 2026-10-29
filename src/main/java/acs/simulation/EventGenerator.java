
package acs.simulation;

import acs.model.Emergency;
import acs.model.EmergencyService;
import acs.model.enums.CrisisSeverity;
import acs.model.enums.CrisisType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventGenerator {

    private final double intervalMinutes;
    private Random random;

    public EventGenerator(long seed, double intervalMinutes) {
        if (!Double.isFinite(intervalMinutes) || intervalMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Interval must be a positive, finite number.");
        }

        this.intervalMinutes = intervalMinutes;
        this.random = new Random(seed);
    }

    public List<Emergency> generateDueEvents(
            double previousSimTime, double currentSimTime) {

        if (!Double.isFinite(previousSimTime)
                || !Double.isFinite(currentSimTime)
                || previousSimTime < 0
                || currentSimTime < previousSimTime) {
            throw new IllegalArgumentException(
                    "Simulation times must be finite, non-negative, and ordered.");
        }

        List<Emergency> events = new ArrayList<>();

        long previousInterval = (long) Math.floor(
                previousSimTime / intervalMinutes);
        long currentInterval = (long) Math.floor(
                currentSimTime / intervalMinutes);

        EmergencyService service = new EmergencyService();

        for (long interval = previousInterval + 1;
             interval <= currentInterval;
             interval++) {

            double eventTime = interval * intervalMinutes;

            CrisisType[] types = CrisisType.values();
            CrisisSeverity[] severities = CrisisSeverity.values();

            CrisisType type = types[random.nextInt(types.length)];
            CrisisSeverity severity =
                    severities[random.nextInt(severities.length)];

            String location = String.format(
                    "LOC_%02d", random.nextInt(24) + 1);

            Emergency emergency = service.createEmergency(
                    type, location, severity, eventTime);

            events.add(emergency);
        }

        return events;
    }

    public void reset(long seed) {
        random = new Random(seed);
    }
}
