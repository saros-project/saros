package de.fu_berlin.inf.dpp.stf.test.stf.chatview;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class ChatViewTest extends StfTestCase {
    String messageBob = "Hello Bob";
    String messageAlice = "Hello Alice";

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    public void testChatSend() throws Exception {
        Util.setUpSessionWithJavaProjectAndClass("foo", "bar", "test", ALICE,
            BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/src/bar/test.java");

        assertEquals("Roundtable", ALICE.remoteBot().view(VIEW_SAROS).bot()
            .cTabItem().getText());

        ALICE.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(messageBob);
        ALICE.remoteBot().sleep(5000);
        assertEquals(messageBob, BOB.superBot().views().sarosView()
            .selectChatroom().getTextOfLastChatLine());
        BOB.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(messageAlice);
        ALICE.remoteBot().sleep(5000);
        assertEquals(messageAlice, ALICE.superBot().views().sarosView()
            .selectChatroom().getTextOfLastChatLine());
    }

    @Test
    public void testChatRegex() throws RemoteException {

        ALICE.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(messageBob);
        ALICE.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(messageBob);
        ALICE.superBot().views().sarosView().selectChatroom()
            .sendChatMessage("ababababab");
        ALICE.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(messageBob);
        ALICE.superBot().views().sarosView().selectChatroom()
            .sendChatMessage(messageBob);

        ALICE.remoteBot().sleep(5000);

        assertEquals("ababababab", ALICE.superBot().views().sarosView()
            .selectChatroom().getTextOfChatLine("(ab)++"));

        assertEquals("ababababab", BOB.superBot().views().sarosView()
            .selectChatroom().getTextOfChatLine("(ab)++"));
    }
}
