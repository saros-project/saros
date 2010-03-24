package de.fu_berlin.inf.dpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.concurrent.ConcurrencyTestSuite;
import de.fu_berlin.inf.dpp.concurrent.jupiter.test.puzzles.JupiterPuzzlesTestSuite;
import de.fu_berlin.inf.dpp.concurrent.undo.ConcurrentUndoTestSuite;
import de.fu_berlin.inf.dpp.net.NetTestSuite;
import de.fu_berlin.inf.dpp.net.internal.InternalTestSuite;
import de.fu_berlin.inf.dpp.net.internal.extensions.InternalExtensionsTestSuite;
import de.fu_berlin.inf.dpp.net.jingle.protocol.JingleProtocolTestSuite;
import de.fu_berlin.inf.dpp.util.UtilTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { ConcurrencyTestSuite.class,
    JupiterPuzzlesTestSuite.class, ConcurrentUndoTestSuite.class,
    NetTestSuite.class, InternalTestSuite.class,
    InternalExtensionsTestSuite.class, JingleProtocolTestSuite.class,
    UtilTestSuite.class })
public class AllTestsSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
