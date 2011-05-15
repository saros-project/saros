package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.saving;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestUserWithWriteAccessSavesFiles extends STFTest {

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
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB, TypeOfTester.CARL,
            TypeOfTester.DAVE, TypeOfTester.EDNA);
        setUpWorkbench();
        setUpSaros();
        alice.superBot().views().packageExplorerView().tree().newC()
            .javaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS2);
        alice.superBot().views().packageExplorerView().tree().newC()
            .cls(PROJECT1, PKG1, CLS3);

        /*
         * build session with bob, carl, dave and edna simultaneously
         */
        buildSessionConcurrently(PROJECT1, TypeOfCreateProject.NEW_PROJECT,
            alice, edna, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();
        setFollowMode(alice, dave, edna);
        dave.superBot().views().sarosView().selectParticipant(alice.getJID())
            .waitUntilIsFollowing();
        edna.superBot().views().sarosView().selectParticipant(alice.getJID())
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
        // assertFalse(dave.bot().editor(CLS1_SUFFIX).isFileDirty());
        // assertFalse(dave.bot().editor(CLS2_SUFFIX).isFileDirty());
        assertFalse(dave.remoteBot().editor(CLS3_SUFFIX).isDirty());
        // assertFalse(edna.bot().editor(CLS1_SUFFIX).isFileDirty());
        // assertFalse(edna.bot().editor(CLS2_SUFFIX).isFileDirty());
        assertFalse(edna.remoteBot().editor(CLS3_SUFFIX).isDirty());

        alice.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String dirtyClsContentOfAlice = alice.remoteBot().editor(CLS1_SUFFIX)
            .getText();

        dave.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        dave.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(dave.remoteBot().editor(CLS1_SUFFIX).isDirty());

        edna.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        edna.remoteBot().editor(CLS1_SUFFIX)
            .waitUntilIsTextSame(dirtyClsContentOfAlice);
        assertTrue(edna.remoteBot().editor(CLS1_SUFFIX).isDirty());
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
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS2).open();
        alice.remoteBot().editor(CLS2_SUFFIX).setTextWithoutSave(CP1);
        String dirtyCls2ContentOfAlice = alice.remoteBot().editor(CLS2_SUFFIX)
            .getText();
        String cls2ContentOfAlice = alice.superBot().views()
            .packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS2));
        String cls2ContentOfBob = bob.superBot().views().packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS2));
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
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS2).open();
        alice.remoteBot().editor(CLS2_SUFFIX).setTextWithoutSave(CP1);
        String dirtyCls2ContentOfAlice = alice.remoteBot().editor(CLS2_SUFFIX)
            .getText();
        carl.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS2)
            .openWith(CM_OPEN_WITH_TEXT_EDITOR);

        carl.remoteBot().editor(CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ContentOfAlice);
        assertTrue(carl.remoteBot().editor(CLS2_SUFFIX).isDirty());
        String dirtyCls2ContentOfCarl = carl.remoteBot().editor(CLS2_SUFFIX)
            .getText();
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
        alice.remoteBot().editor(CLS2_SUFFIX).setTextWithoutSave(CP1);
        carl.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS2)
            .openWith(CM_OPEN_WITH_TEXT_EDITOR);
        carl.remoteBot().editor(CLS2 + SUFFIX_JAVA).closeWithSave();

        alice.remoteBot().editor(CLS2_SUFFIX).setTexWithSave(CP2_CHANGE);
        String dirtyCls2ChangeContentOfAlice = alice.remoteBot().editor(CLS2_SUFFIX)
            .getText();
        dave.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS2).open();
        dave.remoteBot().editor(CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ChangeContentOfAlice);
        assertFalse(dave.remoteBot().editor(CLS2_SUFFIX).isDirty());

        edna.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS2).open();
        edna.remoteBot().editor(CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ChangeContentOfAlice);
        assertFalse(edna.remoteBot().editor(CLS2_SUFFIX).isDirty());

        // bob.state.waitUntilClassContentsSame(PROJECT1, PKG1, CLS2,
        // dirtyCls2ChangeContentOfAlice);
        String contentChangeOfBob = bob.superBot().views()
            .packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS2));
        System.out.println(contentChangeOfBob);
        System.out.println(dirtyCls2ChangeContentOfAlice);
        assertTrue(contentChangeOfBob.equals(dirtyCls2ChangeContentOfAlice));

        carl.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS2).open();
        carl.remoteBot().editor(CLS2_SUFFIX)
            .waitUntilIsTextSame(dirtyCls2ChangeContentOfAlice);
        String contentOfCarl = carl.remoteBot().editor(CLS2_SUFFIX).getText();
        assertTrue(contentOfCarl.equals(dirtyCls2ChangeContentOfAlice));
        carl.remoteBot().editor(CLS2 + SUFFIX_JAVA).closeWithSave();
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
        String clsConentofBob = bob.superBot().views().packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1));
        alice.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        alice.remoteBot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        alice.remoteBot().editor(CLS1_SUFFIX).closeWithSave();
        String clsConentOfAlice = alice.superBot().views()
            .packageExplorerView()
            .getFileContent(getClassPath(PROJECT1, PKG1, CLS1));

        dave.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        dave.superBot()
            .views()
            .packageExplorerView()
            .waitUntilFileContentSame(clsConentOfAlice,
                getClassPath(PROJECT1, PKG1, CLS1));
        assertFalse(dave.remoteBot().editor(CLS1_SUFFIX).isDirty());

        edna.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        edna.remoteBot().editor(CLS1_SUFFIX).waitUntilIsTextSame(clsConentOfAlice);
        assertFalse(edna.remoteBot().editor(CLS1_SUFFIX).isDirty());

        bob.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        String clsContentChangeOfBob = bob.remoteBot().editor(CLS1_SUFFIX).getText();
        assertFalse(clsContentChangeOfBob.equals(clsConentofBob));

        carl.superBot().views().packageExplorerView()
            .selectClass(PROJECT1, PKG1, CLS1).open();
        String clsContentChangeOfCarl = carl.remoteBot().editor(CLS1_SUFFIX)
            .getText();
        assertTrue(clsContentChangeOfCarl.equals(clsConentOfAlice));
        carl.remoteBot().editor(CLS1 + SUFFIX_JAVA).closeWithSave();
    }
}
