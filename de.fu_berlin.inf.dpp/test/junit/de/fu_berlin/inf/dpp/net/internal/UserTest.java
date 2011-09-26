package de.fu_berlin.inf.dpp.net.internal;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.Constants;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo.JoinExtensionProvider;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;

public class UserTest {

    User u1, u2;
    SessionIDObservable sid;

    JID alice = new JID("alice@saros-con.imp.fu-berlin.de/Saros");
    JID bob = new JID("bob@saros-con.imp.fu-berlin.de/Saros");

    SarosSession session;

    SarosTestNet net;
    ConnectionConfiguration conConfig;
    JID me;

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

        sid = new SessionIDObservable();

        session = createMock(SarosSession.class);

        // JoinExtensionProvider j = new JoinExtensionProvider();
        // UserListHandler h = new UserListHandler(net.xmppReceiver, sid, j);
        // net.xmppReceiver.addPacketListener(new PacketListener() {
        //
        // public void processPacket(Packet packet) {
        // System.out.println(packet.toString());
        //
        // }
        // }, new PacketFilter() {
        //
        // public boolean accept(Packet packet) {
        // return true;
        // }
        // });

        // Message m = new Message();
        // m.setBody("Hallo");
        // net.xmppTransmitter.sendMessageToUser(me, m, false);
        //
        // Thread.sleep(1000);
    }

    @Test
    public void testUser() throws InterruptedException {

        u1 = new User(session, alice, 1);
        u2 = new User(session, bob, 2);

        assertFalse(u1.equals(null));
        assertFalse(u1.equals(alice)); // A User is not just the JID
        assertFalse(u1.equals(u2));

        EasyMock.expect(session.getUser(alice)).andReturn(u1); // User is in
        EasyMock.expect(session.getLocalUser()).andReturn(u1).times(1, 5); // User
                                                                           // is
                                                                           // in
        EasyMock.expect(session.getHost()).andReturn(u1).times(1, 5); // User is
                                                                      // in
        EasyMock.replay(session);

        assertTrue(u1.getJID().equals(alice));

        // User should have Write access after creation
        assertTrue(u1.getPermission().equals(Permission.WRITE_ACCESS));
        assertFalse(u1.hasReadOnlyAccess());
        assertTrue(u1.hasWriteAccess());

        Permission p = Permission.READONLY_ACCESS;
        u1.setPermission(p);
        assertEquals(p, u1.getPermission());
        assertFalse(u1.hasWriteAccess());
        assertTrue(u1.hasReadOnlyAccess());

        assertTrue(u1.isInSarosSession());
        assertEquals(alice.getName(), u1.toString());
        assertEquals(1, u1.getColorID());

        assertFalse(u1.isInvitationComplete());

        u1.invitationCompleted();
        assertTrue(u1.isInvitationComplete());
        try {
            u1.invitationCompleted();
            fail();
        } catch (IllegalStateException e) {
            // expected
        }

        u1.setAway(true);
        assertTrue(u1.isAway());
        u1.setAway(false);
        assertFalse(u1.isAway());

        assertEquals(UserConnectionState.UNKNOWN, u1.getConnectionState());
        u1.setConnectionState(UserConnectionState.ONLINE);
        assertEquals(UserConnectionState.ONLINE, u1.getConnectionState());
        assertTrue(u1.getOfflineSeconds() == 0);
        u1.setConnectionState(UserConnectionState.OFFLINE);
        assertEquals(UserConnectionState.OFFLINE, u1.getConnectionState());
        Thread.sleep(2000);
        assertTrue(u1.getOfflineSeconds() > 1);

        assertTrue(u1.isLocal());
        assertFalse(u1.isRemote());

        // Human readable name should be "You" because u1 is local User
        assertEquals("You", u1.getHumanReadableName());

        assertTrue(u1.isHost());
        assertFalse(u1.isClient());
    }

    @Test
    public void testUserListInfo() {
        // Create Collection of Users
        User u = new User(session, alice, 1);

        ArrayList<User> userlist = new ArrayList<User>();
        userlist.add(u);
        assertNotNull(new UserListInfo(sid, "123", userlist));

        assertNotNull(new JoinExtensionProvider());

    }
}
