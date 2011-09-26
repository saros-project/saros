package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Constants;
import de.fu_berlin.inf.dpp.net.JID;

public class JIDTest {

    SarosTestNet net;
    ConnectionConfiguration conConfig;
    JID me;

    private final String ALICE = "alice@saros-con.imp.fu-berlin.de";
    private final String TESTUSER = Constants.INF_XMPP_TESTUSER_NAME + "@"
        + Constants.INF_XMPP_SERVICE_NAME;
    private final String TESTUSER_RESOURCE = "Saros";

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

            // Connect to Server
            net.net.connect(conConfig, Constants.INF_XMPP_TESTUSER_NAME,
                Constants.INF_XMPP_TESTUSER_PASSWORD, false);

            me = net.net.getMyJID();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testJID() throws Exception {
        // Teste ob auch die richtige JID von SarosNet erhalten wurde
        JID exp = new JID(TESTUSER + "/" + TESTUSER_RESOURCE);
        assertTrue(me.equals(exp));

        // Darf nicht mit null erzeugen
        try {
            String s = null;
            new JID(s);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test Name
        assertTrue(me.getName().equals(Constants.INF_XMPP_TESTUSER_NAME));

        // Testuser has "Saros" as Resource
        assertFalse(me.isBareJID());
        assertTrue(me.isResourceQualifiedJID());
        assertTrue(me.getResource().equals(TESTUSER_RESOURCE));
        assertTrue(me.getBareJID().equals(new JID(TESTUSER)));
        assertTrue(me.getRAW().equals(TESTUSER + "/" + TESTUSER_RESOURCE));
        assertTrue(me.getDomain().equals(Constants.INF_XMPP_SERVICE_NAME));

        // Alice does not have a Resource Part
        JID a = new JID(ALICE);
        assertTrue(a.isBareJID());
        assertFalse(a.isResourceQualifiedJID());
        assertTrue(a.getResource().equals(""));
        assertTrue(a.getBareJID().equals(new JID(a.getBase())));
        assertTrue(a.getRAW().equals(ALICE));

    }

    @Test
    public void testEquals() {
        assertTrue(me.strictlyEquals(me));
        assertFalse(me.strictlyEquals(new JID(me.getBase())));

        assertFalse(me.equals(null));
        assertFalse(me.equals(me.getBase())); // JID Does not equal only the
                                              // String
        assertTrue(me.equals(me));
        assertTrue(me.equals(new JID(me.getBase())));
    }

    @Test
    public void testIsValid() {
        // Test static Method
        // Should be connected with a valid JID
        assertTrue(JID.isValid(me));
        // What is a valid JID?
        assertFalse(JID.isValid(new JID("hallo")));

        // Test dynamic Method
        // Should be valid
        assertTrue(me.isValid());
        assertFalse(new JID("Hallo").isValid());
    }

    @Test
    public void testCreateWithRoster() throws XMPPException,
        InterruptedException {

        // Create Elemente in Roster
        Roster r = net.net.getConnection().getRoster();
        r.createEntry(ALICE, "Alice", null);
        Thread.sleep(1000); // wait a bit until Entry exists
        RosterEntry re = r.getEntry(ALICE);

        JID a = new JID(re);
        assertTrue(a.isValid());
        assertTrue(a.getBase().equals(ALICE));

        try {
            RosterEntry re2 = null;
            new JID(re2);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
