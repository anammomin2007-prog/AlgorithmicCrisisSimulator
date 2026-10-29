
package acs.simulation;

import acs.model.Emergency;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventGeneratorTest {

    @Test
    void generatesEventWhenAnIntervalHasElapsed() {
        EventGenerator generator = new EventGenerator(42L, 5.0);

        List<Emergency> events = generator.generateDueEvents(0.0, 5.0);

        assertEquals(1, events.size());
        assertEquals(5.0, events.get(0).getCreationTime());
    }

    @Test
    void generatesMultipleEventsWhenMultipleIntervalsHaveElapsed() {
        EventGenerator generator = new EventGenerator(42L, 5.0);

        List<Emergency> events = generator.generateDueEvents(0.0, 15.0);

        assertEquals(3, events.size());
        assertEquals(5.0, events.get(0).getCreationTime());
        assertEquals(10.0, events.get(1).getCreationTime());
        assertEquals(15.0, events.get(2).getCreationTime());
    }

    @Test
    void sameSeedProducesSameEventDetails() {
        EventGenerator first = new EventGenerator(123L, 5.0);
        EventGenerator second = new EventGenerator(123L, 5.0);

        List<Emergency> firstEvents =
                first.generateDueEvents(0.0, 15.0);
        List<Emergency> secondEvents =
                second.generateDueEvents(0.0, 15.0);

        assertEquals(firstEvents.size(), secondEvents.size());

        for (int i = 0; i < firstEvents.size(); i++) {
            Emergency a = firstEvents.get(i);
            Emergency b = secondEvents.get(i);

            assertEquals(a.getType(), b.getType());
            assertEquals(a.getSeverity(), b.getSeverity());
            assertEquals(a.getLocation(), b.getLocation());
            assertEquals(a.getCreationTime(), b.getCreationTime());
        }
    }

    @Test
    void resetRestartsTheSameEventSequence() {
        EventGenerator generator = new EventGenerator(123L, 5.0);

        List<Emergency> original =
                generator.generateDueEvents(0.0, 10.0);

        generator.reset(123L);

        List<Emergency> repeated =
                generator.generateDueEvents(0.0, 10.0);

        assertEquals(original.size(), repeated.size());

        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).getType(),
                    repeated.get(i).getType());
            assertEquals(original.get(i).getSeverity(),
                    repeated.get(i).getSeverity());
            assertEquals(original.get(i).getLocation(),
                    repeated.get(i).getLocation());
            assertEquals(original.get(i).getCreationTime(),
                    repeated.get(i).getCreationTime());
        }
    }

    @Test
    void rejectsBackwardsSimulationTime() {
        EventGenerator generator = new EventGenerator(42L, 5.0);

        assertThrows(IllegalArgumentException.class,
                () -> generator.generateDueEvents(10.0, 5.0));
    }

    @Test
    void rejectsInvalidInterval() {
        assertThrows(IllegalArgumentException.class,
                () -> new EventGenerator(42L, 0.0));
    }

    @Test
    void generatesNoEventBeforeFirstInterval() {
        EventGenerator generator = new EventGenerator(42L, 5.0);

        List<Emergency> events =
                generator.generateDueEvents(0.0, 4.0);

        assertTrue(events.isEmpty());
    }

    @Test
    void generatesEventWhenUpdateEndsAtIntervalBoundary() {
        EventGenerator generator = new EventGenerator(42L, 5.0);

        List<Emergency> events =
                generator.generateDueEvents(1.0, 5.0);

        assertEquals(1, events.size());
        assertEquals(5.0, events.get(0).getCreationTime());
    }

    @Test
    void generatesOnlyNewIntervalsWhenUpdateStartsBetweenBoundaries() {
        EventGenerator generator = new EventGenerator(42L, 5.0);

        List<Emergency> events =
                generator.generateDueEvents(5.0, 12.0);

        assertEquals(1, events.size());
        assertEquals(10.0, events.get(0).getCreationTime());
    }

}
