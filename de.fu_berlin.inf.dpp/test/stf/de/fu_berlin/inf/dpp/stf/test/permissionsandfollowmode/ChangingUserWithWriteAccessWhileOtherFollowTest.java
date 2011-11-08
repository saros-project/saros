package de.fu_berlin.inf.dpp.stf.test.permissionsandfollowmode;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.DAVE;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class ChangingUserWithWriteAccessWhileOtherFollowTest extends
    StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>Dave (Read-Only Access)</li>
     * <li>All read-only users enable followmode</li>
     * </ol>
     * 
     */

    @BeforeClass
    public static void initializeSaros() throws Exception {
        initTesters(ALICE, BOB, CARL, DAVE);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithJavaProjectAndClass(Constants.PROJECT1,
            Constants.PKG1, Constants.CLS1, ALICE, BOB, CARL, DAVE);
        Util.activateFollowMode(ALICE, BOB, CARL, DAVE);
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE opens a file and edit it.</li>
     * <li>participants leave follow mode after they saw the opened file.</li>
     * <li>ALICE continue to edit the opened file, but doesn't save</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li></li>
     * <li>read-only users saw the opened file and the dirty flag of the file,</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     * @throws InterruptedException
     * 
     * 
     */

    @Test
    public void testChangingWriteAccessWhileOtherFollow() throws Exception {

        /*
         * After new release 10.10.28 all read-only users is automatically in
         * follow mode(are the read-only users really in follow mode???) when
         * host grants someone exclusive write access. So the following three
         * line have to comment out, otherwise you should get timeoutException.
         */
        // ALICE.bot.waitUntilFollowed(CARL.getBaseJid());
        // BOB.bot.waitUntilFollowed(CARL.getBaseJid());
        // DAVE.bot.waitUntilFollowed(CARL.getBaseJid());

        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextFromFile(Constants.CP1);

        String dirtyClsContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS1_SUFFIX).getText();

        CARL.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());

        BOB.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());

        DAVE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());

        Util.stopFollowModeSequentially(CARL, BOB, DAVE);
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextFromFile(Constants.CP1_CHANGE);
        // ALICE.bot().editor(CLS1_SUFFIX).closeAndSave();

        assertTrue(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(CARL.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
        /*
         * TODO ALICE can still see the changes maded by CARL, although she
         * already leave follow mode. There is a bug here (see Bug 3094186)and
         * it should be fixed, so that asserts that the following condition is
         * false
         * 
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(ALICE.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(BOB.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());

        /*
         * TODO BOB can still see the changes maded by CARL, although he already
         * leave follow mode. There is a bug here (see Bug 3094186) and it
         * should be fixed, so that asserts that the following condition is
         * false.
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(BOB.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isActive());
        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
        /*
         * TODO DAVE can still see the changes , although he already leave
         * follow mode. There is a bug here (see Bug 3094186) and it should be
         * fixed, so that asserts that the following condition is false.
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(DAVE.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

    }
}
