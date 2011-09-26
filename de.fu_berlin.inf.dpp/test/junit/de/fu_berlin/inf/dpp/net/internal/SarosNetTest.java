package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Constants;

public class SarosNetTest {

    SarosTestNet net;
    ConnectionConfiguration conConfig;

    @Before
    public void setUp() {
        net = new SarosTestNet(Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_SERVICE_NAME);

        try {
            conConfig = new ConnectionConfiguration(
                Constants.INF_XMPP_SERVICE_NAME);
            conConfig
                .setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
            conConfig.setReconnectionAllowed(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConnection() throws Exception {
        assertFalse(net.net.isConnected());

        net.net.initialize();
        assertFalse(net.net.isConnected());

        // Connect to Server
        net.net.connect(conConfig, Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, false);

        assertTrue(net.net.isConnected());
        assertNotNull(net.net.getConnection());

        // Already connected and connect again
        net.net.connect(conConfig, Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, false);

        assertTrue(net.net.isConnected());

        net.net.disconnect();
        assertFalse(net.net.isConnected());
        assertNull(net.net.getConnection());

        net.net.connect(conConfig, Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, false);
        assertTrue(net.net.isConnected());
        net.net.uninitialize();

        System.out
            .println("SarosNet.unInitialize should get a description, that it works async");
        Thread.sleep(1000);
        assertFalse(net.net.isConnected());

        net.net.uninitialize();
        Thread.sleep(1000);
        assertFalse(net.net.isConnected());

    }
}
