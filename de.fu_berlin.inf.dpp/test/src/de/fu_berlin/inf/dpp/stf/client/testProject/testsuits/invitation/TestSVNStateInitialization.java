package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.invitation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.ConfigTester;
import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

/**
 * Tests for the initial synchronization of the SVN state during the invitation.<br>
 * <br>
 * Bob doesn't have a project, Alice has an SVN project, AND<br>
 * <br>
 * Alice has one resource updated to another revision<br>
 * Alice has one resource switched to another URL<br>
 * Alice has one resource switched and updated<br>
 * Alice has one resource modified<br>
 * TODO Alice has one resource added (not promoted)<br>
 * TODO Alice has one resource added (promoted)<br>
 * TODO Alice has one resource removed<br>
 * <br>
 * use existing:<br>
 * TODO Alice has an SVN project, Bob has the non-SVN project (connect during
 * invitation)<br>
 * TODO Alice has a non-SVN project, Bob has the SVN project (disconnect during
 * invitation)<br>
 * TODO Alice has an SVN project, one resource switched and updated, Bob has the
 * unmodified project<br>
 * TODO Alice has an SVN project, one resource switched and updated, Bob has the
 * managed project with other resources switched/updated/deleted.<br>
 */
public class TestSVNStateInitialization extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Alice has the project {@link STFTest#SVN_PROJECT_COPY}, which is
     * checked out from SVN:<br>
     * repository: {@link STFTest#SVN_REPOSITORY_URL}<br>
     * path: {@link STFTest#SVN_PROJECT_PATH}
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
        if (!alice.fileM.existsProject(SVN_PROJECT_COPY)) {
            alice.fileM.newJavaProject(SVN_PROJECT_COPY);
            alice.team.shareProjectWithSVNUsingSpecifiedFolderName(
                SVN_PROJECT_COPY, SVN_REPOSITORY_URL, SVN_PROJECT_PATH);
        }
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        if (ConfigTester.DEVELOPMODE) {
            if (alice.sessionV.isInSessionGUI())
                alice.sessionV.leaveTheSessionByHost();
            // don't delete SVN_PROJECT_COPY
        } else {
            alice.workbench.resetSaros();
        }
    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice copied {@link STFTest#SVN_PROJECT_COPY} to
     * {@link STFTest#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */
    @Before
    public void setUp() throws RemoteException {
        alice.editM.copyProject(SVN_PROJECT, SVN_PROJECT_COPY);
        assertTrue(alice.fileM.existsProject(SVN_PROJECT));
        assertTrue(alice.team.isProjectManagedBySVN(SVN_PROJECT));
        assertTrue(alice.fileM.existsFile(SVN_CLS1_FULL_PATH));
    }

    @After
    public void tearDown() throws RemoteException, InterruptedException {
        alice.leaveSessionHostFirstDone(bob);

        if (alice.fileM.existsProject(SVN_PROJECT))
            alice.editM.deleteProject(SVN_PROJECT);
        bob.workbench.deleteAllProjects();
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice shares project SVN_PROJECT with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of SVN_PROJECT is managed by SVN.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testSimpleCheckout() throws RemoteException {
        alice.buildSessionDoneSequentially(SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        alice.sessionV.waitUntilInviteeIsInSession(bob.sessionV);
        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));

        assertTrue(alice.sessionV.isDriver());
        assertTrue(alice.sessionV.isParticipant(bob.jid));
        assertTrue(bob.sessionV.isObserver(bob.jid));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates {@link STFTest#SVN_CLS1} to revision
     * {@link STFTest#SVN_CLS1_REV1}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} has revision
     * {@link STFTest#SVN_CLS1_REV1}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithUpdate() throws RemoteException {
        alice.team.updateClass(SVN_PROJECT, SVN_PKG, SVN_CLS1, SVN_CLS1_REV1);
        assertEquals(SVN_CLS1_REV1, alice.team.getRevision(SVN_CLS1_FULL_PATH));
        alice.buildSessionDoneSequentially(SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        alice.sessionV.waitUntilInviteeIsInSession(bob.sessionV);

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_REV1, bob.team.getRevision(SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches {@link STFTest#SVN_CLS1} to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} is switched to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithSwitch() throws RemoteException {
        alice.team.switchResource(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);
        assertEquals(SVN_CLS1_SWITCHED_URL,
            alice.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        alice.buildSessionDoneSequentially(SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        alice.sessionV.waitUntilInviteeIsInSession(bob.sessionV);
        bob.sessionV.waitUntilIsInSession();

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_SWITCHED_URL,
            bob.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches {@link STFTest#SVN_CLS1} to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}@{@link STFTest#SVN_CLS1_REV3}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} is switched to
     * {@link STFTest#SVN_CLS1_SWITCHED_URL}@{@link STFTest#SVN_CLS1_REV3}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithSwitch2() throws RemoteException {
        alice.team.switchResource(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL,
            SVN_CLS1_REV3);
        assertEquals(SVN_CLS1_SWITCHED_URL,
            alice.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        assertEquals(SVN_CLS1_REV3, alice.team.getRevision(SVN_CLS1_FULL_PATH));
        alice.buildSessionDoneSequentially(SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        alice.sessionV.waitUntilInviteeIsInSession(bob.sessionV);
        bob.sessionV.waitUntilIsInSession();

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_SWITCHED_URL,
            bob.team.getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        assertEquals(SVN_CLS1_REV3, bob.team.getRevision(SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice modifies her working copy by changing the content of the file
     * {@link STFTest#SVN_CLS1} to {@link STFTest#CP1}.</li>
     * <li>Alice shares project {@link STFTest#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link STFTest#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link STFTest#SVN_CLS1} has the same content as
     * Alice's copy.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithModification() throws RemoteException {
        assertTrue(alice.fileM.existsClass(SVN_PROJECT, SVN_PKG, SVN_CLS1));
        String cls1_content_before = alice.editor.getTextOfJavaEditor(
            SVN_PROJECT, SVN_PKG, SVN_CLS1);
        alice.editor.setTextInJavaEditorWithSave(CP1, SVN_PROJECT, SVN_PKG,
            SVN_CLS1);
        String cls1_content_after = alice.editor.getTextOfJavaEditor(
            SVN_PROJECT, SVN_PKG, SVN_CLS1);
        assertFalse(cls1_content_after.equals(cls1_content_before));

        alice.buildSessionDoneSequentially(SVN_PROJECT,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            bob);
        alice.sessionV.waitUntilInviteeIsInSession(bob.sessionV);
        bob.sessionV.waitUntilIsInSession();

        assertTrue(bob.team.isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(cls1_content_after,
            bob.editor.getTextOfJavaEditor(SVN_PROJECT, SVN_PKG, SVN_CLS1));
    }

}
