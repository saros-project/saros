package de.fu_berlin.inf.dpp.stf.test.invitation;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

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
public class SVNStateInitializationTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Alice has the project {@link Constants#SVN_PROJECT_COPY}, which is
     * checked out from SVN:<br>
     * repository: {@link Constants#SVN_REPOSITORY_URL}<br>
     * path: {@link Constants#SVN_PROJECT_PATH}
     * </ol>
     * 
     * @throws RemoteException
     */
    @BeforeClass
    public static void initMusicians() throws Exception {
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
        if (!ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.SVN_PROJECT_COPY)) {
            ALICE.superBot().views().packageExplorerView().tree().newC()
                .javaProject(Constants.SVN_PROJECT_COPY);
            ALICE
                .superBot()
                .views()
                .packageExplorerView()
                .selectProject(Constants.SVN_PROJECT_COPY)
                .team()
                .shareProjectUsingSpecifiedFolderName(
                    Constants.SVN_REPOSITORY_URL, Constants.SVN_PROJECT_PATH);
        }
    }

    /**
     * Preconditions:
     * <ol>
     * <li>Alice copied {@link Constants#SVN_PROJECT_COPY} to
     * {@link Constants#SVN_PROJECT}.</li>
     * </ol>
     * Only SVN_PROJECT is used in the tests. Copying from SVN_PROJECT_COPY is
     * faster than checking out the project for every test.
     * 
     * @throws RemoteException
     */

    @Before
    public void beforeEveryTest() throws RemoteException {
        ALICE.superBot().views().packageExplorerView()
            .selectProject(Constants.SVN_PROJECT_COPY).copy();
        ALICE.superBot().views().packageExplorerView().tree()
            .paste(Constants.SVN_PROJECT);

        assertTrue(ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.SVN_PROJECT));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg("stf_test_project", "pkg").existsWithRegex("Test.java"));
    }

    @After
    public void afterEveryTest() throws RemoteException {
        leaveSessionHostFirst(ALICE);

        if (ALICE.superBot().views().packageExplorerView().tree()
            .existsWithRegex(Constants.SVN_PROJECT))
            ALICE.superBot().views().packageExplorerView()
                .selectJavaProject(Constants.SVN_PROJECT).delete();
        BOB.superBot().internal().clearWorkspace();
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
        // ALICE, BOB);

        ALICE.superBot().views().packageExplorerView()
            .selectJavaProject(Constants.SVN_PROJECT).shareWith()
            .buddy(BOB.getJID());
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        ALICE.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(BOB.superBot());
        assertTrue(BOB.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));

        // assertTrue(ALICE.superBot().views().sarosView()
        // .existsParticipant(BOB.getJID()));
        assertTrue(ALICE.superBot().views().sarosView()
            .selectParticipant(BOB.getJID()).hasWriteAccess());
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice updates {@link Constants#SVN_CLS1} to revision
     * {@link Constants#SVN_CLS1_REV1}.</li>
     * <li>Alice shares project {@link Constants#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link Constants#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link Constants#SVN_CLS1} has revision
     * {@link Constants#SVN_CLS1_REV1}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithUpdate() throws RemoteException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1).team().update(Constants.SVN_CLS1_REV1);
        assertEquals(Constants.SVN_CLS1_REV1, ALICE.superBot().views()
            .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // ALICE, BOB);
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .workTogetherOn().project(Constants.SVN_PROJECT);

        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        ALICE.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(BOB.superBot());

        assertTrue(BOB.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));
        assertEquals(Constants.SVN_CLS1_REV1, BOB.superBot().views()
            .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches {@link Constants#SVN_CLS1} to
     * {@link Constants#SVN_CLS1_SWITCHED_URL}.</li>
     * <li>Alice shares project {@link Constants#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link Constants#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link Constants#SVN_CLS1} is switched to
     * {@link Constants#SVN_CLS1_SWITCHED_URL}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithSwitch() throws RemoteException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .team()
            .switchResource(Constants.SVN_CLS1_FULL_PATH,
                Constants.SVN_CLS1_SWITCHED_URL);
        assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
            ALICE.superBot().views().packageExplorerView()
                .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // ALICE, BOB);
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .workTogetherOn().project(Constants.SVN_PROJECT);
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        ALICE.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(BOB.superBot());
        BOB.superBot().views().sarosView().waitUntilIsInSession();

        assertTrue(BOB.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));
        assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
            BOB.superBot().views().packageExplorerView()
                .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice switches {@link Constants#SVN_CLS1} to
     * {@link Constants#SVN_CLS1_SWITCHED_URL}@{@link Constants#SVN_CLS1_REV3}.</li>
     * <li>Alice shares project {@link Constants#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link Constants#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link Constants#SVN_CLS1} is switched to
     * {@link Constants#SVN_CLS1_SWITCHED_URL}@{@link Constants#SVN_CLS1_REV3}.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithSwitch2() throws RemoteException {
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .team()
            .switchResource(Constants.SVN_CLS1_FULL_PATH,
                Constants.SVN_CLS1_SWITCHED_URL, Constants.SVN_CLS1_REV3);
        assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
            ALICE.superBot().views().packageExplorerView()
                .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
        assertEquals(Constants.SVN_CLS1_REV3, ALICE.superBot().views()
            .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // ALICE, BOB);
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .workTogetherOn().project(Constants.SVN_PROJECT);
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        ALICE.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(BOB.superBot());
        BOB.superBot().views().sarosView().waitUntilIsInSession();

        assertTrue(BOB.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));
        assertEquals(Constants.SVN_CLS1_SWITCHED_URL,
            BOB.superBot().views().packageExplorerView()
                .getURLOfRemoteResource(Constants.SVN_CLS1_FULL_PATH));
        assertEquals(Constants.SVN_CLS1_REV3, BOB.superBot().views()
            .packageExplorerView().getRevision(Constants.SVN_CLS1_FULL_PATH));
    }

    /**
     * Steps:
     * <ol>
     * <li>Alice modifies her working copy by changing the content of the file
     * {@link Constants#SVN_CLS1} to {@link Constants#CP1}.</li>
     * <li>Alice shares project {@link Constants#SVN_PROJECT} with Bob.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob's copy of {@link Constants#SVN_PROJECT} is managed by SVN.</li>
     * <li>Bob's copy of {@link Constants#SVN_CLS1} has the same content as
     * Alice's copy.</li>
     * </ol>
     * 
     * @throws RemoteException
     * 
     */
    @Test
    public void testCheckoutWithModification() throws RemoteException {
        assertTrue(ALICE.superBot().views().packageExplorerView()
            .selectPkg(Constants.SVN_PROJECT, Constants.SVN_PKG)
            .existsWithRegex(Constants.SVN_CLS1 + SUFFIX_JAVA));
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1).open();
        String cls1_content_before = ALICE.remoteBot()
            .editor(Constants.SVN_CLS1_SUFFIX).getText();
        ALICE.remoteBot().editor(Constants.SVN_CLS1 + SUFFIX_JAVA)
            .setTextFromFile(Constants.CP1);
        String cls1_content_after = ALICE.remoteBot()
            .editor(Constants.SVN_CLS1_SUFFIX).getText();
        assertFalse(cls1_content_after.equals(cls1_content_before));

        // buildSessionSequentially(SVN_PROJECT,
        // TypeOfCreateProject.NEW_PROJECT,
        // ALICE, BOB);
        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .workTogetherOn().project(Constants.SVN_PROJECT);
        BOB.superBot().confirmShellSessionInvitationAndShellAddProject(
            Constants.SVN_PROJECT, TypeOfCreateProject.NEW_PROJECT);

        ALICE.superBot().views().sarosView()
            .waitUntilIsInviteeInSession(BOB.superBot());
        BOB.superBot().views().sarosView().waitUntilIsInSession();

        assertTrue(BOB.superBot().views().packageExplorerView()
            .isProjectManagedBySVN(Constants.SVN_PROJECT));
        BOB.superBot()
            .views()
            .packageExplorerView()
            .selectClass(Constants.SVN_PROJECT, Constants.SVN_PKG,
                Constants.SVN_CLS1).open();
        assertEquals(cls1_content_after,
            BOB.remoteBot().editor(Constants.SVN_CLS1_SUFFIX).getText());
    }
}
