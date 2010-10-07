package de.fu_berlin.inf.dpp.stf.test.invitation;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.sarosswtbot.BotConfiguration;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.Musician;
import de.fu_berlin.inf.dpp.stf.sarosswtbot.SarosConstant;
import de.fu_berlin.inf.dpp.stf.test.InitMusician;

public class TestEditDuringInvitation {

    /**
     * Steps: 1. Alice invites Bob 2. Bob accepts the invitation 3. Alice gives
     * Bob driver capability 4. Alice invites Carl 5. Bob changes data during
     * the running invtiation of Carl.
     * 
     * Expected Results: All changes that Bob has done should be on Carl's side.
     * There should not be an inconsistency.
     */
    private static final Logger log = Logger
        .getLogger(TestEditDuringInvitation.class);
    private static final String PROJECT = BotConfiguration.PROJECTNAME;
    private static final String CLS = BotConfiguration.CLASSNAME;
    private static final String PKG = BotConfiguration.PACKAGENAME;
    private static Musician carl;
    private static Musician alice;
    private static Musician bob;

    @BeforeClass
    public static void initMusicians() throws RemoteException {
        alice = InitMusician.newAlice();
        bob = InitMusician.newBob();
        carl = InitMusician.newCarl();
        alice.bot.newJavaProjectWithClass(PROJECT, PKG, CLS);
    }

    @AfterClass
    public static void resetSaros() throws RemoteException {
        carl.bot.resetSaros();
        bob.bot.resetSaros();
        alice.bot.resetSaros();
    }

    @After
    public void cleanUp() throws RemoteException {
        carl.bot.resetWorkbench();
        bob.bot.resetWorkbench();
        alice.bot.resetWorkbench();
    }

    /**
     * @throws RemoteException
     */
    @Test
    public void testEditDuringInvitation() throws RemoteException {
        log.trace("starting testEditDuringInvitation, alice.buildSession");
        alice.buildSessionSequential(PROJECT,
            SarosConstant.CONTEXT_MENU_SHARE_PROJECT, bob);

        log.trace("alice.giveDriverRole");
        alice.bot.giveDriverRole(bob.getPlainJid());

        assertTrue(bob.state.isDriver(alice.jid));

        log.trace("alice.inviteUser(carl");
        alice.bot.comfirmInvitationWindow(carl.getPlainJid(), PROJECT);

        log.trace("carl.confirmSessionInvitationWindowStep1");
        // waitUntilShellActive(SarosConstant.SHELL_TITLE_SESSION_INVITATION);
        carl.bot.confirmSessionInvitationWindowStep1(alice.getPlainJid());

        log.trace("bob.setTextInJavaEditor");
        bob.bot.setTextInJavaEditor(BotConfiguration.CONTENTPATH, PROJECT, PKG,
            CLS);

        log.trace("carl.confirmSessionInvitationWindowStep2UsingNewproject");
        carl.bot.confirmSessionInvitationWindowStep2UsingNewproject(PKG);

        log.trace("getTextOfJavaEditor");
        String textFromCarl = carl.bot.getTextOfJavaEditor(PROJECT, PKG, CLS);
        String textFormAlice = alice.bot.getTextOfJavaEditor(PROJECT, PKG, CLS);
        assertTrue(textFromCarl.equals(textFormAlice));

        log.trace("testEditDuringInvitation done");
    }
}
