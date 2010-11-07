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

        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
        alice.bot.newClass(PROJECT, PKG, CLS2);
        alice.bot.newClass(PROJECT, PKG, CLS3);

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
        bob.bot.resetSaros();
        carl.bot.resetSaros();
        dave.bot.resetSaros();
        edna.bot.resetSaros();
        alice.bot.resetSaros();
    }

    /**
     * make sure,all opened popup windows and editor should be closed. if you
     * need some more after condition for your tests, please add it.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.bot.resetWorkbench();
        carl.bot.resetWorkbench();
        dave.bot.resetWorkbench();
        edna.bot.resetWorkbench();
        alice.bot.resetWorkbench();
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
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS3,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS3,
            SarosConstant.ID_JAVA_EDITOR));

        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        String dirtyClsContentOfAlice = alice.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS);

        dave.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfAlice);
        assertTrue(dave.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        edna.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS,
            dirtyClsContentOfAlice);
        assertTrue(edna.bot.isClassDirty(PROJECT, PKG, CLS,
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
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS2);
        String dirtyCls2ContentOfAlice = alice.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS2);
        String cls2ContentOfAlice = alice.bot.getClassContent(PROJECT, PKG,
            CLS2);
        String cls2ContentOfBob = bob.bot.getClassContent(PROJECT, PKG, CLS2);
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
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS2);
        String dirtyCls2ContentOfAlice = alice.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS2);
        carl.bot.openClassWith("Text Editor", PROJECT, PKG, CLS2);

        carl.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ContentOfAlice);
        assertTrue(carl.bot.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_TEXT_EDITOR));
        String dirtyCls2ContentOfCarl = carl.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS2);
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
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS2);
        carl.bot.openClassWith("Text Editor", PROJECT, PKG, CLS2);
        carl.bot.closeJavaEditorWithSave(CLS2);

        alice.bot.setTextInJavaEditorWithSave(CP2_change, PROJECT, PKG, CLS2);
        String dirtyCls2ChangeContentOfAlice = alice.bot.getTextOfJavaEditor(
            PROJECT, PKG, CLS2);
        dave.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));
        edna.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS2,
            SarosConstant.ID_JAVA_EDITOR));

        bob.bot.waitUntilClassContentsSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        String contentChangeOfBob = bob.bot.getClassContent(PROJECT, PKG, CLS2);
        assertTrue(contentChangeOfBob.equals(dirtyCls2ChangeContentOfAlice));

        carl.bot.openClass(PROJECT, PKG, CLS2);
        carl.bot.waitUntilEditorContentSame(PROJECT, PKG, CLS2,
            dirtyCls2ChangeContentOfAlice);
        String contentOfCarl = carl.bot.getTextOfJavaEditor(PROJECT, PKG, CLS2);
        assertTrue(contentOfCarl.equals(dirtyCls2ChangeContentOfAlice));
        carl.bot.closeJavaEditorWithSave(CLS2);
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
        String clsConentofBob = bob.bot.getClassContent(PROJECT, PKG, CLS);
        alice.bot.setTextInJavaEditorWithoutSave(CP, PROJECT, PKG, CLS);
        alice.bot.closeJavaEditorWithSave(CLS);
        String clsConentOfAlice = alice.bot.getClassContent(PROJECT, PKG, CLS);

        dave.bot
            .waitUntilClassContentsSame(PROJECT, PKG, CLS, clsConentOfAlice);
        assertFalse(dave.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));
        edna.bot
            .waitUntilEditorContentSame(PROJECT, PKG, CLS, clsConentOfAlice);
        assertFalse(edna.bot.isClassDirty(PROJECT, PKG, CLS,
            SarosConstant.ID_JAVA_EDITOR));

        String clsContentChangeOfBob = bob.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS);
        assertFalse(clsContentChangeOfBob.equals(clsConentofBob));

        String clsContentChangeOfCarl = carl.bot.getTextOfJavaEditor(PROJECT,
            PKG, CLS);
        assertTrue(clsContentChangeOfCarl.equals(clsConentOfAlice));
        carl.bot.closeJavaEditorWithSave(CLS);
    }

}
