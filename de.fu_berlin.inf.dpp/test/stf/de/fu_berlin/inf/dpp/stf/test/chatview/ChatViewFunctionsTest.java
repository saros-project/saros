package de.fu_berlin.inf.dpp.stf.test.chatview;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.shared.Constants.VIEW_SAROS;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Util;

public class ChatViewFunctionsTest extends StfTestCase {

    String message = "Hello Bob";

    @BeforeClass
    public static void selectTesters() throws Exception {
        select(ALICE, BOB);
    }

    @Test
    public void testChat() throws RemoteException {
        Util.setUpSessionWithJavaProjectAndClass("foo", "bar", "test", ALICE,
            BOB);

        assertEquals("Roundtable", ALICE.remoteBot().view(VIEW_SAROS).bot()
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
