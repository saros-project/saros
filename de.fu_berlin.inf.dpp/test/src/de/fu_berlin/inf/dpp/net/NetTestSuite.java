package de.fu_berlin.inf.dpp.net;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { FileListTest.class, GZipTest.class, JIDTest.class,
    XMPPConnectionTest.class })
public class NetTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
