package de.fu_berlin.inf.dpp.stf.test.saving;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ CreatingNewFileTest.class,
    UserWithWriteAccessResetsFilesTest.class,
    UserWithWriteAccessSavesFilesTest.class })
public class TestSuite {
    // the class remains completely empty,
    // being used only as a holder for the above annotations
}