package de.fu_berlin.inf.dpp.test.util;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

public class SarosTestUtils {

    /**
     * Returns a mock for the given class which is already switched to replay
     */
    public static <T> T createReplayMock(Class<T> clazz) {
        return replay(createMock(clazz));
    }

    public static <T> T replay(T t) {
        EasyMock.replay(t);
        return t;
    }

    public static SubMonitor submonitor() {
        NullProgressMonitor monitor = new NullProgressMonitor();
        return SubMonitor.convert(monitor);
    }

    /**
     * Calls for max 5 seconds ervery 10ms {@link TCondition#isFullfilled()}.
     * 
     * @see SarosTestUtils#waitFor(TCondition, int, TimeUnit, int)
     */
    public static void waitFor(TCondition testCondition) {
        waitFor(testCondition, 5);
    }

    /**
     * Calls for max @seconds ervery 10ms {@link TCondition#isFullfilled()}.
     * 
     * @see SarosTestUtils#waitFor(TCondition, int, TimeUnit, int)
     */
    public static void waitFor(TCondition testCondition, int seconds) {
        waitFor(testCondition, 100 * seconds, TimeUnit.MILLISECONDS, 10);
    }

    /**
     * Calls periodically {@link TCondition#isFullfilled()} .
     * 
     * If the condition is fullfilled in @maxTries everything is fine and
     * nothing happens. Otherwise the condition is after all tries not
     * fullfilled you will get an error message which is specified by
     * {@link TCondition#getFailureMessage()}.
     * 
     * @param testCondition
     *            condition which will be asked periodically
     * @param maxTries
     *            number of tries the condition will be asked if its fullfilled
     * @param timeUnit
     *            {@link TimeUnit} for pollDelay
     * @param pollDelay
     *            amount of time to wait before asking the condition
     */
    public static void waitFor(TCondition testCondition, int maxTries,
        TimeUnit timeUnit, int pollDelay) {
        boolean fullfilled = false;
        for (int i = 0; i < maxTries; i++) {
            try {
                timeUnit.sleep(pollDelay);
                if (testCondition.isFullfilled()) {
                    fullfilled = true;
                    break;
                }
            } catch (Exception e) {
                // do nothing ...
            }
        }
        try {
            assertTrue(testCondition.getFailureMessage(), fullfilled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
