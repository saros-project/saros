package de.fu_berlin.inf.dpp.project;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SharedProjectUpdatableValueTest.class,
    SarosSessionManagerTest.class, SharedResourcesManagerTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
