package de.fu_berlin.inf.dpp.stf.test.chatview;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.server.STFMessage.VIEW_SAROS;
import static org.junit.Assert.assertEquals;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class ChatViewFunctionsTest extends StfTestCase {

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
        initTesters(ALICE, BOB);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(ALICE, BOB);
    }

    /**
     * Steps:
     * <ol>
     * <li>ALICE open the chat view</li>
     * <li>BOB open the chat view</li>
     * <li>ALICE sends IM to BOB (both users should have beep button active by
     * default)</li>
     * </ol>
     * 
     * Result:
     * <ol>
     * <li>The chat should have automatically connected. ALICE should see e.g.
     * <p>
     * "You&nbsp;&nbsp;&nbsp;31/10/10 17:19"
     * <p>
     * "... joined the chat"
     * <p>
     * "BOB1_fu@saros-con.imp.fu-berlin.de"
     * <p>
     * "... joined the chat"</li>
     * <li>The chat should have automatically connected. BOB should see e.g.
     * <p>
     * "You&nbsp;&nbsp;&nbsp;31/10/10 17:19"
     * <p>
     * "... joined the chat"
     * <p>
     * "ALICE1_fu@saros-con.imp.fu-berlin.de"
     * <p>
     * "... joined the chat"</li>
     * <li>BOB should receive the IM from ALICE.Only user B hears a beep when
     * receiving IM.</li>
     * </ol>
     * 
     * TODO: replace BOB.sleep() by a better condition and do we need the
     * comperator.class for other tests?
     */

    @Test
    public void testChat() throws RemoteException {

        assertEquals("Chatroom 1", ALICE.remoteBot().view(VIEW_SAROS).bot()
            .cTabItem().getText());

        ALICE.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(message);
        System.out.println(ALICE.remoteBot().view(VIEW_SAROS).bot().label(1)
            .getText());
        ALICE.remoteBot().sleep(1000);
        System.out.println(ALICE.superBot().views().sarosView()
            .selectChatroom().getTextOfLastChatLine());
        System.out.println(BOB.remoteBot().view(VIEW_SAROS).bot().label(1)
            .getText());

    }
}
