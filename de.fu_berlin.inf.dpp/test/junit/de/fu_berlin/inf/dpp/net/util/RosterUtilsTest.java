/**
 * 
 */
package de.fu_berlin.inf.dpp.net.util;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.easymock.EasyMock;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Constants;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.internal.SarosTestNet;
import de.fu_berlin.inf.dpp.net.util.RosterUtils.DialogContent;

/**
 * All Methods are tested by using the SarosTestNet.
 * 
 * By using Mock Objects it would be possible to increase the code coverage
 * 
 */
public class RosterUtilsTest {

    DialogContent dialog1 = new DialogContent("Buddy Unknown",
        "The buddy is unknown to the XMPP/Jabber server.\n\n"
            + "Do you want to add the buddy anyway?",
        "Buddy unknown to XMPP/Jabber server.");

    DialogContent dialog2 = new DialogContent("Server Not Found",
        "The responsible XMPP/Jabber server could not be found.\n\n"
            + "Do you want to add the buddy anyway?",
        "Unable to find the responsible XMPP/Jabber server.");

    DialogContent dialog3 = new DialogContent("Unsupported Buddy Status Check",
        "The responsible XMPP/Jabber server does not support status requests.\n\n"
            + "If the buddy exists you can still successfully add him.\n\n"
            + "Do you want to try to add the buddy?",
        "Buddy status check unsupported by XMPP/Jabber server.");

    DialogContent dialog4 = new DialogContent(
        "Unknown Buddy Status",
        "For privacy reasons the XMPP/Jabber server does not reply to status requests.\n\n"
            + "If the buddy exists you can still successfully add him.\n\n"
            + "Do you want to try to add the buddy?",
        "Unable to check the buddy status.");

    DialogContent dialog5 = new DialogContent("Server Not Responding",
        "The responsible XMPP/Jabber server is not connectable.\n"
            + "The server is either inexistent or offline right now.\n\n"
            + "Do you want to add the buddy anyway?",
        "The XMPP/Jabber server did not respond.");

    SarosTestNet testNet;
    ConnectionConfiguration conConfig;
    JID jid;
    JID jid2 = new JID("christian_test@saros-con.imp.fu-berlin.de");

    @Before
    public void setUp() throws Exception {

        testNet = new SarosTestNet(Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_SERVICE_NAME);

        conConfig = new ConnectionConfiguration(Constants.INF_XMPP_SERVICE_NAME);
        conConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        conConfig.setReconnectionAllowed(false);

        // Connect to Server
        testNet.net.connect(conConfig, Constants.INF_XMPP_TESTUSER_NAME,
            Constants.INF_XMPP_TESTUSER_PASSWORD, false);

        jid = testNet.net.getMyJID();

    }

