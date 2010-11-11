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
import de.fu_berlin.inf.dpp.stf.server.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.server.SarosConstant;

public class TestDriverSavesFiles {
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;

    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String CLS2 = BotConfiguration.CLASSNAME2;
    private static final String CLS3 = BotConfiguration.CLASSNAME3;

    private static final String CP = BotConfiguration.CONTENTPATH;
    private static final String CP2_change = BotConfiguration.CONTENTCHANGEPATH2;

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

        alice.mainMenu.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.mainMenu.newClass(PROJECT, PKG, CLS2);
        alice.mainMenu.newClass(PROJECT, PKG, CLS3);

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
        assertFalse(dave.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(dave.eclipseEditor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(dave.eclipseEditor.isClassDirty(PROJECT, PKG, CLS3,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.eclipseEditor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.eclipseEditor.isClassDirty(PROJECT, PKG, CLS3,
            SarosConstant.ID_JAVA_EDITOR));

        alice.eclipseEditor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG,
            CLS);
        String dirtyClsContentOfAlice = alice.eclipseEditor
            .getTextOfJavaEditor(PROJECT, PKG, CLS);

        dave.eclipseEditor.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfAlice);
        assertTrue(dave.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        edna.eclipseEditor.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfAlice);
        assertTrue(edna.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
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
        alice.eclipseEditor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG,
            CLS2);
        String dirtyCls2ContentOfAlice = alice.eclipseEditor
            .getTextOfJavaEditor(PROJECT, PKG, CLS2);
        String cls2ContentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS2);
        String cls2ContentOfBob = bob.eclipseState.getClassContent(PROJECT,
            PKG, CLS2);
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
        alice.eclipseEditor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG,
            CLS2);
        String dirtyCls2ContentOfAlice = alice.eclipseEditor
            .getTextOfJavaEditor(PROJECT, PKG, CLS2);
        carl.packageExplorerV.openClassWith("Text Editor", PROJECT, PKG, CLS2);

        carl.eclipseEditor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ContentOfAlice);
        assertTrue(carl.eclipseEditor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_TEXT_EDITOR));
        String dirtyCls2ContentOfCarl = carl.eclipseEditor.getTextOfJavaEditor(
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
        alice.eclipseEditor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG,
            CLS2);
        carl.packageExplorerV.openClassWith("Text Editor", PROJECT, PKG, CLS2);
        carl.eclipseEditor.closeJavaEditorWithSave(CLS2);

        alice.eclipseEditor.setTextInJavaEditorWithSave(CP2_change, PROJECT,
            PKG, CLS2);
        String dirtyCls2ChangeContentOfAlice = alice.eclipseEditor
            .getTextOfJavaEditor(PROJECT, PKG, CLS2);
        dave.eclipseEditor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        assertFalse(dave.eclipseEditor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        edna.eclipseEditor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        assertFalse(edna.eclipseEditor.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));

        bob.eclipseState.waitUntilClassContentsSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        String contentChangeOfBob = bob.eclipseState.getClassContent(PROJECT,
            PKG, CLS2);
        assertTrue(contentChangeOfBob.equals(dirtyCls2ChangeContentOfAlice));

        carl.packageExplorerV.openClass(PROJECT, PKG, CLS2);
        carl.eclipseEditor.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        String contentOfCarl = carl.eclipseEditor.getTextOfJavaEditor(PROJECT,
            PKG, CLS2);
        assertTrue(contentOfCarl.equals(dirtyCls2ChangeContentOfAlice));
        carl.eclipseEditor.closeJavaEditorWithSave(CLS2);
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
        String clsConentofBob = bob.eclipseState.getClassContent(PROJECT, PKG,
            CLS);
        alice.eclipseEditor.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG,
            CLS);
        alice.eclipseEditor.closeJavaEditorWithSave(CLS);
        String clsConentOfAlice = alice.eclipseState.getClassContent(PROJECT,
            PKG, CLS);

        dave.eclipseState.waitUntilClassContentsSame(PROJECT, PKG, CLS,
            clsConentOfAlice);
        assertFalse(dave.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        edna.eclipseEditor.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            clsConentOfAlice);
        assertFalse(edna.eclipseEditor.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        String clsContentChangeOfBob = bob.eclipseEditor.getTextOfJavaEditor(
            PROJECT, PKG, CLS);
        assertFalse(clsContentChangeOfBob.equals(clsConentofBob));

        String clsContentChangeOfCarl = carl.eclipseEditor.getTextOfJavaEditor(
            PROJECT, PKG, CLS);
        assertTrue(clsContentChangeOfCarl.equals(clsConentOfAlice));
        carl.eclipseEditor.closeJavaEditorWithSave(CLS);
    }

}
