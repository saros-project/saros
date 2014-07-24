package de.fu_berlin.inf.dpp.monitoring;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SubProgressMonitorTest {

    private static class TestMonitor extends NullProgressMonitor {

        private int worked;

        @Override
        public void worked(int amount) {
            worked += amount;
        }

        public int getWorked() {
            return worked;
        }

        public void reset() {
            worked = 0;
        }
    }

    private TestMonitor consumer;

    @Before
    public void setup() {
        consumer = new TestMonitor();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullValueAsParentMonitor() {
        new SubProgressMonitor(null, 0);
    }

    @Test
    public void testConsumeOnDoneWithoutCallingWorked() {

        SubProgressMonitor monitor = new SubProgressMonitor(consumer, 100);

        monitor.beginTask("foo", 300);
        monitor.done();

        assertEquals(100, consumer.getWorked());
    }

    @Test
    public void testWorkedScalingLargeInterval() {

        SubProgressMonitor monitor = new SubProgressMonitor(consumer, 100);

        monitor.beginTask("foo", 400);
        monitor.worked(100);
        assertEquals(25, consumer.getWorked());

        monitor.worked(100);
        assertEquals(50, consumer.getWorked());

        monitor.worked(100);
        assertEquals(75, consumer.getWorked());

        monitor.worked(100);
        assertEquals(100, consumer.getWorked());

        /* ------------------------------------ */

        consumer.reset();

        monitor = new SubProgressMonitor(consumer, 100);

        monitor.beginTask("foo", 400);

        for (int i = 0; i < 400; i++) {
            monitor.worked(1);

            if (i == 100)
                assertEquals(25, consumer.getWorked());
            else if (i == 200)
                assertEquals(50, consumer.getWorked());
            if (i == 300)
                assertEquals(75, consumer.getWorked());
        }

        assertEquals(100, consumer.getWorked());
    }

    @Test
    public void testWorkedScalingSmallInterval() {

        SubProgressMonitor monitor = new SubProgressMonitor(consumer, 100);

        monitor.beginTask("foo", 400);

        for (int i = 0; i < 400; i++) {
            monitor.worked(1);

            if (i == 100)
                assertEquals(25, consumer.getWorked());
            else if (i == 200)
                assertEquals(50, consumer.getWorked());
            if (i == 300)
                assertEquals(75, consumer.getWorked());
        }

        assertEquals(100, consumer.getWorked());

        /* ------------------------------------ */

        consumer.reset();

        monitor = new SubProgressMonitor(consumer, 100);

        monitor.beginTask("foo", 4);
        monitor.worked(1);
        assertEquals(25, consumer.getWorked());

        monitor.worked(1);
        assertEquals(50, consumer.getWorked());

        monitor.worked(1);
        assertEquals(75, consumer.getWorked());

        monitor.worked(1);
        assertEquals(100, consumer.getWorked());

    }
}
