package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertTrue;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Constants;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.SubscriptionManager;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events.IncomingSubscriptionEvent;
import de.fu_berlin.inf.dpp.net.internal.subscriptionManager.events.SubscriptionManagerListener;

public class SubscriptionManagerTest {

    SarosTestNet minSarosSender, minSarosReceiver;
    ConnectionConfiguration conConfig1, conConfig2;

    JID alice = new JID(Constants.INF_XMPP_TESTUSER_NAME + "@"
        + Constants.INF_XMPP_SERVICE_NAME);
    JID bob = new JID(Constants.INF_XMPP_TESTUSER2_NAME + "@"
        + Constants.INF_XMPP_SERVICE_NAME);

    // @Before
    // public void setUp() {
    //
    // minSarosSender = new SarosTestNet(Constants.INF_XMPP_TESTUSER_NAME,
    // Constants.INF_XMPP_SERVICE_NAME);
    // minSarosReceiver = new SarosTestNet(Constants.INF_XMPP_TESTUSER2_NAME,
    // Constants.INF_XMPP_SERVICE_NAME);
    //
    // conConfig1 = new ConnectionConfiguration(
    // Constants.INF_XMPP_SERVICE_NAME);
    // conConfig1
    // .setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
    // conConfig1.setReconnectionAllowed(false);
    //
    // conConfig2 = new ConnectionConfiguration(
    // Constants.INF_XMPP_SERVICE_NAME);
    // conConfig2
    // .setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
    // conConfig2.setReconnectionAllowed(false);
    //
    // }

    @Test
    @Ignore("this test is testing nothing at all")
    public void testConnection() throws Exception {

        SubscriptionManager s1 = new SubscriptionManager(minSarosSender.net);
        SubscriptionManager s2 = new SubscriptionManager(minSarosReceiver.net);

        SubscriptionManagerListener l = new SubscriptionManagerListener() {

            public void subscriptionReceived(IncomingSubscriptionEvent event) {
                System.out.println("Event: " + event);
            }
        };
        s1.addSubscriptionManagerListener(l);
        s2.addSubscriptionManagerListener(l);

        // Connect to the server
        minSarosSender.net.connect(conConfig1,
            Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, true);
        minSarosReceiver.net.connect(conConfig2,
            Constants.INF_XMPP_TESTUSER2_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, true);

        assertTrue(minSarosSender.net.isConnected());
        assertTrue(minSarosReceiver.net.isConnected());

        // if connected, this method is called by the listener
        // s1.prepareConnection(minSarosSender.net.getConnection());

        // on disconnect, this method is called by the listener
        // s1.disposeConnection();

        minSarosSender.net.disconnect();

        minSarosSender.net.connect(conConfig1,
            Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, true);

        // for Test Coverage: run each Presence Type

        Presence p2 = new Presence(Type.error, "Status", 1, Mode.available);
        p2.setFrom(alice.toString());
        p2.setError(new XMPPError(Condition.interna_server_error));
        s1.processPresence(p2);

        // subscribe alice
        Presence p = new Presence(Type.subscribe, "Status", 1, Mode.available);
        p.setFrom(alice.toString());
        s1.processPresence(p);

        Presence p3 = new Presence(Type.subscribed, "Status", 1, Mode.available);
        p3.setFrom(alice.toString());
        s1.processPresence(p3);

        Presence p4 = new Presence(Type.unavailable, "Status", 1,
            Mode.available);
        p4.setFrom(alice.toString());
        s1.processPresence(p4);

        Presence p1 = new Presence(Type.available, "Status", 1, Mode.available);
        p1.setFrom(alice.toString());
        s1.processPresence(p1);

        // unsubscribe alice
        Presence p5 = new Presence(Type.unsubscribe, "Status", 1,
            Mode.available);
        p5.setFrom(alice.toString());
        s1.processPresence(p5);

        Presence p6 = new Presence(Type.unsubscribed, "Status", 1,
            Mode.available);
        p6.setFrom(alice.toString());
        s1.processPresence(p6);

        s1.removeSubscriptionManagerListener(l);
        s1.removeSubscriptionManagerListener(l);

        minSarosReceiver.net.disconnect();
        minSarosSender.net.disconnect();
    }
}
