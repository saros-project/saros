package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;

public class ChecksumActivityTest extends AbstractResourceActivityTest {

    @Override
    @Test
    public void testConversion() {
        long[] hashes = { 0L, 1L, 10L, 1024L, 834711L };
        long[] lengths = { 0L, 1L, 10L, 1024L, 12398L };

        List<JupiterVectorTime> timestamps = toListPlusNull(
            new JupiterVectorTime(0, 0), new JupiterVectorTime(10, 2),
            new JupiterVectorTime(3, 9));

        for (SPath path : paths) {
            for (long hash : hashes) {
                for (long length : lengths) {
                    for (Timestamp timestamp : timestamps) {
                        ChecksumActivity ca;
                        try {
                            ca = new ChecksumActivity(source, path, hash,
                                length, timestamp);
                        } catch (IllegalArgumentException e) {
                            // ctor rejected current configuration --> next one
                            continue;
                        }

                        testConversionAndBack(ca);
                    }
                }
            }
        }
    }
}
