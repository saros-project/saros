package de.fu_berlin.inf.dpp.net;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.roster.RosterTracker;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.junit.Before;
import org.junit.Test;

public class RosterTrackerTest {

  private static Presence createPresence(String user, Presence.Type type) {
    Presence p = new Presence(type);
    p.setFrom(user);

    return p;
  }

  private static List<Presence> roster0 =
      Arrays.asList(
          createPresence("alice", Presence.Type.available),
          createPresence("bob", Presence.Type.available),
          createPresence("carl", Presence.Type.available),
          createPresence("dave", Presence.Type.available),
          createPresence("edna", Presence.Type.unavailable));

  private static List<Presence> roster1 =
      Arrays.asList(
          createPresence("alice", Presence.Type.available),
          createPresence("bob", Presence.Type.available),
          createPresence("carl", Presence.Type.unavailable),
          createPresence("dave", Presence.Type.available),
          createPresence(null, Presence.Type.available));

  private XMPPConnectionService connectionServiceMock;

  private Capture<IConnectionListener> connectionListener = new Capture<IConnectionListener>();

  private XMPPConnectionService createXMPPConnectionServiceMock(
      Capture<IConnectionListener> connectionListener) {
    XMPPConnectionService net = EasyMock.createMock(XMPPConnectionService.class);
    net.addListener(
        EasyMock.and(
            EasyMock.isA(IConnectionListener.class), EasyMock.capture(connectionListener)));
    EasyMock.expectLastCall().once();
    EasyMock.replay(net);
    return net;
  }

  @Before
  public void setUp() {
    connectionServiceMock = createXMPPConnectionServiceMock(connectionListener);
  }

  @Test
  public void testEmptyRosterGetPresence() {
    RosterTracker tracker = new RosterTracker(connectionServiceMock);

    assertFalse(
        "this iterator must not have any elements",
        tracker.getPresences(new JID("bla")).iterator().hasNext());
  }

  @Test
  public void testEmptyRosterGetAvailablePresences() {
    RosterTracker tracker = new RosterTracker(connectionServiceMock);

    assertFalse(
        "this iterator must not have any elements",
        tracker.getAvailablePresences(new JID("bla")).iterator().hasNext());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAvailablePresencesWithNullJid() {
    RosterTracker tracker = new RosterTracker(connectionServiceMock);
    tracker.getAvailablePresences(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetPresencesWithNullJid() {
    RosterTracker tracker = new RosterTracker(connectionServiceMock);
    tracker.getPresences(null);
  }

  @Test
  public void testGetPresences() {
    XMPPConnection mockConnection = createMock(XMPPConnection.class);
    Roster mockRoster = createMock(Roster.class);
    expect(mockConnection.getRoster()).andReturn(mockRoster);
    expect(mockRoster.getPresences("devil")).andReturn(roster0.iterator());
    mockRoster.addRosterListener(isA(RosterListener.class));
    expectLastCall();
    replay(mockConnection);
    replay(mockRoster);

    RosterTracker tracker = new RosterTracker(connectionServiceMock);

    connectionListener
        .getValue()
        .connectionStateChanged(mockConnection, ConnectionState.CONNECTING);

    connectionListener.getValue().connectionStateChanged(mockConnection, ConnectionState.CONNECTED);

    tracker.getPresences(new JID("devil"));

    verify(mockConnection);
    verify(mockRoster);
  }

  @Test
  public void testGetAvailablePresences() {
    XMPPConnection mockConnection = createMock(XMPPConnection.class);
    Roster mockRoster = createMock(Roster.class);
    expect(mockConnection.getRoster()).andReturn(mockRoster);
    expect(mockRoster.getPresences("devil")).andReturn(roster1.iterator());
    mockRoster.addRosterListener(isA(RosterListener.class));
    expectLastCall();
    replay(mockConnection);
    replay(mockRoster);

    RosterTracker tracker = new RosterTracker(connectionServiceMock);

    connectionListener
        .getValue()
        .connectionStateChanged(mockConnection, ConnectionState.CONNECTING);

    connectionListener.getValue().connectionStateChanged(mockConnection, ConnectionState.CONNECTED);

    Iterator<JID> p = tracker.getAvailablePresences(new JID("devil")).iterator();

    int c = 0;
    while (p.hasNext()) {
      c++;
      p.next();
    }

    assertEquals(3, c);

    verify(mockConnection);
    verify(mockRoster);
  }

  @Test
  public void testRosterAvailability() {

    XMPPConnection mockConnection = createMock(XMPPConnection.class);
    Roster mockRoster = createMock(Roster.class);
    expect(mockConnection.getRoster()).andReturn(mockRoster);
    mockRoster.addRosterListener(isA(RosterListener.class));
    expectLastCall();
    mockRoster.removeRosterListener(isA(RosterListener.class));
    expectLastCall();
    replay(mockConnection);
    replay(mockRoster);

    RosterTracker tracker = new RosterTracker(connectionServiceMock);

    connectionListener
        .getValue()
        .connectionStateChanged(mockConnection, ConnectionState.CONNECTING);

    assertTrue(
        "roster must be available on connection state 'CONNECTING'", tracker.getRoster() != null);

    connectionListener.getValue().connectionStateChanged(mockConnection, ConnectionState.CONNECTED);

    assertTrue(
        "roster must be available on connection state 'CONNECTED'", tracker.getRoster() != null);

    connectionListener
        .getValue()
        .connectionStateChanged(mockConnection, ConnectionState.DISCONNECTING);

    assertFalse(
        "roster must not be available on connection state other than 'CONNECTING' and 'CONNECTED'",
        tracker.getRoster() != null);

    verify(mockConnection);
    verify(mockRoster);
  }
}
