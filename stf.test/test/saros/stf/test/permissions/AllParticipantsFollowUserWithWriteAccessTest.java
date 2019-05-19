package saros.stf.test.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;
import static saros.stf.client.tester.SarosTester.CARL;
import static saros.stf.client.tester.SarosTester.DAVE;
import static saros.stf.shared.Constants.SUFFIX_JAVA;

import java.rmi.RemoteException;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.test.stf.Constants;

public class AllParticipantsFollowUserWithWriteAccessTest extends StfTestCase {

    /**
     * Preconditions:
     *
     * <ol>
     * <li>Alice (Host, Write Access)
     * <li>Bob (Read-Only Access)
     * <li>Carl (Read-Only Access)
     * <li>Dave (Read-Only Access)
     * <li>All read-only users enable followmode
     * </ol>
     */
    @BeforeClass
    public static void selectTester() throws Exception {
        Assume.assumeTrue(checkIfShare3UsersConcurrentlySucceeded());
        select(ALICE, BOB, CARL, DAVE);

        Util.setUpSessionWithJavaProjectAndClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB, CARL, DAVE);
    }

    @Before
    public void runBeforeEveryTest() throws Exception {
        closeAllShells();
        closeAllEditors();
        Util.activateFollowMode(ALICE, BOB, CARL, DAVE);
    }

    /**
     * Steps:
     *
     * <ol>
     * <li>Alice open a editor
     * </ol>
     *
     * Result:
     *
     * <ol>
     * <li>BOB, CARL and DAVE's editor would be opened too.
     * </ol>
     *
     * @throws RemoteException
     * @throws InterruptedException
     */
    @Test
    public void followingUserOpenClassWhenFollowedUserOpenClass()
        throws Exception {
        assertFalse(ALICE.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));
        assertFalse(BOB.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));
        assertFalse(CARL.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));
        assertFalse(DAVE.remoteBot().isEditorOpen(Constants.CLS1_SUFFIX));

        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();

        BOB.remoteBot().waitUntilEditorOpen(Constants.CLS1_SUFFIX);
        CARL.remoteBot().waitUntilEditorOpen(Constants.CLS1_SUFFIX);
        DAVE.remoteBot().waitUntilEditorOpen(Constants.CLS1_SUFFIX);

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).closeWithSave();
        BOB.remoteBot().editor(Constants.CLS1_SUFFIX).closeWithSave();
        CARL.remoteBot().editor(Constants.CLS1_SUFFIX).closeWithSave();
        DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).closeWithSave();
    }

    /**
     * Steps:
     *
     * <ol>
     * <li>Alice open a editor and set text in the opened java editor without
     * save.
     * </ol>
     *
     * Result:
     *
     * <ol>
     * <li>BOB, CARL and DAVE have the same dirty content as ALICE and their
     * editor would be opened and activated too.
     * </ol>
     *
     * @throws RemoteException
     */
    @Test
    public void testFollowModeByEditingClassByAlice() throws Exception {

        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .typeText("foo la la la");

        String dirtyClsContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS1_SUFFIX).getText();

        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));

        CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
        assertTrue(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));

        DAVE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).getText()
            .equals(dirtyClsContentOfAlice));

        ALICE.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
        BOB.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
        CARL.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
        DAVE.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
    }
}
