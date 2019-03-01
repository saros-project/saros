package saros.stf.test.stf.chatview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static saros.stf.client.tester.SarosTester.ALICE;
import static saros.stf.client.tester.SarosTester.BOB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import saros.stf.client.StfTestCase;
import saros.stf.client.util.Util;
import saros.stf.shared.Constants;

public class ChatViewTest extends StfTestCase {
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

    Util.setUpSessionWithJavaProjectAndClass("foo", "bar", "test", ALICE, BOB);

    BOB.superBot().views().packageExplorerView().waitUntilResourceIsShared("foo/src/bar/test.java");
  }

  @After
  public void tearDown() throws Exception {
    leaveSessionPeersFirst(ALICE);
  }

  @Test
  public void testChatSend() throws Exception {

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage(messageBob);

    /* Wait so that the message is received on the other side */
    Thread.sleep(5000);

    assertEquals(
        messageBob,
        BOB.superBot()
            .views()
            .sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .getTextOfLastChatLine());

    BOB.superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage(messageAlice);

    /* Wait so that the message is received on the other side */
    Thread.sleep(5000);

    assertEquals(
        messageAlice,
        ALICE
            .superBot()
            .views()
            .sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .getTextOfLastChatLine());
  }

  @Test
  public void testChatRegex() throws Exception {

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage(messageBob);

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage(messageBob);

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage("ababababab");

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage(messageBob);

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage(messageBob);

    /* Wait so that the message is received on the other side */
    Thread.sleep(5000);

    assertEquals(
        "ababababab",
        ALICE
            .superBot()
            .views()
            .sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .getTextOfChatLine("(ab)++"));

    assertEquals(
        "ababababab",
        BOB.superBot()
            .views()
            .sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .getTextOfChatLine("(ab)++"));
  }

  @Test
  public void testMultipleOpenedChatRooms() throws Exception {
    ALICE.superBot().views().sarosView().selectUser(BOB.getJID()).openChat();

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroom(Constants.CHATROOM_TAB_LABEL)
        .sendChatMessage("Session chat");

    ALICE
        .superBot()
        .views()
        .sarosView()
        .selectChatroomWithRegex(BOB.getBaseJid() + ".*")
        .sendChatMessage("1 to 1 chat");

    /* Wait so that the message is received on the other side */
    Thread.sleep(5000);

    String lastMUCLine =
        BOB.superBot()
            .views()
            .sarosView()
            .selectChatroom(Constants.CHATROOM_TAB_LABEL)
            .getTextOfLastChatLine();

    String lastSUCLine =
        BOB.superBot()
            .views()
            .sarosView()
            .selectChatroomWithRegex(ALICE.getBaseJid() + ".*")
            .getTextOfLastChatLine();

    assertTrue("chat message was not send or timeout is exeeded", lastMUCLine.length() > 0);

    assertTrue("chat message was not send or timeout is exeeded", lastSUCLine.length() > 0);

    assertEquals("chat message was send in the wrong chat", "Session chat", lastMUCLine);

    assertEquals("chat message was send in the wrong chat", "1 to 1 chat", lastSUCLine);
  }
}
