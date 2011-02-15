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
        alice.fileM.newJavaProjectWithClasses(PROJECT1, PKG1, CLS1);
        alice.fileM.newClass(PROJECT1, PKG1, CLS2);
        alice.fileM.newClass(PROJECT1, PKG1, CLS3);

        /*
         * build session with bob, carl, dave and edna simultaneously
         */
        buildSessionConcurrently(VIEW_PACKAGE_EXPLORER, PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            alice, edna, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();
        setFollowMode(alice, dave, edna);
        dave.sarosSessionV.waitUntilIsFollowingBuddy(alice.jid);
        edna.sarosSessionV.waitUntilIsFollowingBuddy(alice.jid);

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
        assertFalse(dave.bot().editor(CLS3_SUFFIX).isFileDirty());
        // assertFalse(edna.bot().editor(CLS1_SUFFIX).isFileDirty());
        // assertFalse(edna.bot().editor(CLS2_SUFFIX).isFileDirty());
        assertFalse(edna.bot().editor(CLS3_SUFFIX).isFileDirty());

        alice.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        String dirtyClsContentOfAlice = alice.bot().editor(CLS1_SUFFIX)
            .getText();

        dave.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        dave.bot().editor(CLS1_SUFFIX)
            .waitUntilContentSame(dirtyClsContentOfAlice);
        assertTrue(dave.bot().editor(CLS1_SUFFIX).isFileDirty());

        edna.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        edna.bot().editor(CLS1_SUFFIX)
            .waitUntilContentSame(dirtyClsContentOfAlice);
        assertTrue(edna.bot().editor(CLS1_SUFFIX).isFileDirty());
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
        alice.bot().editor(CLS2_SUFFIX).setTextWithoutSave(CP1);
        String dirtyCls2ContentOfAlice = alice.bot().editor(CLS2_SUFFIX)
            .getText();
        String cls2ContentOfAlice = alice.editor.getClassContent(PROJECT1,
            PKG1, CLS2);
        String cls2ContentOfBob = bob.editor.getClassContent(PROJECT1, PKG1,
            CLS2);
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
        alice.bot().editor(CLS2_SUFFIX).setTextWithoutSave(CP1);
        String dirtyCls2ContentOfAlice = alice.bot().editor(CLS2_SUFFIX)
            .getText();
        carl.openC.openClassWith(VIEW_PACKAGE_EXPLORER, "Text Editor",
            PROJECT1, PKG1, CLS2);

        carl.bot().editor(CLS2_SUFFIX)
            .waitUntilContentSame(dirtyCls2ContentOfAlice);
        assertTrue(carl.bot().editor(CLS2_SUFFIX).isFileDirty());
        String dirtyCls2ContentOfCarl = carl.bot().editor(CLS2_SUFFIX)
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
        alice.bot().editor(CLS2_SUFFIX).setTextWithoutSave(CP1);
        carl.openC.openClassWith(VIEW_PACKAGE_EXPLORER, "Text Editor",
            PROJECT1, PKG1, CLS2);
        carl.bot().editor(CLS2 + SUFFIX_JAVA).closeAndSave();

        alice.bot().editor(CLS2_SUFFIX).setTextAndSave(CP2_CHANGE);
        String dirtyCls2ChangeContentOfAlice = alice.bot().editor(CLS2_SUFFIX)
            .getText();
        dave.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS2);
        dave.bot().editor(CLS2_SUFFIX)
            .waitUntilContentSame(dirtyCls2ChangeContentOfAlice);
        assertFalse(dave.bot().editor(CLS2_SUFFIX).isFileDirty());

        edna.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS2);
        edna.bot().editor(CLS2_SUFFIX)
            .waitUntilContentSame(dirtyCls2ChangeContentOfAlice);
        assertFalse(edna.bot().editor(CLS2_SUFFIX).isFileDirty());

        // bob.state.waitUntilClassContentsSame(PROJECT1, PKG1, CLS2,
        // dirtyCls2ChangeContentOfAlice);
        String contentChangeOfBob = bob.editor.getClassContent(PROJECT1, PKG1,
            CLS2);
        System.out.println(contentChangeOfBob);
        System.out.println(dirtyCls2ChangeContentOfAlice);
        assertTrue(contentChangeOfBob.equals(dirtyCls2ChangeContentOfAlice));

        carl.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS2);
        carl.bot().editor(CLS2_SUFFIX)
            .waitUntilContentSame(dirtyCls2ChangeContentOfAlice);
        String contentOfCarl = carl.bot().editor(CLS2_SUFFIX).getText();
        assertTrue(contentOfCarl.equals(dirtyCls2ChangeContentOfAlice));
        carl.bot().editor(CLS2 + SUFFIX_JAVA).closeAndSave();
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
        String clsConentofBob = bob.editor
            .getClassContent(PROJECT1, PKG1, CLS1);
        alice.bot().editor(CLS1_SUFFIX).setTextWithoutSave(CP1);
        alice.bot().editor(CLS1 + SUFFIX_JAVA).closeAndSave();
        String clsConentOfAlice = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);

        dave.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        dave.editor.waitUntilClassContentsSame(PROJECT1, PKG1, CLS1,
            clsConentOfAlice);
        assertFalse(dave.bot().editor(CLS1_SUFFIX).isFileDirty());

        edna.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        edna.bot().editor(CLS1_SUFFIX).waitUntilContentSame(clsConentOfAlice);
        assertFalse(edna.bot().editor(CLS1_SUFFIX).isFileDirty());

        bob.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        String clsContentChangeOfBob = bob.bot().editor(CLS1_SUFFIX).getText();
        assertFalse(clsContentChangeOfBob.equals(clsConentofBob));

        carl.openC.openClass(VIEW_PACKAGE_EXPLORER, PROJECT1, PKG1, CLS1);
        String clsContentChangeOfCarl = carl.bot().editor(CLS1_SUFFIX)
            .getText();
        assertTrue(clsContentChangeOfCarl.equals(clsConentOfAlice));
        carl.bot().editor(CLS1 + SUFFIX_JAVA).closeAndSave();
    }
}
