package de.fu_berlin.inf.dpp.communication.muc.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ChatState;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryMessageReceptionElement;
import de.fu_berlin.inf.dpp.net.JID;

public class MucSessionTest {

    private static Connection connectionAlice;
    private static Connection connectionBob;

    private static final JID ALICE_JID = new JID(
        "alice_stf@saros-con.imp.fu-berlin.de");
    private static final JID BOB_JID = new JID(
        "bob_stf@saros-con.imp.fu-berlin.de");

    @BeforeClass
    public static void connectToXmppServer() throws XMPPException {

        connectionAlice = new XMPPConnection(ALICE_JID.getDomain());
        connectionAlice.connect();
        connectionAlice.login(ALICE_JID.getName(), "dddfffggg");

        connectionBob = new XMPPConnection(BOB_JID.getDomain());
        connectionBob.connect();
        connectionBob.login(BOB_JID.getName(), "dddfffggg");

        SmackConfiguration.setPacketReplyTimeout(10000);
    }

    @AfterClass
    public static void disconnectFromXmppServer() {
        connectionAlice.disconnect();
        connectionBob.disconnect();
    }

    private MUCSessionPreferences createMUCPrefernce(String roomName) {
        return new MUCSessionPreferences("conference.jabber.se", roomName,
            "backebackekuchen");
    }

    @Test(timeout = 30000)
    public void testSwitchToMultipleRoomsAndSendMessages()
        throws XMPPException, InterruptedException {
        for (int i = 0; i < 5; i++) {
            MUCSessionPreferences preferences = createMUCPrefernce("saros_muc_test"
                + String.valueOf(System.currentTimeMillis()));
            MUCSession alice = new MUCSession(connectionAlice, preferences);
            MUCSession bob = new MUCSession(connectionBob, preferences);

            alice.connect();
            bob.connect();
            assertTrue(alice.isJoined());
            assertTrue(bob.isJoined());

            alice.setState(ChatState.active);
            bob.setState(ChatState.active);

            Set<String> messagesSendToAlice = new HashSet<String>();
            Set<String> messagesSendToBob = new HashSet<String>();

            for (int j = 0; j < 5; j++) {
                alice.sendMessage("Hello Bob" + j);
                bob.sendMessage("Hello Alice" + j);
                messagesSendToAlice.add("Hello Alice" + j);
                messagesSendToBob.add("Hello Bob" + j);
            }

            // Wait for update
            Thread.sleep(2000);

            assertTrue(alice.isJoined(BOB_JID));
            assertTrue(bob.isJoined(ALICE_JID));

            int aliceRevMessageCount = 0;
            int bobRevMessageCount = 0;

            MUCSessionHistoryElement[] aliceHistory = alice.getHistory();
            MUCSessionHistoryElement[] bobHistory = bob.getHistory();

            for (MUCSessionHistoryElement element : aliceHistory) {

                if (!element.getSender().equals(BOB_JID))
                    continue;

                if (element instanceof MUCSessionHistoryMessageReceptionElement) {
                    String message = ((MUCSessionHistoryMessageReceptionElement) element)
                        .getMessage();
                    assertTrue(messagesSendToAlice.contains(message));
                    aliceRevMessageCount++;
                }
            }

            for (MUCSessionHistoryElement element : bobHistory) {

                if (!element.getSender().equals(ALICE_JID))
                    continue;

                if (element instanceof MUCSessionHistoryMessageReceptionElement) {
                    String message = ((MUCSessionHistoryMessageReceptionElement) element)
                        .getMessage();
                    assertTrue(messagesSendToBob.contains(message));
                    bobRevMessageCount++;
                }
            }

            assertEquals("alice and or bob has not received all messages", 0,
                aliceRevMessageCount - bobRevMessageCount);

            alice.disconnect();
            Thread.sleep(1000);
            bob.disconnect();

        }
    }
}
