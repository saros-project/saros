package de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.chatview;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.testProject.testsuits.STFTest;

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
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(TypeOfTester.ALICE, TypeOfTester.BOB);
        setUpWorkbenchs();
        setUpSaros();
        setUpSession(alice, bob);
    }

    @AfterClass
    public static void runAfterClass() throws RemoteException,
        InterruptedException {
        alice.leaveSessionHostFirstDone(bob);
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
        alice.workbench.sleep(2000);
        System.out.println(alice.chatV.getTextOfLastChatLine());

        bob.workbench.sleep(2000);
        System.out.println(bob.chatV.getTextOfLastChatLine());
        System.out.println(bob.chatV
            .getUserNameOnChatLinePartnerChangeSeparator(alice.getBaseJid()));

        // bob.bot.waitUntilGetChatMessage(alice.getName(), message);
        // assertTrue(bob.bot.compareChatMessage(alice.getName(), message));

    }
}
