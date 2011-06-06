package de.fu_berlin.inf.dpp.stf.test.saving;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.DAVE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.EDNA;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.CM_OPEN_WITH_TEXT_EDITOR;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.SUFFIX_JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;
import de.fu_berlin.inf.dpp.stf.test.Constants;

public class UserWithWriteAccessSavesFilesTest extends StfTestCase {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * <li>Carl (Read-Only Access)</li>
     * <li>Dave (Read-Only in Follow-Mode)</li>
     * <li>Edna (Read-Only in Follow-Mode)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(ALICE, BOB, CARL, DAVE, EDNA);
        setUpWorkbench();
        setUpSaros();
        ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .tree()
            .newC()
            .javaProjectWithClasses(Constants.PROJECT1, Constants.PKG1,
                Constants.CLS1);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS2);
        ALICE.superBot().views().packageExplorerView().tree().newC()
            .cls(Constants.PROJECT1, Constants.PKG1, Constants.CLS3);

        /*
         * build session with BOB, CARL, DAVE and EDNA simultaneously
         */
        Util.buildSessionConcurrently(Constants.PROJECT1,
            TypeOfCreateProject.NEW_PROJECT, ALICE, EDNA, BOB, CARL, DAVE);
        // ALICE.bot.waitUntilNoInvitationProgress();
        Util.setFollowMode(ALICE, DAVE, EDNA);
        DAVE.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .waitUntilIsFollowing();
        EDNA.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .waitUntilIsFollowing();

    }

    /**
     * Steps:
     * <ol>
     * <li>Alice makes changes in CLS without saving the files.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Dave and Edna check that the modified files have a dirty flag (*).</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */

    @Test
    public void testExistDirtyFlagByDaveAndEdnaDuringAlicMakeChange()
        throws IOException, CoreException {
        // assertFalse(DAVE.bot().editor(CLS1_SUFFIX).isFileDirty());
        // assertFalse(DAVE.bot().editor(CLS2_SUFFIX).isFileDirty());
        assertFalse(DAVE.remoteBot().editor(Constants.CLS3_SUFFIX).isDirty());
        // assertFalse(EDNA.bot().editor(CLS1_SUFFIX).isFileDirty());
        // assertFalse(EDNA.bot().editor(CLS2_SUFFIX).isFileDirty());
        assertFalse(EDNA.remoteBot().editor(Constants.CLS3_SUFFIX).isDirty());

        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextWithoutSave(Constants.CP1);
        String dirtyClsContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS1_SUFFIX).getText();

        DAVE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        DAVE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());

        EDNA.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        EDNA.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(EDNA.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());
    }

    /**
     * <ol>
     * <li>Alice makes changes in CLS2 without saving the files.</li>
     * <li>Bob opens one of the edited files in an external editor.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Bob should see no changes from Alice in the external opened file.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */
    @Test
    public void testNoChangeByExternalEditorByBob() throws IOException,
        CoreException {
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .open();
        ALICE.remoteBot().editor(Constants.CLS2_SUFFIX)
            .setTextWithoutSave(Constants.CP1);
        String dirtyCls2ContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS2_SUFFIX).getText();
        String cls2ContentOfAlice = ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS2));
        String cls2ContentOfBob = BOB
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS2));
        assertFalse(cls2ContentOfBob.equals(dirtyCls2ContentOfAlice));
        assertTrue(cls2ContentOfBob.equals(cls2ContentOfAlice));
    }

    /**
     * <ol>
     * <li>Alice makes changes in CLS2 without saving the files.</li>
     * <li>Carl opens one of the edited files with the Eclipse editor.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Carl should see the changes from Alice and the dirty flag on the
     * file.</li>
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testChangeByEclipseEditorByCarl() throws RemoteException {
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .open();
        ALICE.remoteBot().editor(Constants.CLS2_SUFFIX)
            .setTextWithoutSave(Constants.CP1);
        String dirtyCls2ContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS2_SUFFIX).getText();
        CARL.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .openWith(CM_OPEN_WITH_TEXT_EDITOR);

        CARL.remoteBot().editor(Constants.CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ContentOfAlice);
        assertTrue(CARL.remoteBot().editor(Constants.CLS2_SUFFIX).isDirty());
        String dirtyCls2ContentOfCarl = CARL.remoteBot()
            .editor(Constants.CLS2_SUFFIX).getText();
        assertTrue(dirtyCls2ContentOfCarl.equals(dirtyCls2ContentOfAlice));
    }

    /**
     * <ol>
     * <li>Alice makes changes in CLS2 without saving the files.</li>
     * <li>Carl opens one of the edited files with the Eclipse editor.</li>
     * <li>Carl closes the file in Eclipse.</li>
     * <li>Alice makes more changes to the file that was opened by Carl and
     * saves it.</li>
     * <li>Alice closes a changed (dirty) file and confirms that it will saved.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Dave and Edna verify that the dirty flag disappears. Bob verifies
     * that the file content has changed with an external editor. Carl opens the
     * file in Eclipse, verifies the correct content and closes the file.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */

    @Test
    public void testChangingInClosedFile() throws IOException, CoreException {
        ALICE.remoteBot().editor(Constants.CLS2_SUFFIX)
            .setTextWithoutSave(Constants.CP1);
        CARL.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .openWith(CM_OPEN_WITH_TEXT_EDITOR);
        CARL.remoteBot().editor(Constants.CLS2 + SUFFIX_JAVA).closeWithSave();

        ALICE.remoteBot().editor(Constants.CLS2_SUFFIX)
            .setTexWithSave(Constants.CP2_CHANGE);
        String dirtyCls2ChangeContentOfAlice = ALICE.remoteBot()
            .editor(Constants.CLS2_SUFFIX).getText();
        DAVE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .open();
        DAVE.remoteBot().editor(Constants.CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ChangeContentOfAlice);
        assertFalse(DAVE.remoteBot().editor(Constants.CLS2_SUFFIX).isDirty());

        EDNA.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .open();
        EDNA.remoteBot().editor(Constants.CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ChangeContentOfAlice);
        assertFalse(EDNA.remoteBot().editor(Constants.CLS2_SUFFIX).isDirty());

        // BOB.state.waitUntilClassContentsSame(PROJECT1, PKG1, CLS2,
        // dirtyCls2ChangeContentOfAlice);
        String contentChangeOfBob = BOB
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS2));
        System.out.println(contentChangeOfBob);
        System.out.println(dirtyCls2ChangeContentOfAlice);
        assertTrue(contentChangeOfBob.equals(dirtyCls2ChangeContentOfAlice));

        CARL.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS2)
            .open();
        CARL.remoteBot().editor(Constants.CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ChangeContentOfAlice);
        String contentOfCarl = CARL.remoteBot().editor(Constants.CLS2_SUFFIX)
            .getText();
        assertTrue(contentOfCarl.equals(dirtyCls2ChangeContentOfAlice));
        CARL.remoteBot().editor(Constants.CLS2 + SUFFIX_JAVA).closeWithSave();
    }

    /**
     * <ol>
     * <li>Alice makes changes in CLS without saving the files.</li>
     * <li>Alice closes a changed (dirty) file and confirms that it will saved.</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>Dave and Edna verify that the dirty flag disappears. Bob verifies
     * that the file content has changed with an external editor. Carl opens the
     * file in Eclipse, verifies the correct content and closes the file.</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */
    @Test
    public void testCloseDirtyFileByAlice() throws RemoteException,
        IOException, CoreException {
        String clsConentofBob = BOB
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));
        ALICE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX)
            .setTextWithoutSave(Constants.CP1);
        ALICE.remoteBot().editor(Constants.CLS1_SUFFIX).closeWithSave();
        String clsConentOfAlice = ALICE
            .superBot()
            .views()
            .packageExplorerView()
            .getFileContent(
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));

        DAVE.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        DAVE.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileContentSame(
                clsConentOfAlice,
                Util.getClassPath(Constants.PROJECT1, Constants.PKG1,
                    Constants.CLS1));
        assertFalse(DAVE.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());

        EDNA.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        EDNA.remoteBot().editor(Constants.CLS1_SUFFIX)
            .waitUntilIsTextSame(clsConentOfAlice);
        assertFalse(EDNA.remoteBot().editor(Constants.CLS1_SUFFIX).isDirty());

        BOB.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        String clsContentChangeOfBob = BOB.remoteBot()
            .editor(Constants.CLS1_SUFFIX).getText();
        assertFalse(clsContentChangeOfBob.equals(clsConentofBob));

        CARL.superBot().views().packageExplorerView()
            .selectClass(Constants.PROJECT1, Constants.PKG1, Constants.CLS1)
            .open();
        String clsContentChangeOfCarl = CARL.remoteBot()
            .editor(Constants.CLS1_SUFFIX).getText();
        assertTrue(clsContentChangeOfCarl.equals(clsConentOfAlice));
        CARL.remoteBot().editor(Constants.CLS1 + SUFFIX_JAVA).closeWithSave();
    }
}
