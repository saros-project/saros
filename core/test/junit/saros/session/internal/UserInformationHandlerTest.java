package saros.session.internal;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import org.easymock.EasyMock;
import org.jivesoftware.smack.filter.PacketFilter;
import org.junit.Before;
import org.junit.Test;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.PacketCollector;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.session.User;

public class UserInformationHandlerTest {

  private final List<User> emptyUserList = Collections.emptyList();

  private ITransmitter transmitter;
  private IReceiver receiver;
  private ISarosSession session;
  private PacketCollector dummyCollector;

  @Before
  public void setUp() {
    transmitter = EasyMock.createNiceMock(ITransmitter.class);
    receiver = EasyMock.createNiceMock(IReceiver.class);
    dummyCollector = EasyMock.createNiceMock(PacketCollector.class);

    EasyMock.expect(receiver.createCollector(EasyMock.isA(PacketFilter.class)))
        .andStubReturn(dummyCollector);
    EasyMock.replay(transmitter, receiver, dummyCollector);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSynchronizeWithEmptyRemoteUsersCollection() {
    session = EasyMock.createNiceMock(ISarosSession.class);
    EasyMock.expect(session.isHost()).andStubReturn(true);
    EasyMock.replay(session);

    UserInformationHandler handler = new UserInformationHandler(session, transmitter, receiver);

    User alice = new User(new JID("alice@test/Saros"), false, false, 0, 0);

    handler.start();

    handler.synchronizeUserList(Collections.singletonList(alice), null, emptyUserList);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSynchronizeWithEmptyUserData() {
    session = EasyMock.createNiceMock(ISarosSession.class);
    EasyMock.expect(session.isHost()).andStubReturn(true);
    EasyMock.replay(session);

    UserInformationHandler handler = new UserInformationHandler(session, transmitter, receiver);

    User alice = new User(new JID("alice@test/Saros"), false, false, 0, 0);

    handler.start();

    handler.synchronizeUserList(emptyUserList, emptyUserList, Collections.singletonList(alice));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSynchronizeWithNullUserData() {
    session = EasyMock.createNiceMock(ISarosSession.class);
    EasyMock.expect(session.isHost()).andStubReturn(true);
    EasyMock.replay(session);

    UserInformationHandler handler = new UserInformationHandler(session, transmitter, receiver);

    User alice = new User(new JID("alice@test/Saros"), false, false, 0, 0);

    handler.start();

    handler.synchronizeUserList(null, null, Collections.singletonList(alice));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSynchronizeWithEmptyAndNullUserData() {
    session = EasyMock.createNiceMock(ISarosSession.class);
    EasyMock.expect(session.isHost()).andStubReturn(true);
    EasyMock.replay(session);

    UserInformationHandler handler = new UserInformationHandler(session, transmitter, receiver);

    User alice = new User(new JID("alice@test/Saros"), false, false, 0, 0);

    handler.start();

    handler.synchronizeUserList(emptyUserList, null, Collections.singletonList(alice));
  }

  @Test(timeout = 30000)
  public void testSynchronizeWhileUserLeavesSession() {
    session = EasyMock.createNiceMock(ISarosSession.class);
    EasyMock.expect(session.isHost()).andStubReturn(true);
    EasyMock.expect(session.getRemoteUsers()).andStubReturn(emptyUserList);
    EasyMock.replay(session);

    UserInformationHandler handler = new UserInformationHandler(session, transmitter, receiver);

    User alice = new User(new JID("alice@test/Saros"), false, false, 0, 0);
    User bob = new User(new JID("bob@test/Saros"), false, false, 0, 0);

    alice.setInSession(false);

    handler.start();

    List<User> notResponded =
        handler.synchronizeUserList(
            Collections.singletonList(bob), null, Collections.singletonList(alice));

    assertEquals(
        "notResponded must be empty as the user left the session while synchronizing",
        emptyUserList,
        notResponded);
  }
}
