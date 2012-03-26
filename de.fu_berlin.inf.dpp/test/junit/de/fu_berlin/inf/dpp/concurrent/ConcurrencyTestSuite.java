package de.fu_berlin.inf.dpp.concurrent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles.JupiterPuzzlesTestSuite;
import de.fu_berlin.inf.dpp.concurrent.undo.ConcurrentUndoTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SplitOperationTest.class, JupiterPuzzlesTestSuite.class,
    ConcurrentUndoTestSuite.class, })
public class ConcurrencyTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
