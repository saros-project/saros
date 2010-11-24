package de.fu_berlin.inf.dpp.stf.client.test.testcases.saving;

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

import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestDriverResetsFiles extends STFTest {

    /**
     * Preconditions:
     * <ol>
     * 
     * <li>alice1_fu (Host, Driver, all files are closed)</li>
     * <li>bob1_fu (Observer)</li>
     * <li>carl1_fu (Observer)</li>
     * <li>dave1_fu (Observer in Follow-Mode)</li>
     * <li>edna1_fu (Observer in Follow-Mode)</li>
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
            MusicianConfigurationInfos.PORT_ALICE, MusicianConfigurationInfos.PORT_BOB,
            MusicianConfigurationInfos.PORT_CARL, MusicianConfigurationInfos.PORT_DAVE,
            MusicianConfigurationInfos.PORT_EDNA);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);
        dave = musicians.get(3);
        edna = musicians.get(4);

        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);

        /*
         * build session with bob, carl, dave and edna simultaneously
         */
        alice.buildSessionConcurrently(PROJECT1, CONTEXT_MENU_SHARE_PROJECT,
            edna, bob, carl, dave);
        // alice.bot.waitUntilNoInvitationProgress();

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
     * <li>Alice edits a file without saving</li>
     * <li>Alice closes the file and declines saving the file</li>
     * <li>bob1_fu opens the file with an external text editor(don't need to do)
     * </li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li></li>
     * <li>Dave and Edna verify that the dirty flag of the file disappears and
     * that the conent is the same as Carl</li>
     * <li>bob verifies that th content of the file is the same as carl</li>
     * </ol>
     * 
     * @throws CoreException
     * @throws IOException
     */

    @Test
    public void testAliceResetsFile() throws IOException, CoreException {
        dave.sessionV.followThisUser(alice.state);
        edna.sessionV.followThisUser(alice.state);
        alice.editor.setTextInJavaEditorWithoutSave(CP1, PROJECT1, PKG1, CLS1);

        alice.editor.closejavaEditorWithoutSave(CLS1);
        dave.editor.confirmSaveSourceWindow(NO);
        edna.editor.confirmSaveSourceWindow(NO);

        String contentOfAlice = alice.state.getClassContent(PROJECT1, PKG1,
            CLS1);
        System.out.println("alice's class content" + contentOfAlice);
        String contentOfDave = dave.state.getClassContent(PROJECT1, PKG1, CLS1);
        System.out.println("dave's class content" + contentOfDave);
        String contentOfEdna = edna.state.getClassContent(PROJECT1, PKG1, CLS1);
        System.out.println("dave's class content" + contentOfDave);
        String contentOfBob = bob.state.getClassContent(PROJECT1, PKG1, CLS1);
        System.out.println("bob's class content" + contentOfBob);
        assertTrue(contentOfAlice.equals(contentOfDave));
        assertTrue(contentOfAlice.equals(contentOfEdna));
        assertTrue(contentOfAlice.equals(contentOfBob));
    }
}
