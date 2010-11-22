package de.fu_berlin.inf.dpp.stf.client.test.testcases.chatview;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.MusicianConfigurationInfos;
import de.fu_berlin.inf.dpp.stf.client.Musician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.InitMusician;
import de.fu_berlin.inf.dpp.stf.client.test.helpers.STFTest;

public class TestChatViewFunctions extends STFTest {

    String message = "Hello Bob";

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Driver)</li>
     * <li>Bob (Observer)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */
    @BeforeClass
    public static void initMusicans() throws AccessException, RemoteException,
        InterruptedException {
        /*
         * initialize the musicians simultaneously
         */
        List<Musician> musicians = InitMusician.initMusiciansConcurrently(
            MusicianConfigurationInfos.PORT_ALICE, MusicianConfigurationInfos.PORT_BOB);
        alice = musicians.get(0);
        bob = musicians.get(1);

        alice.pEV.newJavaProjectWithClass(PROJECT1, PKG1, CLS1);

        /*
         * alice build session with bob.
         */
        alice.shareProjectWithDone(PROJECT1, CONTEXT_MENU_SHARE_PROJECT, bob);
    }

    /**
     * make sure, all opened xmppConnects, pop up windows and editor should be
     * closed.
     * <p>
     * make sure, all existed projects should be deleted.
     * 
     * @throws RemoteException
     */
    @AfterClass
    public static void resetSaros() throws RemoteException {
        bob.workbench.resetSaros();
        alice.workbench.resetSaros();
    }

    /**
     * make sure,all opened pop up windows and editor should be closed.
     * 
     * @throws RemoteException
     */
    @After
    public void cleanUp() throws RemoteException {
        bob.workbench.resetWorkbench();
        alice.workbench.resetWorkbench();
    }

    /**
     * Steps:
     * <ol>
     * <li>alice open the chat view</li>
     * <li>bob open the chat view</li>
     * <li>alice sends IM to bob (both users should have beep button active by
     * default)</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>The chat should have automatically connected. alice should see e.g.
     * <p>
     * "You&nbsp;&nbsp;&nbsp;31/10/10 17:19"
     * <p>
     * "... joined the chat"
     * <p>
     * "bob1_fu@saros-con.imp.fu-berlin.de"
     * <p>
     * "... joined the chat"</li>
     * <li>The chat should have automatically connected. bob should see e.g.
     * <p>
     * "You&nbsp;&nbsp;&nbsp;31/10/10 17:19"
     * <p>
     * "... joined the chat"
     * <p>
     * "alice1_fu@saros-con.imp.fu-berlin.de"
     * <p>
     * "... joined the chat"</li>
     * <li>bob should receive the IM from alice.Only user B hears a beep when
     * receiving IM.</li>
     * </ol>
     * 
     * TODO: replace bob.sleep() by a better condition and do we need the
     * comperator.class for other tests?
     */

    @Test
    public void testChat() throws RemoteException {
        alice.chatV.sendChatMessage(message);
        // System.out.println(alice.bot
        // .getUserNameOnChatLinePartnerChangeSeparator());
        // System.out.println(alice.bot.getTextOfChatLine());
        // System.out.println(alice.bot.getTextOfChatLine(".*joined the chat.*"));
        alice.basic.sleep(2000);
        System.out.println(alice.chatV.getTextOfLastChatLine());

        bob.basic.sleep(2000);
        System.out.println(bob.chatV.getTextOfLastChatLine());
        System.out.println(bob.chatV
            .getUserNameOnChatLinePartnerChangeSeparator(alice.getBaseJid()));

        // bob.bot.waitUntilGetChatMessage(alice.getName(), message);
        // assertTrue(bob.bot.compareChatMessage(alice.getName(), message));

    }
}
