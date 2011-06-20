package de.fu_berlin.inf.dpp.test.util;

import static org.easymock.EasyMock.createMock;

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

}
