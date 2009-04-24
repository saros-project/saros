package de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles;

import de.fu_berlin.inf.dpp.concurrent.jupiter.InclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.GOTOInclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.SplitOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.util.JupiterTestCase;

public class GOTOInclusionTransformationTest extends JupiterTestCase {

    protected InclusionTransformation inclusion = new GOTOInclusionTransformation();
    protected Operation insertOp = new InsertOperation(3, "abc");
    protected Operation splitOp1 = new SplitOperation(new DeleteOperation(2,
        "234"), new DeleteOperation(6, "6"));
    protected Operation splitOp2 = new SplitOperation(insertOp,
        new InsertOperation(7, "ins"));
    protected Operation splitOp3 = new SplitOperation(insertOp,
        new InsertOperation(6, "ins"));

    public void testSplitInsertTransformation() {

        Operation newOp = inclusion.transform(splitOp1, insertOp, Boolean.TRUE);
        Operation expectedOp = new SplitOperation(new SplitOperation(
            new DeleteOperation(2, "2"), new DeleteOperation(6, "34")),
            new DeleteOperation(9, "6"));
        assertEquals(expectedOp, newOp);
    }

    public void testInsertSplitTransformation() {

        Operation newOp = inclusion.transform(insertOp, splitOp1, Boolean.TRUE);
        Operation expectedOp = new InsertOperation(2, "abc", 3);
        // now position 2 but origin is 3
        assertEquals(expectedOp, newOp);
    }

    public void testSplitSplitTransformation() {

        Operation newOp = inclusion.transform(splitOp1, splitOp2, Boolean.TRUE);
        Operation expectedOp = new SplitOperation(new SplitOperation(
            new DeleteOperation(2, "2"), new DeleteOperation(6, "34")),
            new DeleteOperation(9, "6"));
        assertEquals(expectedOp, newOp);
    }

    public void testSplitSplitTransformation2() {

        Operation newOp = inclusion.transform(splitOp1, splitOp3, Boolean.TRUE);
        Operation expectedOp = new SplitOperation(new SplitOperation(
            new DeleteOperation(2, "2"), new DeleteOperation(6, "34")),
            new DeleteOperation(12, "6"));
        assertEquals(expectedOp, newOp);
    }

    public void testReplaceTransformation() {

        Operation replace1 = new SplitOperation(new DeleteOperation(3, "def"),
            new InsertOperation(3, "345"));
        Operation replace2 = new SplitOperation(new InsertOperation(1, "123"),
            new DeleteOperation(1, "bcd"));
        Operation newOp = inclusion.transform(replace1, replace2, Boolean.TRUE);
        Operation expectedOp = new SplitOperation(new DeleteOperation(4, "ef"),
            new InsertOperation(4, "345", 3));
        assertEquals(expectedOp, newOp);
    }
}
