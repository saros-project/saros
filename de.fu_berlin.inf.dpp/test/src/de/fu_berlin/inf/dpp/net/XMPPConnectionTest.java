package de.fu_berlin.inf.dpp.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.UnknownHostException;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Constants;

/**
 * Test the Smack XMPP Connection class. We test this external class we
 * discovered timeout issues with the behavior of the DNS lookup library Smack
 * uses (com.sun.jndi.dns).
 * 
 * @author florianthiel
 */
public class XMPPConnectionTest {
    /*
     * This connection is instantiated in BeforeClass, so no DNS lookups have to
     * be done in the tests itself. Works around an issue on Mac OS where DNS
     * requests time out for the library used by Smack.
     */
    private static XMPPConnection connectionLookedUp;
    private static XMPPConnection connection;

    /**
     * WARNING: This might take a long time on Macs because DNS requests time
     * out (see class docs)
     */
    @BeforeClass
    public static void beforeClass() {
        connectionLookedUp = new XMPPConnection(Constants.INF_XMPP_SERVICE_NAME);
    }

    @AfterClass
    public static void afterClass() {
        connectionLookedUp = null;
    }

    @After
    public void tearDown() throws Exception {
        if (connectionLookedUp.isConnected())
            connectionLookedUp.disconnect();
        if (connection != null && connection.isConnected())
            connection.disconnect();
    }

    /**
     * Timeout test for DNS lookups in Mac OS (see class documentation) Need to
     * use a different service name from the one used in beforeClass because of
     * Smack caching lookups.
     */
    @Test(timeout = 3000)
    public void testXMPPConnectionString() {
        connection = new XMPPConnection(Constants.XMPP_OTHER_SERVICE_NAME);
    }

    /**
     * Non-existing DNS entry must trigger UnknownHostException and result in
     * unconnected state
     */
    @SuppressWarnings("null")
    @Test(timeout = 3000)
    public void testXMPPConnectionStringNonExistingServer() {
        XMPPConnection connection = null;
        try {
            connection = new XMPPConnection(Constants.XMPP_NON_EXISTING_SERVER);
            connection.connect();
        } catch (XMPPException e) {
            if (e.getWrappedThrowable() == null
                || !(e.getWrappedThrowable() instanceof UnknownHostException))
                fail();
        }
        assertFalse(connection.isConnected());
    }

    /**
     * DNS name that is different from the actual XMPP service name should fail
     * connections and connecting should trigger an UnknownHostException.
     */
    @Test(timeout = 3000)
    public void testXMPPConnectionStringServerName() {
        XMPPConnection connection = new XMPPConnection("broken");
        boolean isConnected = false;
        try {
            // At this point, connecting should not be possible due to unknown
            // host, and the connection state should be "not connected".
            connection.connect();
            isConnected = connection.isConnected();
        } catch (XMPPException e) {
            if (e.getWrappedThrowable() == null
                || !(e.getWrappedThrowable() instanceof UnknownHostException))
                fail();
        }
        assertFalse(isConnected);
    }

    @Test(timeout = 3000)
    public void testConnect() throws XMPPException {
        connectionLookedUp.connect();
        connectionLookedUp.login(Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD);
        assertTrue(connectionLookedUp.isConnected());
        assertTrue(connectionLookedUp.isAuthenticated());
    }

    @Test(timeout = 3000)
    public void testGetServiceName() {
        assertEquals(Constants.INF_XMPP_SERVICE_NAME,
            connectionLookedUp.getServiceName());
    }

    @Test(timeout = 3000)
    public void testDisconnect() throws XMPPException {
        connectionLookedUp.connect();
        connectionLookedUp.login(Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD);
        assertTrue(connectionLookedUp.isConnected());
        connectionLookedUp.disconnect();
        assertFalse(connectionLookedUp.isConnected());
    }
}