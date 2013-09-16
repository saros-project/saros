package de.fu_berlin.inf.dpp.project.internal;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.jivesoftware.smack.filter.PacketFilter;
import org.junit.Before;
import org.junit.Test;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;

public class UserInformationHandlerTest {

    private final List<User> emptyUserList = Collections.emptyList();

    private SessionIDObservable sessionID;
    private ITransmitter transmitter;
    private IReceiver receiver;
    private ISarosSession session;
    private SarosPacketCollector dummyCollector;

    @Before
    public void setUp() {
        sessionID = new SessionIDObservable();
        transmitter = EasyMock.createNiceMock(ITransmitter.class);
        receiver = EasyMock.createNiceMock(IReceiver.class);
        dummyCollector = EasyMock.createNiceMock(SarosPacketCollector.class);

        EasyMock.expect(
            receiver.createCollector(EasyMock.isA(PacketFilter.class)))
            .andStubReturn(dummyCollector);
        EasyMock.replay(transmitter, receiver, dummyCollector);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynchronizeWithEmptyRemoteUsersCollection() {
        session = EasyMock.createNiceMock(ISarosSession.class);
        EasyMock.expect(session.isHost()).andStubReturn(true);
        EasyMock.replay(session);

        UserInformationHandler handler = new UserInformationHandler(session,
            sessionID, transmitter, receiver);

        User alice = new User(session, new JID("alice@test/Saros"), 0, 0);

        handler.start();

        handler.synchronizeUserList(Collections.singletonList(alice), null,
            emptyUserList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynchronizeWithEmptyUserData() {
        session = EasyMock.createNiceMock(ISarosSession.class);
        EasyMock.expect(session.isHost()).andStubReturn(true);
        EasyMock.replay(session);

        UserInformationHandler handler = new UserInformationHandler(session,
            sessionID, transmitter, receiver);

        User alice = new User(session, new JID("alice@test/Saros"), 0, 0);

        handler.start();

        handler.synchronizeUserList(emptyUserList, emptyUserList,
            Collections.singletonList(alice));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynchronizeWithNullUserData() {
        session = EasyMock.createNiceMock(ISarosSession.class);
        EasyMock.expect(session.isHost()).andStubReturn(true);
        EasyMock.replay(session);

        UserInformationHandler handler = new UserInformationHandler(session,
            sessionID, transmitter, receiver);

        User alice = new User(session, new JID("alice@test/Saros"), 0, 0);

        handler.start();

        handler.synchronizeUserList(null, null,
            Collections.singletonList(alice));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynchronizeWithEmptyAndNullUserData() {
        session = EasyMock.createNiceMock(ISarosSession.class);
        EasyMock.expect(session.isHost()).andStubReturn(true);
        EasyMock.replay(session);

        UserInformationHandler handler = new UserInformationHandler(session,
            sessionID, transmitter, receiver);

        User alice = new User(session, new JID("alice@test/Saros"), 0, 0);

        handler.start();

        handler.synchronizeUserList(emptyUserList, null,
            Collections.singletonList(alice));
    }

    @Test(timeout = 30000)
    public void testSynchronizeWhileUserLeavesSession() {
        session = EasyMock.createNiceMock(ISarosSession.class);
        EasyMock.expect(session.isHost()).andStubReturn(true);
        EasyMock.expect(session.getRemoteUsers()).andStubReturn(emptyUserList);
        EasyMock.replay(session);

        UserInformationHandler handler = new UserInformationHandler(session,
            sessionID, transmitter, receiver);

        User alice = new User(session, new JID("alice@test/Saros"), 0, 0);
        User bob = new User(session, new JID("alice@test/Saros"), 0, 0);

        handler.start();

        List<User> notResponded = handler.synchronizeUserList(
            Collections.singletonList(bob), null,
            Collections.singletonList(alice));

        assertEquals(
            "notResponded must be empty as the user left the session while synchronizing",
            emptyUserList, notResponded);
    }
}
