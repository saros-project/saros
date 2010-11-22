package de.fu_berlin.inf.dpp.stf.client.test.testcases.invitation;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestEditDuringInvitation extends STFTest {

    private static final Logger log = Logger
        .getLogger(TestEditDuringInvitation.class);

    /**
     * Preconditions:
     * <ol>
     * <li>alice (Host, Driver), alice share a java project with bob and carl.</li>
     * <li>bob (Observer)</li>
     * <li>carl (Observer)</li>
     * </ol>
     * 
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusicians() throws RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            MusicianConfigurationInfos.PORT_ALICE, MusicianConfigurationInfos.PORT_BOB,
            MusicianConfigurationInfos.PORT_CARL);
        alice = musicians.get(0);
        bob = musicians.get(1);
        carl = musicians.get(2);

        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        carl.workbench.resetSaros();
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        carl.workbench.resetWorkbench();
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    /**
     * 
     * Steps:
     * <ol>
     * <li>Alice invites Bob.</li>
     * <li>Bob accepts the invitation</li>
     * <li>Alice gives Bob driver capability</li>
     * <li>Alice invites Carl</li>
     * <li>Bob changes data during the running invtiation of Carl.</li>
     * </ol>
     * 
     * 
     * Expected Results:
     * <ol>
     * <li>All changes that Bob has done should be on Carl's side. There should
     * not be an inconsistency.</li>.
     * </ol>
     * 
     * @throws RemoteException
     */
    @Test
    public void testEditDuringInvitation() throws RemoteException {
        log.trace("starting testEditDuringInvitation, alice.buildSession");
        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);

        log.trace("alice.giveDriverRole");
        alice.sessionV.giveDriverRole(bob.state);

        assertTrue(bob.state.isDriver(alice.jid));

        log.trace("alice.inviteUser(carl");
        alice.sessionV.openInvitationInterface(carl.getBaseJid());

        log.trace("carl.confirmSessionInvitationWindowStep1");
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        carl.pEV.confirmFirstPageOfWizardSessionInvitation();

        log.trace("bob.setTextInJavaEditor");
        bob.editor.setTextInJavaEditorWithSave(CP1, PROJECT1, PKG1, CLS1);

        log.trace("carl.confirmSessionInvitationWindowStep2UsingNewproject");
        carl.pEV
            .confirmSecondPageOfWizardSessionInvitationUsingNewproject(PKG1);

        log.trace("getTextOfJavaEditor");
        String textFromCarl = carl.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        String textFormAlice = alice.editor.getTextOfJavaEditor(PROJECT1, PKG1,
            CLS1);
        assertTrue(textFromCarl.equals(textFormAlice));

        log.trace("testEditDuringInvitation done");
    }
}
