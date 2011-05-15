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
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
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
        setUpWorkbench();
        setUpSaros();
        if (!alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(SVN_PROJECT_COPY)) {
            alice.superBot().views().packageExplorerView().tree().newC()
                .javaProject(SVN_PROJECT_COPY);
            alice
                .superBot()
                .views()
                .packageExplorerView()
                .selectProject(SVN_PROJECT_COPY)
                .team()
                .shareProjectUsingSpecifiedFolderName(SVN_REPOSITORY_URL,
                    SVN_PROJECT_PATH);
        }
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        resetSaros(bob);
        // if (ConfigTester.DEVELOPMODE) {
        // if (alice.sarosBot().sessionView().isInSession())
        // alice.sarosBot().sessionView().leaveTheSessionByHost();
        // // don't delete SVN_PROJECT_COPY
        // } else {
        resetSaros(alice);
        // }
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
        alice.superBot().views().packageExplorerView()
            .selectProject(SVN_PROJECT_COPY).copy();
        alice.superBot().views().packageExplorerView().tree()
            .paste(SVN_PROJECT);

        assertTrue(alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(SVN_PROJECT));
        assertTrue(alice.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg("stf_test_project", "pkg").existsWithRegex("Test.java"));
    }

    @After
    public void tearDown() throws RemoteException, InterruptedException {
        leaveSessionHostFirst(alice);

        if (alice.superBot().views().packageExplorerView().tree()
            .existsWithRegex(SVN_PROJECT))
            alice.superBot().views().packageExplorerView()
                .selectJavaProject(SVN_PROJECT).delete();
        deleteAllProjects(bob);
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

        /**
         * FIXME: by shareProjects Wizard there are tableItem need to be
         * selected, which can not be found with regexText by SWTBot
         */
        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // alice, bob);

        alice.superBot().views().packageExplorerView()
            .selectJavaProject(SVN_PROJECT).shareWith().buddy(bob.getJID());
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        alice.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(bob.superBot());
        assertTrue(bob.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));

        // assertTrue(alice.superBot().views().sarosView()
        // .existsParticipant(bob.getJID()));
        assertTrue(alice.superBot().views().sarosView()
            .selectParticipant(bob.getJID()).hasWriteAccess());
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
        alice.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).team()
            .update(SVN_CLS1_REV1);
        assertEquals(SVN_CLS1_REV1, alice.superBot().views()
            .packageExplorerView().getRevision(SVN_CLS1_FULL_PATH));
        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // alice, bob);
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .workTogetherOn().project(SVN_PROJECT);

        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        alice.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(bob.superBot());

        assertTrue(bob.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_REV1, bob.superBot().views()
            .packageExplorerView().getRevision(SVN_CLS1_FULL_PATH));
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
        alice.superBot().views().packageExplorerView().tree().team()
            .switchResource(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL);
        assertEquals(SVN_CLS1_SWITCHED_URL, alice.superBot().views()
            .packageExplorerView().getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // alice, bob);
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .workTogetherOn().project(SVN_PROJECT);
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        alice.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(bob.superBot());
        bob.superBot().views().sarosView().waitUntilIsInSession();

        assertTrue(bob.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_SWITCHED_URL, bob.superBot().views()
            .packageExplorerView().getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
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
        alice
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .team()
            .switchResource(SVN_CLS1_FULL_PATH, SVN_CLS1_SWITCHED_URL,
                SVN_CLS1_REV3);
        assertEquals(SVN_CLS1_SWITCHED_URL, alice.superBot().views()
            .packageExplorerView().getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        assertEquals(SVN_CLS1_REV3, alice.superBot().views()
            .packageExplorerView().getRevision(SVN_CLS1_FULL_PATH));
        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // alice, bob);
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .workTogetherOn().project(SVN_PROJECT);
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        alice.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(bob.superBot());
        bob.superBot().views().sarosView().waitUntilIsInSession();

        assertTrue(bob.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));
        assertEquals(SVN_CLS1_SWITCHED_URL, bob.superBot().views()
            .packageExplorerView().getURLOfRemoteResource(SVN_CLS1_FULL_PATH));
        assertEquals(SVN_CLS1_REV3, bob.superBot().views()
            .packageExplorerView().getRevision(SVN_CLS1_FULL_PATH));
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
        assertTrue(alice.superBot().views().packageExplorerView()
            .selectPkg(SVN_PROJECT, SVN_PKG)
            .existsWithRegex(SVN_CLS1 + SUFFIX_JAVA));
        alice.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).open();
        String cls1_content_before = alice.remoteBot().editor(SVN_CLS1_SUFFIX)
            .getText();
        alice.remoteBot().editor(SVN_CLS1 + SUFFIX_JAVA).setTexWithSave(CP1);
        String cls1_content_after = alice.remoteBot().editor(SVN_CLS1_SUFFIX)
            .getText();
        assertFalse(cls1_content_after.equals(cls1_content_before));

        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // alice, bob);
        alice.superBot().views().sarosView().selectBuddy(bob.getJID())
            .workTogetherOn().project(SVN_PROJECT);
        bob.superBot().confirmShellSessionInvitationAndShellAddProject(
            SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        alice.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(bob.superBot());
        bob.superBot().views().sarosView().waitUntilIsInSession();

        assertTrue(bob.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(SVN_PROJECT));
        bob.superBot().views().packageExplorerView()
            .selectClass(SVN_PROJECT, SVN_PKG, SVN_CLS1).open();
        assertEquals(cls1_content_after, bob.remoteBot().editor(SVN_CLS1_SUFFIX)
            .getText());
    }
}
