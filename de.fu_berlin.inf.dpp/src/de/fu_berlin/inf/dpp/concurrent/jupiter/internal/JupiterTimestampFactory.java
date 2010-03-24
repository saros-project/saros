package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TimestampFactory;

/**
 * TimestampFactory that creates Jupiter specific Timestamp objects. The
 * encoding for Jupiter specific Timestamps is a component array of length 2
 * whereby the first index of the array contains the local operation count and
 * the second index of the array contains the remote operation count.
 */
public class JupiterTimestampFactory implements TimestampFactory {

    public Timestamp createTimestamp(int[] components) {
        if (components.length != 2) {
            throw new IllegalArgumentException(
                "JupiterTimestampFactory expects a component array"
                    + "of length 2");
        }
        return new JupiterVectorTime(components[0], components[1]);
    }

}
