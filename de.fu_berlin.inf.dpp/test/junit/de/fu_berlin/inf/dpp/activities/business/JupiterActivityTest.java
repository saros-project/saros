package de.fu_berlin.inf.dpp.activities.business;

import java.util.List;

import org.junit.Test;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.JupiterVectorTime;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.NoOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.TimestampOperation;

public class JupiterActivityTest extends AbstractResourceActivityTest {

    @Test
    @Override
    public void testConversion() {
        List<JupiterVectorTime> timestamps = toListPlusNull(
            new JupiterVectorTime(0, 0), new JupiterVectorTime(10, 2),
            new JupiterVectorTime(3, 9));

        List<Operation> operations = toListPlusNull(new NoOperation(),
            new SplitOperation(null, null), new TimestampOperation(),
            new InsertOperation(0, "abc"), new DeleteOperation(10, "abc"));

        for (Timestamp timestamp : timestamps) {
            for (Operation operation : operations) {
                for (SPath path : paths) {
                    JupiterActivity ja;
                    try {
                        ja = new JupiterActivity(timestamp, operation, source,
                            path);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    testConversionAndBack(ja);
                }
            }
        }
    }
}
