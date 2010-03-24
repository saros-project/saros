package de.fu_berlin.inf.dpp.test.util;

import static org.easymock.classextension.EasyMock.createMock;

import org.easymock.EasyMock;

public class SarosTestUtils {

    /**
     * Returns a mock for the given class which is already switched to replay
     */
    public static <T> T createReplayMock(Class<T> clazz) {
        return replayFluid(createMock(clazz));
    }

    public static <T> T replayFluid(T t) {
        EasyMock.replay(t);
        return t;
    }

}
