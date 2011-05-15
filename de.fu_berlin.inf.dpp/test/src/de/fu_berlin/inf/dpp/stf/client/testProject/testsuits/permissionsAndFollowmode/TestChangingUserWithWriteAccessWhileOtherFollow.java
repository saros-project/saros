package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.permissionsAndFollowmode;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestChangingUserWithWriteAccessWhileOtherFollow extends STFTest {

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
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob, carl, dave);
        setFollowMode(alice, bob, carl, dave);
    }

    /**
     * Steps:
     * <ol>
     * <li>alice opens a file and edit it.</li>
     * <li>participants leave follow mode after they saw the opened file.</li>
     * <li>alice continue to edit the opened file, but doesn't save</li>
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
    public void testChangingWriteAccessWhileOtherFollow() throws IOException,
        CoreException, InterruptedException {

        /*
         * After new release 10.10.28 all read-only users is automatically in
         * follow mode(are the read-only users really in follow mode???) when
         * host grants someone exclusive write access. So the following three
         * line have to comment out, otherwise you should get timeoutException.
         */
        // alice.bot.waitUntilFollowed(carl.getBaseJid());
        // bob.bot.waitUntilFollowed(carl.getBaseJid());
        // dave.bot.waitUntilFollowed(carl.getBaseJid());

        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        alice.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String dirtyClsContentOfAlice = alice.remoteBot().editor(CLS1_SUFFIX)
            .getText();

        carl.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(carl.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(carl.remoteBot().editor(CLS1_SUFFIX).isDirty());

        bob.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(bob.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(bob.remoteBot().editor(CLS1_SUFFIX).isDirty());

        dave.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).isDirty());

        resetFollowModeSequentially(carl, bob, dave);
        alice.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1_CHANGE);
        // alice.bot().editor(CLS1_SUFFIX).closeAndSave();

        assertTrue(carl.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(carl.remoteBot().editor(CLS1_SUFFIX).isDirty());
        /*
         * TODO alice can still see the changes maded by carl, although she
         * already leave follow mode. There is a bug here (see Bug 3094186)and
         * it should be fixed, so that asserts that the following condition is
         * false
         * 
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(alice.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

        assertTrue(bob.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(bob.remoteBot().editor(CLS1_SUFFIX).isDirty());

        /*
         * TODO bob can still see the changes maded by carl, although he already
         * leave follow mode. There is a bug here (see Bug 3094186) and it
         * should be fixed, so that asserts that the following condition is
         * false.
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(bob.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).isActive());
        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).isDirty());
        /*
         * TODO dave can still see the changes , although he already leave
         * follow mode. There is a bug here (see Bug 3094186) and it should be
         * fixed, so that asserts that the following condition is false.
         * 
         * With Saros Version 10.10.29.DEVEL.
         */
        // assertFalse(dave.bot.getTextOfJavaEditor(PROJECT, PKG, CLS).equals(
        // dirtyClsChangeContentOfCarl));

    }
}
