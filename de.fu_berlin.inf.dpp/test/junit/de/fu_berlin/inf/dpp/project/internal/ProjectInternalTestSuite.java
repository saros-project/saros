package de.fu_berlin.inf.dpp.project.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ActivityHandlerTest.class, ActivityQueuerTest.class,
    ActivitySequencerTest.class, ChecksumCacheTest.class,
    SarosProjectMapperTest.class, SarosSessionTest.class })
public class ProjectInternalTestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}