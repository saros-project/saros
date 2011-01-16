package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.saving;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestDriverSavesFiles extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * <li>Carl (Observer)</li>
     * <li>Dave (Observer in Follow-Mode)</li>
     * <li>Edna (Observer in Follow-Mode)</li>
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
        setUpWorkbenchs();
        setUpSaros();
        alice.file.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
        alice.file.newClass(PROJECT1, PKG1, CLS2);
        alice.file.newClass(PROJECT1, PKG1, CLS3);

        /*
         * build session with bob, carl, dave and edna simultaneously
         */
        alice.buildSessionDoneConcurrently(PROJECT1,
            TypeOfShareProject.SHARE_PROJECT, TypeOfCreateProject.NEW_PROJECT,
            edna, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();
        alice.followedBy(dave, edna);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(edna, bob, carl, dave);
    }

    @Before
    public void runBeforeEveryTest() {
        //
    }

    @After
    public void runAfterEveryTest() {
        //
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
        assertFalse(dave.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
        assertFalse(dave.editor.isClassDirty(PROJECT1, PKG1, CLS2,
            ID_JAVA_EDITOR));
        assertFalse(dave.editor.isClassDirty(PROJECT1, PKG1, CLS3,
            ID_JAVA_EDITOR));
        assertFalse(edna.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
        assertFalse(edna.editor.isClassDirty(PROJECT1, PKG1, CLS2,
            ID_JAVA_EDITOR));
        assertFalse(edna.editor.isClassDirty(PROJECT1, PKG1, CLS3,
            ID_JAVA_EDITOR));

        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        String dirtyClsContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT1, PKG1, CLS1);

        dave.editor.waitUntilJavaEditorContentSame(dirtyClsContentOfAlice,
            PROJECT1, PKG1, CLS1);
        assertTrue(dave.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
        edna.editor.waitUntilJavaEditorContentSame(dirtyClsContentOfAlice,
            PROJECT1, PKG1, CLS1);
        assertTrue(edna.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
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
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS2);
        String dirtyCls2ContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT1, PKG1, CLS2);
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
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS2);
        String dirtyCls2ContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT1, PKG1, CLS2);
        carl.pEV.openClassWith("Text Editor", PROJECT1, PKG1, CLS2);

        carl.editor.waitUntilJavaEditorContentSame(dirtyCls2ContentOfAlice,
            PROJECT1, PKG1, CLS2);
        assertTrue(carl.editor.isClassDirty(PROJECT1, PKG1, CLS2,
            ID_TEXT_EDITOR));
        String dirtyCls2ContentOfCarl = carl.editor.getTextOfJavaEditor(
            PROJECT1, PKG1, CLS2);
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
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS2);
        carl.pEV.openClassWith("Text Editor", PROJECT1, PKG1, CLS2);
        carl.editor.closeJavaEditorWithSave(CLS2);

        alice.editor.setTextInJavaEditorWithSave(CP2_CHANGE, PROJECT1, PKG1,
            CLS2);
        String dirtyCls2ChangeContentOfAlice = alice.editor
            .getTextOfJavaEditor(PROJECT1, PKG1, CLS2);
        dave.editor.waitUntilJavaEditorContentSame(
            dirtyCls2ChangeContentOfAlice, PROJECT1, PKG1, CLS2);
        assertFalse(dave.editor.isClassDirty(PROJECT1, PKG1, CLS2,
            ID_JAVA_EDITOR));
        edna.editor.waitUntilJavaEditorContentSame(
            dirtyCls2ChangeContentOfAlice, PROJECT1, PKG1, CLS2);
        assertFalse(edna.editor.isClassDirty(PROJECT1, PKG1, CLS2,
            ID_JAVA_EDITOR));

        // bob.state.waitUntilClassContentsSame(PROJECT1, PKG1, CLS2,
        // dirtyCls2ChangeContentOfAlice);
        String contentChangeOfBob = bob.editor.getClassContent(PROJECT1, PKG1,
            CLS2);
        System.out.println(contentChangeOfBob);
        System.out.println(dirtyCls2ChangeContentOfAlice);
        assertTrue(contentChangeOfBob.equals(dirtyCls2ChangeContentOfAlice));

        carl.pEV.openClass(PROJECT1, PKG1, CLS2);
        carl.editor.waitUntilJavaEditorContentSame(
            dirtyCls2ChangeContentOfAlice, PROJECT1, PKG1, CLS2);
        String contentOfCarl = carl.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS2);
        assertTrue(contentOfCarl.equals(dirtyCls2ChangeContentOfAlice));
        carl.editor.closeJavaEditorWithSave(CLS2);
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
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);
        alice.editor.closeJavaEditorWithSave(CLS1);
        String clsConentOfAlice = alice.editor.getClassContent(PROJECT1, PKG1,
            CLS1);

        dave.editor.waitUntilClassContentsSame(PROJECT1, PKG1, CLS1,
            clsConentOfAlice);
        assertFalse(dave.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));
        edna.editor.waitUntilJavaEditorContentSame(clsConentOfAlice, PROJECT1,
            PKG1, CLS1);
        assertFalse(edna.editor.isClassDirty(PROJECT1, PKG1, CLS1,
            ID_JAVA_EDITOR));

        String clsContentChangeOfBob = bob.editor.getTextOfJavaEditor(PROJECT1,
            PKG1, CLS1);
        assertFalse(clsContentChangeOfBob.equals(clsConentofBob));

        String clsContentChangeOfCarl = carl.editor.getTextOfJavaEditor(
            PROJECT1, PKG1, CLS1);
        assertTrue(clsContentChangeOfCarl.equals(clsConentOfAlice));
        carl.editor.closeJavaEditorWithSave(CLS1);
    }

}
