package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;

public class ViewportActivityTest extends AbstractResourceActivityTest {

    @Test
    @Override
    public void testConversion() {
        for (int topIndex : new int[] { 0, 9, 10, 50 }) {
            for (int bottomIndex : new int[] { 1, 10, 20, 100 }) {
                for (SPath path : paths) {
                    ViewportActivity va;
                    try {
                        va = new ViewportActivity(source, topIndex,
                            bottomIndex, path);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    testConversionAndBack(va);
                }
            }
        }
    }
}
