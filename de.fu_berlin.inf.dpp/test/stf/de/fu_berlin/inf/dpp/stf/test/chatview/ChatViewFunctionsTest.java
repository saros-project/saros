package de.fu_berlin.inf.dpp.stf.test.chatview;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants;

public class ChatViewFunctionsTest extends StfTestCase {

    String messageBob = "Hello Bob";
    String messageAlice = "Hello Alice";

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Before
    public void setUp() throws Exception {
        closeAllShells();
        closeAllEditors();
        clearWorkspaces();

        Util.setUpSessionWithJavaProjectAndClass("foo", "bar", "test", ALICE,
            BOB);

    }

    @After
    public void tearDown() throws Exception {
        leaveSessionPeersFirst(ALICE);
    }

    @Test
    public void testChat() throws Exception {

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/src/bar/test.java");

        ALICE.superBot().views().sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .sendChatMessage(messageBob);

        /* Wait so that the message is received on the other side */
        Thread.sleep(5000);

        assertEquals(messageBob, BOB.superBot().views().sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .getTextOfLastChatLine());

        BOB.superBot().views().sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .sendChatMessage(messageAlice);

        /* Wait so that the message is received on the other side */
        Thread.sleep(5000);

        assertEquals(messageAlice, ALICE.superBot().views().sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .getTextOfLastChatLine());
    }

    /*
     * Test for misbehavior on file opening in follow mode as described by bug
     * #3455372.
     */
    @Test
    public void testUnintentionalCursorMove() throws Exception {

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/src/bar/test.java");

        ALICE.superBot().internal().createFile("foo", "src/bar/test2.java", "");

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/src/bar/test2.java");

        BOB.superBot().views().sarosView().selectParticipant(ALICE.getJID())
            .followParticipant();

        BOB.superBot().views().sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .enterChatMessage("Hello how");

        ALICE.superBot().views().packageExplorerView()
            .selectClass("foo", "bar", "test2").open();

        /* Wait a short time so the editor change is executed on Bobs side */
        Thread.sleep(5000);

        BOB.superBot().views().sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .enterChatMessage(" are you?");

        assertEquals(
            "cursor position changed during focus lost and focus regain",
            "Hello how are you?", BOB.superBot().views().sarosView()
                .selectChatroom(Constants.CHATROOM_TAB_LABEL).getChatMessage());
    }
}
