package de.fu_berlin.inf.dpp.stf.client.testProject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.saving.TestUserWithWriteAccessResetsFiles;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.saving.TestUserWithWriteAccessSavesFiles;

@RunWith(Suite.class)
@SuiteClasses({ TestUserWithWriteAccessResetsFiles.class,
    TestUserWithWriteAccessSavesFiles.class })
public class AllTestsBy5Testers {
    /**
     * Run AllTests in the list of SuiteClasses annotation
     */
}
