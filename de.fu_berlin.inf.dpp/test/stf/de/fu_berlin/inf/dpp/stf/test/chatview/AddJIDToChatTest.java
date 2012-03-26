package de.fu_berlin.inf.dpp.stf.test.chatview;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class AddJIDToChatTest extends StfTestCase {

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    public void testChat() throws Exception {
        Util.setUpSessionWithJavaProjectAndClass("foo", "bar", "test", ALICE,
            BOB);

        BOB.superBot().views().packageExplorerView()
            .waitUntilResourceIsShared("foo/src/bar/test.java");

        ALICE.superBot().views().sarosView().selectBuddy(BOB.getJID())
            .addJIDToChat();

        ALICE.superBot().views().sarosView().selectChatroom().sendChatMessage();
        ALICE.remoteBot().sleep(5000);

        assertTrue("JID was not insert in the chat or was not send", BOB
            .superBot().views().sarosView().selectChatroom()
            .getTextOfLastChatLine().contains(BOB.getJID().getBase()));
    }
}
