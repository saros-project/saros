package de.fu_berlin.inf.dpp.activities.business;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;

public class TextSelectionActivityTest extends AbstractResourceActivityTest {

    @Test
    @Override
    public void testConversion() {
        int[] offsets = { 0, 1, 10, 100, 247 };
        int[] lengths = { 0, 1, 10, 100, 248 };

        for (int offset : offsets) {
            for (int length : lengths) {
                for (SPath path : paths) {
                    TextSelectionActivity ts;
                    try {
                        ts = new TextSelectionActivity(source, offset, length,
                            path);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    testConversionAndBack(ts);
                }
            }
        }
    }
}
