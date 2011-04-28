package de.fu_berlin.inf.dpp.stf.client.testProject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.RosterViewBehaviour.TestChangingNameInRosterView;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations.TestFileOperations;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.fileFolderOperations.TestFolderOperations;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.initialising.TestHandleContacts;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.TestShare2UsersSequentially;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.TestShare3UsersConcurrently;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.TestShare3UsersSequentially;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation.TestShareProjectUsingExistingProject;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.permissionsAndFollowmode.TestWriteAccessChangeAndImmediateWrite;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.saving.TestCreatingNewFile;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.testSTF.views.sarosViews.TestSarosView;

@RunWith(Suite.class)
@SuiteClasses({ TestSarosView.class, TestChangingNameInRosterView.class,
    TestHandleContacts.class, TestShare2UsersSequentially.class,
    TestShare3UsersSequentially.class, TestShare3UsersConcurrently.class,
    TestShareProjectUsingExistingProject.class, TestFileOperations.class,
    TestFolderOperations.class, TestWriteAccessChangeAndImmediateWrite.class,
    TestCreatingNewFile.class })
public class AllTests {
    /**
     * Run AllTests in the list of SuiteClasses annotation
     */
}