    @After
    public void tearDown() {
        testNet.net.disconnect();
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#getDialogContent(org.jivesoftware.smack.XMPPException)}
     * .
     */
    @Test
    @Ignore("senseless test")
    public void testGetDialogContent() {
        XMPPException e1 = new XMPPException("item-not-found");
        assertTrue("item-not-found is wrong implemented",
            RosterUtils.getDialogContent(e1).dialogTitle
                .equals(dialog1.dialogTitle));

        XMPPException e2 = new XMPPException("remote-server-not-found");
        assertTrue("remote-server-not-found is wrong implemented",
            RosterUtils.getDialogContent(e2).dialogTitle
                .equals(dialog2.dialogTitle));

        XMPPException e3 = new XMPPException("501");
        assertTrue("501 is wrong implemented",
            RosterUtils.getDialogContent(e3).dialogTitle
                .equals(dialog3.dialogTitle));

        XMPPException e4 = new XMPPException("503");
        assertTrue("503 is wrong implemented",
            RosterUtils.getDialogContent(e4).dialogTitle
                .equals(dialog4.dialogTitle));

        XMPPException e5 = new XMPPException("No response from the server");
        assertTrue("No response from the server is wrong implemented",
            RosterUtils.getDialogContent(e5).dialogTitle
                .equals(dialog5.dialogTitle));

        XMPPException e6 = new XMPPException("something else");
        assertTrue("Unknown Error is wrong implemented",
            RosterUtils.getDialogContent(e6).dialogTitle
                .equals("Unknown Error"));
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#getNickname(de.fu_berlin.inf.dpp.net.SarosNet, de.fu_berlin.inf.dpp.net.JID)}
     * .
     * 
     * Not completely finished
     */
    @Test
    @Ignore("not finished yet")
    public void testGetNickname() {
        assertTrue("getNickname with null SarosNet should return null ",
            RosterUtils.getNickname(null, null) == null);

        assertTrue("getNickname with null SarosNet should return null ",
            RosterUtils.getNickname(testNet.net, jid) == null);

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#getDisplayableName(org.jivesoftware.smack.RosterEntry)}
     * .
     */
    @Test
    public void testGetDisplayableName() {
        RosterEntry entry = createMock(RosterEntry.class);
        EasyMock.expect(entry.getName()).andReturn("Alice").times(1);
        EasyMock.replay(entry);

        assertTrue("The returned nickname should be Alice", RosterUtils
            .getDisplayableName(entry).equals("Alice"));

        EasyMock.verify(entry);

        RosterEntry entry2 = createMock(RosterEntry.class);
        EasyMock.expect(entry2.getName()).andReturn("").times(1);
        EasyMock.expect(entry2.getUser()).andReturn("Alice_User").times(1);
        EasyMock.replay(entry2);

        assertTrue("The returned nickname should be Alice", RosterUtils
            .getDisplayableName(entry2).equals("Alice_User"));

        EasyMock.verify(entry2);

        RosterEntry entry3 = createMock(RosterEntry.class);
        EasyMock.expect(entry3.getName()).andReturn(null).times(1);
        EasyMock.expect(entry3.getUser()).andReturn("Alice_User").times(1);
        EasyMock.replay(entry3);

        assertTrue("The returned nickname should be Alice", RosterUtils
            .getDisplayableName(entry3).equals("Alice_User"));

        EasyMock.verify(entry3);

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#isAccountCreationPossible(org.jivesoftware.smack.Connection, java.lang.String)}
     * .
     */
    @Test
    @Ignore("will not work if the order of the test cases is permutated on every run")
    public void testIsAccountCreationPossible() {
        Connection con = testNet.net.getConnection();
        assertTrue("Account is unused, so it should be possible to create",
            RosterUtils.isAccountCreationPossible(con, "Christian_Z") == null);

        assertTrue("Account is used, so it should return an error message",
            RosterUtils.isAccountCreationPossible(con, "christian_test")
                .contains("Account"));

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#addToRoster(org.jivesoftware.smack.Connection, de.fu_berlin.inf.dpp.net.JID, java.lang.String, org.eclipse.core.runtime.SubMonitor)}
     * .
     * 
     * @throws InvocationTargetException
     */
    @Test
    @Ignore("will not work if the order of the test cases is permutated on every run")
    public void testAddToRoster() throws InvocationTargetException {
        Connection con = testNet.net.getConnection();
        RosterUtils.addToRoster(con, jid2, "Chris", null);
        assertTrue("The new buddy should be added.",
            con.getRoster().contains(jid2.toString()));

        RosterUtils.addToRoster(con, jid, "tester", null);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#addToRoster(org.jivesoftware.smack.Connection, de.fu_berlin.inf.dpp.net.JID, java.lang.String, org.eclipse.core.runtime.SubMonitor)}
     * .
     * 
     * @throws InvocationTargetException
     */
    @Test(expected = InvocationTargetException.class)
    @Ignore("will not work if the order of the test cases is permutated on every run")
    public void testAddToRoster2() throws InvocationTargetException {

        RosterUtils.addToRoster(null, jid, "Alice", null);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#addToRoster(org.jivesoftware.smack.Connection, de.fu_berlin.inf.dpp.net.JID, java.lang.String, org.eclipse.core.runtime.SubMonitor)}
     * .
     * 
     * @throws InvocationTargetException
     * 
     *             To get to the catch clause of XMPPException
     */
    @Test
    @Ignore("will not work if the order of the test cases is permutated on every run")
    public void testAddToRoster3() throws InvocationTargetException {
        Connection con = testNet.net.getConnection();
        con.disconnect();
        RosterUtils.addToRoster(con, jid, "Alice", null);
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#getNickname(de.fu_berlin.inf.dpp.net.SarosNet, de.fu_berlin.inf.dpp.net.JID)}
     * .
     * 
     * 
     */
    @Test
    @Ignore("will not work if the order of the test cases is permutated on every run")
    public void testGetNickname2() {
        SarosNet sarosNet = testNet.net;
        assertTrue("getNickname should now return the nickname: Chris ",
            RosterUtils.getNickname(sarosNet, jid2).equals("Chris"));

    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#removeFromRoster(org.jivesoftware.smack.Connection, org.jivesoftware.smack.RosterEntry)}
     * .
     * 
     * @throws XMPPException
     * @throws InterruptedException
     */
    @Test
    @Ignore("will not work if the order of the test cases is permutated on every run")
    public void testRemoveFromRoster() throws XMPPException,
        InterruptedException {
        Connection con = testNet.net.getConnection();

        System.out.println(con.getRoster().getEntries());
        RosterUtils.removeFromRoster(con,
            con.getRoster().getEntry(jid2.toString()));
        System.out.println(con.getRoster().getEntries());
        assertFalse("The buddy should be removed",
            con.getRoster().contains(jid2.toString()));
    }

    /**
     * Test method for
     * {@link de.fu_berlin.inf.dpp.net.util.RosterUtils#removeFromRoster(org.jivesoftware.smack.Connection, org.jivesoftware.smack.RosterEntry)}
     * .
     * 
     * @throws XMPPException
     */
    @Test(expected = XMPPException.class)
    @Ignore("will not work if the order of the test cases is permutated on every run")
    public void testRemoveFromRoster2() throws XMPPException {
        Connection con = testNet.net.getConnection();
        con.disconnect();
        RosterUtils.removeFromRoster(con,
            con.getRoster().getEntry(jid.toString()));
    }

}
