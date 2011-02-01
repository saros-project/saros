package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.enteringAndExitingSession;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestHostWithWriteAccessInvitesBelatedly extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>All read-only users enable follow mode</li>
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
     * TODO: There are some bugs, if bob leave session and alice try it again,
     * bob will not get the same project like alice.
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testFollowModeByOpenClassbyAlice() throws IOException,
        CoreException, InterruptedException {
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        bob.editor
            .setTextInJavaEditorWithSave(CP1_CHANGE, PROJECT1, PKG1, CLS1);

        alice.editor.setTextInJavaEditorWithoutSave(CP2, PROJECT1, PKG1, CLS2);
        bob.editor.setTextInJavaEditorWithoutSave(CP2_CHANGE, PROJECT1, PKG1,
            CLS2);

        alice.inviteUsersInSessionDone(PROJECT1,
            TypeOfCreateProject.EXIST_PROJECT, bob);

        bob.editor.activateJavaEditor(CLS1);
        bob.editor.waitUntilJavaEditorContentSame(CP1, PROJECT1, PKG1, CLS1);
        bob.editor.waitUntilJavaEditorContentSame(CP2, PROJECT1, PKG1, CLS2);
        String CLSContentOfAlice = alice.editor.getTextOfJavaEditor(PROJECT1,
            PKG1, CLS1);
        String CLS2ContentOfAlice = alice.editor.getTextOfJavaEditor(PROJECT1,
            PKG1, CLS2);

        String CLSContentOfBob = bob.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        String CLS2ContentOfBob = bob.editor.getTextOfJavaEditor(PROJECT1,
            PKG1, CLS2);

        assertEquals(CLSContentOfAlice, CLSContentOfBob);
        assertEquals(CLS2ContentOfAlice, CLS2ContentOfBob);
    }
}
