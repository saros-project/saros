package de.fu_berlin.inf.dpp.net;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.fu_berlin.inf.dpp.net.internal.InternalTestSuite;
import de.fu_berlin.inf.dpp.net.util.NetUtilTestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FileListTest.class, GZipTest.class, JIDTest.class,
    XMPPConnectionTest.class, RosterTrackerTest.class, UPnPTest.class,
    TimedActivityDataObjectTest.class, XMPPUtilTest.class,
    InternalTestSuite.class, NetUtilTestSuite.class })
public class NetTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
