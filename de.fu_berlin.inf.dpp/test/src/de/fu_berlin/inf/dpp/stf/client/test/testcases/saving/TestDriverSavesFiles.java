package de.fu_berlin.inf.dpp.stf.client.test.testcases.saving;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestDriverSavesFiles extends STFTest {

    private static Musician alice;
    private static Musician bob;
    private static Musician carl;
    private static Musician dave;
    private static Musician edna;

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
    public static void initMusican() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            BotConfiguration.PORT_ALICE, BotConfiguration.PORT_BOB,
            BotConfiguration.PORT_CARL, BotConfiguration.PORT_DAVE,
            BotConfiguration.PORT_EDNA);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        edna = musicians.get(4);

        alice.pEV.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.pEV.newClass(PROJECT, PKG, CLS2);
        alice.pEV.newClass(PROJECT, PKG, CLS3);

        /*
         * build session with bob, carl, dave and edna simultaneously
         */
        alice.buildSessionConcurrently(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, edna, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();

        dave.sessionV.followThisUser(alice.state);
        edna.sessionV.followThisUser(alice.state);
    }

    /**
     * make sure, all opened xmppConnects, popup windows and editor should be
     * closed. make sure, all existed projects should be deleted. if you need
     * some more after class condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        carl.workbench.resetSaros();
        dave.workbench.resetSaros();
        edna.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        carl.workbench.resetWorkbench();
        dave.workbench.resetWorkbench();
        edna.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
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
        assertFalse(dave.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(dave.editor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(dave.editor.isClassDirty(PROJECT, PKG, CLS3,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.editor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.editor.isClassDirty(PROJECT, PKG, CLS3,
            SarosConstant.ID_JAVA_EDITOR));

        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT, PKG, CLS);

        dave.editor.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfAlice);
        assertTrue(dave.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        edna.editor.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfAlice);
        assertTrue(edna.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
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
        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS2);
        String dirtyCls2ContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT, PKG, CLS2);
        String cls2ContentOfAlice = alice.state.getClassContent(PROJECT, PKG,
            CLS2);
        String cls2ContentOfBob = bob.state.getClassContent(PROJECT, PKG, CLS2);
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
        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS2);
        String dirtyCls2ContentOfAlice = alice.editor.getTextOfJavaEditor(
            PROJECT, PKG, CLS2);
        carl.pEV.openFileWith("Text Editor", getClassNodes(PROJECT, PKG, CLS2));

        carl.editor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ContentOfAlice);
        assertTrue(carl.editor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_TEXT_EDITOR));
        String dirtyCls2ContentOfCarl = carl.editor.getTextOfJavaEditor(
            PROJECT, PKG, CLS2);
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
        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS2);
        carl.pEV.openFileWith("Text Editor", getClassNodes(PROJECT, PKG, CLS2));
        carl.editor.closeJavaEditorWithSave(CLS2);

        alice.editor
            .setTextInJavaEditorWithSave(CP2_CHANGE, PROJECT, PKG, CLS2);
        String dirtyCls2ChangeContentOfAlice = alice.editor
            .getTextOfJavaEditor(PROJECT, PKG, CLS2);
        dave.editor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        assertFalse(dave.editor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        edna.editor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        assertFalse(edna.editor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));

        bob.state.waitUntilClassContentsSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        String contentChangeOfBob = bob.state.getClassContent(PROJECT, PKG,
            CLS2);
        assertTrue(contentChangeOfBob.equals(dirtyCls2ChangeContentOfAlice));

        carl.pEV.openFile(getClassNodes(PROJECT, PKG, CLS2));
        carl.editor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        String contentOfCarl = carl.editor.getTextOfJavaEditor(PROJECT, PKG,
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
        String clsConentofBob = bob.state.getClassContent(PROJECT, PKG, CLS);
        alice.editor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        alice.editor.closeJavaEditorWithSave(CLS);
        String clsConentOfAlice = alice.state
            .getClassContent(PROJECT, PKG, CLS);

        dave.state.waitUntilClassContentsSame(PROJECT, PKG, CLS,
            clsConentOfAlice);
        assertFalse(dave.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        edna.editor.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            clsConentOfAlice);
        assertFalse(edna.editor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        String clsContentChangeOfBob = bob.editor.getTextOfJavaEditor(PROJECT,
            PKG, CLS);
        assertFalse(clsContentChangeOfBob.equals(clsConentofBob));

        String clsContentChangeOfCarl = carl.editor.getTextOfJavaEditor(
            PROJECT, PKG, CLS);
        assertTrue(clsContentChangeOfCarl.equals(clsConentOfAlice));
        carl.editor.closeJavaEditorWithSave(CLS);
    }

}
