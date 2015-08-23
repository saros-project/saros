package de.fu_berlin.inf.dpp.project.internal;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SarosProjectMapperTest.class, SarosSessionTest.class,
    UserInformationHandlerTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}