package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.chatview;

import static org.junit.Assert.assertEquals;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

public class TestChatViewFunctions extends STFTest {

    String message = "Hello Bob";

    /**
     * Preconditions:
     * <ol>
     * <li>Alice (Host, Write Access)</li>
     * <li>Bob (Read-Only Access)</li>
     * </ol>
     * 
     * @throws AccessException
     * @throws RemoteException
     * @throws InterruptedException
     */

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbench();
        setUpSaros();
        setUpSessionWithAJavaProjectAndAClass(alice, bob);
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

        assertEquals("Chatroom 1", alice.remoteBot().view(VIEW_SAROS).bot()
            .cTabItem().getText());

        alice.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(message);
        System.out.println(alice.remoteBot().view(VIEW_SAROS).bot().label(1)
            .getText());
        alice.remoteBot().sleep(1000);
        System.out.println(alice.superBot().views().sarosView()
            .selectChatroom().getTextOfLastChatLine());
        System.out.println(bob.remoteBot().view(VIEW_SAROS).bot().label(1).getText());

        //
        // // System.out.println(alice.bot.getTextOfChatLine());
        // //
        // // System.out.println(alice.superBot().views().sarosView()
        // // .getTextOfChatLine(".*joined the chat.*"));
        // alice.bot().sleep(1000);
        // String messageByAlice = alice.superBot().views().sarosView()
        // .selectChatroom().getTextOfLastChatLine();
        // System.out.println(messageByAlice);
        // // bob.bot().sleep(1000);
        // String messageByBob = bob.superBot().views().sarosView()
        // .selectChatroom().getUserNameOnChatLinePartnerChangeSeparator();
        //
        // System.out.println("bob: " + messageByBob);
        // assertEquals(messageByAlice, messageByBob);
        // System.out.println(bob.superBot().views().sarosView().selectChatroom()
        // .getUserNameOnChatLinePartnerChangeSeparator(alice.getBaseJid()));
        //
        // // bob.bot.waitUntilGetChatMessage(alice.getName(), message);
        // // assertTrue(bob.bot.compareChatMessage(alice.getName(),
        // // message));

    }
}
