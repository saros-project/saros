package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.enteringAndExitingSession;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestHostAsDriverInvitesBelatedly extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>All observers enable follow mode</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL);
        setUpWorkbenchs();
        setUpSaros();
        alice.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        bob.fileM.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        bob.fileM.newClass(PROJECT1, PKG1, CLS2);

        /*
         * alice build session with carl and is followed by carl.
         */
        alice.buildSessionDoneSequentially(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            carl);
        alice.followedBy(carl);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(bob, carl);
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() throws RemoteException {
        alice.addBuddyGUIDone(bob);
        bob.addBuddyGUIDone(alice);
    }

    /**
     * Steps:
     * 
     * 1. alice edits the file CLS but don't saves it.
     * 
     * 2. bob edits the file CLS in the project currently used and saves it.
     * 
     * 3. alice edits the file CLS2 but don't saves it.
     * 
     * 4. bob edits the file CLS2 in the project currently used and don't saves
     * it.
     * 
     * 5. alice invites bob.
     * 
     * 6. The question about the changed files at bob is answered with YES.
     * 
     * 7. bob accepts and uses project from test X.
     * 
     * Expected Results:
     * 
     * 7. bob has the same project like host.
     * 
     * @throws CoreException
     * @throws IOException
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws IOException,
        CoreException {
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        bob.editor
            .setTextInJavaEditorWithSave(CP1_CHANGE, PROJECT1, PKG1, CLS1);

        alice.editor.setTextInJavaEditorWithoutSave(CP2, PROJECT1, PKG1, CLS2);
        bob.editor.setTextInJavaEditorWithoutSave(CP2_CHANGE, PROJECT1, PKG1,
            CLS2);

        alice.sessionV.openInvitationInterface(bob.getBaseJid());

        bob.pEV.confirmFirstPageOfWizardSessionInvitation();
        bob.pEV
            .confirmSecondPageOfWizardSessionInvitationUsingExistProject(PROJECT1);

        bob.workbench.sleep(500);
        alice.sessionV.waitUntilSessionOpenBy(bob.sessionV);

        String CLSContentOfAlice = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);
        String CLS2ContentOfAlice = alice.editor.getClassContent(PROJECT1,
            PKG1, CLS2);

        String CLSContentOfBob = bob.editor.getClassContent(PROJECT1, PKG1,
            CLS1);
        String CLS2ContentOfBob = bob.editor.getClassContent(PROJECT1, PKG1,
            CLS2);

        assertEquals(CLSContentOfAlice, CLSContentOfBob);
        assertEquals(CLS2ContentOfAlice, CLS2ContentOfBob);
    }
}
