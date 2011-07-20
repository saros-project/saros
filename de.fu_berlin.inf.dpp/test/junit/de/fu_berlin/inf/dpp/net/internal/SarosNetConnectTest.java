package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Constants;

public class SarosNetConnectTest {

    SarosTestNet minSarosSender, minSarosReceiver;
    ConnectionConfiguration conConfig1, conConfig2;

    static Logger log = Logger.getLogger(SarosNetConnectTest.class);

    @Before
    public void setUp() {

        minSarosSender = new SarosTestNet(Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_SERVICE_NAME);
        minSarosReceiver = new SarosTestNet(Constants.INF_XMPP_TESTUSER2_NAME,
            Constants.INF_XMPP_SERVICE_NAME);

        try {
            conConfig1 = new ConnectionConfiguration(
                Constants.INF_XMPP_SERVICE_NAME);
            conConfig1
                .setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
            conConfig1.setReconnectionAllowed(false);

            conConfig2 = new ConnectionConfiguration(
                Constants.INF_XMPP_SERVICE_NAME);
            conConfig2
                .setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
            conConfig2.setReconnectionAllowed(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConnection() throws Exception {

        // Connect to the server
        minSarosSender.net.connect(conConfig1,
            Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, true);
        minSarosReceiver.net.connect(conConfig2,
            Constants.INF_XMPP_TESTUSER2_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, true);

        assertTrue(minSarosSender.net.isConnected());
        assertTrue(minSarosReceiver.net.isConnected());

        // Disconnect from the server
        minSarosSender.net.disconnect();
        assertFalse(minSarosSender.net.isConnected());

        // Disconnect from the server
        minSarosReceiver.net.disconnect();
        assertFalse(minSarosReceiver.net.isConnected());

        minSarosSender = null;
        minSarosReceiver = null;
    }
}
