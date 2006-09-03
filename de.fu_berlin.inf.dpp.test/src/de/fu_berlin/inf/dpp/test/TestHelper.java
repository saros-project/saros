
package de.fu_berlin.inf.dpp.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;

public class TestHelper {

    public static void assertList(Object[] expected, List actual) {
        boolean equals = expected.length == actual.size();

        if (equals) {
            for (int i = 0; i < expected.length; i++) {
                if (!expected[i].equals(actual.get(i))) {
                    equals = false;
                    break;
                }
            }
        }

        if (!equals) {
            List<Object> expectedList = new ArrayList<Object>(expected.length);
            for (int i = 0; i < expected.length; i++) {
                expectedList.add(expected[i]);
            }

            throw new AssertionFailedError(
                "expected:" + expectedList + " but was " + actual);
        }
    }
}
