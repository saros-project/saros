package saros.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.jivesoftware.smack.packet.PacketExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.activities.IActivity;
import saros.activities.NOPActivity;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.session.ISarosSession;
import saros.session.User;
import saros.test.fakes.net.FakeConnectionFactory;
import saros.test.fakes.net.FakeConnectionFactory.FakeConnectionFactoryResult;

public class ActivitySequencerTest {

  private static final JID ALICE_JID = new JID("alice@test/Saros");
  private static final JID BOB_JID = new JID("bob@test/Saros");

  private final AtomicReference<String> aliceSessionId = new AtomicReference<>();
  private final AtomicReference<String> bobSessionId = new AtomicReference<>();

  private final User aliceUser = new User(ALICE_JID, true, true, null);
  private final User bobUser = new User(BOB_JID, false, true, null);

  private final User bobUserInAliceSession = new User(BOB_JID, false, false, null);
  private final User aliceUserInBobSession = new User(ALICE_JID, true, false, null);

  private List<IActivity> aliceReceivedActivitiesBuffer;
  private List<IActivity> bobReceivedActivitiesBuffer;

  private ISarosSession sessionStubAlice;
  private ISarosSession sessionStubBob;

  private ITransmitter aliceTransmitter;
  private ITransmitter bobTransmitter;

  private IReceiver aliceReceiver;
  private IReceiver bobReceiver;

  private ActivitySequencer aliceSequencer;
  private ActivitySequencer bobSequencer;

  @Before
  public void setUp() {

    aliceSessionId.set("0815");
    bobSessionId.set("0815");

    aliceReceivedActivitiesBuffer = Collections.synchronizedList(new ArrayList<>());
    bobReceivedActivitiesBuffer = Collections.synchronizedList(new ArrayList<>());

    sessionStubAlice =
        createSessionMock(
            aliceUser, bobUserInAliceSession, aliceSessionId, aliceReceivedActivitiesBuffer);
    sessionStubBob =
        createSessionMock(
            bobUser, aliceUserInBobSession, bobSessionId, bobReceivedActivitiesBuffer);

    FakeConnectionFactoryResult result =
        FakeConnectionFactory.createConnections(ALICE_JID, BOB_JID).withStrictJIDLookup().get();

    aliceTransmitter = result.getTransmitter(ALICE_JID);
    bobTransmitter = result.getTransmitter(BOB_JID);

    aliceReceiver = result.getReceiver(ALICE_JID);
    bobReceiver = result.getReceiver(BOB_JID);
  }

  // ENSURE that testStartAndStop works or this will crash the CI !!!!!!
  @After
  public void tearDown() {
    if (aliceSequencer != null) aliceSequencer.stop();

    if (bobSequencer != null) bobSequencer.stop();

    aliceSequencer = null;
    bobSequencer = null;
  }

  @Test(timeout = 30000)
  public void testStartAndStop() {
    ActivitySequencer sequencer =
        new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    sequencer.start();
    sequencer.stop();
  }

  @Test(timeout = 30000, expected = IllegalStateException.class)
  public void testMultipleStarts() {
    ActivitySequencer sequencer =
        new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    for (int i = 0; i < Integer.MAX_VALUE; i++) sequencer.start();
  }

  @Test(timeout = 30000, expected = IllegalStateException.class)
  public void testStopWithoutStart() {
    ActivitySequencer sequencer =
        new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    sequencer.stop();
  }

  @Test(timeout = 30000)
  public void testMultipleStops() {
    ActivitySequencer sequencer =
        new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    sequencer.start();
    try {
      sequencer.stop();
      sequencer.stop();
    } catch (Exception e) {
      e.printStackTrace();
      fail("stopping the sequencer multiple times should not raise any exception");
    }
  }

  @Test(timeout = 30000)
  public void testUnregisterUserOnTransmissionFailure() {

    ITransmitter brokenTransmitter = EasyMock.createNiceMock(ITransmitter.class);

    try {
      brokenTransmitter.send(
          EasyMock.anyObject(String.class),
          EasyMock.anyObject(JID.class),
          EasyMock.anyObject(PacketExtension.class));
    } catch (IOException e) {
      // cannot happen in recording mode
    }

    EasyMock.expectLastCall().andStubThrow(new IOException());

    EasyMock.replay(brokenTransmitter);

    aliceSequencer =
        new ActivitySequencer(sessionStubAlice, brokenTransmitter, aliceReceiver, null);

    aliceSequencer.start();

    aliceSequencer.registerUser(bobUserInAliceSession);

    assertTrue("Bob is not registered", aliceSequencer.isUserRegistered(bobUser));

    aliceSequencer.sendActivity(
        Collections.singletonList(bobUserInAliceSession),
        new NOPActivity(aliceUser, bobUserInAliceSession, 0));

    aliceSequencer.flush(bobUserInAliceSession);

    assertFalse("Bob is still registered", aliceSequencer.isUserRegistered(bobUserInAliceSession));
  }

  @Test(timeout = 30000)
  public void testSendAndFlushAndReceiveAndOrder() {

    int activityCount = 1000;

    aliceSequencer = new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    bobSequencer = new ActivitySequencer(sessionStubBob, bobTransmitter, bobReceiver, null);

    aliceSequencer.start();
    bobSequencer.start();

    aliceSequencer.registerUser(bobUserInAliceSession);
    bobSequencer.registerUser(aliceUserInBobSession);

    for (int i = 0; i < activityCount; i++)
      aliceSequencer.sendActivity(
          Collections.singletonList(bobUserInAliceSession),
          new NOPActivity(aliceUser, bobUserInAliceSession, i));

    aliceSequencer.flush(bobUserInAliceSession);

    assertEquals("not all activies received", activityCount, bobReceivedActivitiesBuffer.size());

    for (int i = 0; i < activityCount; i++) {
      NOPActivity activity = (NOPActivity) bobReceivedActivitiesBuffer.get(i);
      assertEquals("activity is out of order", i, activity.getID());
    }
  }

  @Test(timeout = 30000)
  public void testSendWithoutRegisteredUser() {

    aliceSequencer = new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    bobSequencer = new ActivitySequencer(sessionStubBob, bobTransmitter, bobReceiver, null);

    aliceSequencer.start();
    bobSequencer.start();

    bobSequencer.registerUser(aliceUserInBobSession);

    aliceSequencer.sendActivity(
        Collections.singletonList(bobUserInAliceSession),
        new NOPActivity(aliceUser, bobUserInAliceSession, 0));

    aliceSequencer.flush(bobUserInAliceSession);

    assertEquals(
        "received activies although the user is not registered on sender side",
        0,
        bobReceivedActivitiesBuffer.size());
  }

  @Test(timeout = 30000)
  public void testReceiveWithoutRegisteredUser() {

    aliceSequencer = new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    bobSequencer = new ActivitySequencer(sessionStubBob, bobTransmitter, bobReceiver, null);

    aliceSequencer.start();
    bobSequencer.start();

    aliceSequencer.registerUser(bobUserInAliceSession);

    aliceSequencer.sendActivity(
        Collections.singletonList(bobUserInAliceSession),
        new NOPActivity(aliceUser, bobUserInAliceSession, 0));

    aliceSequencer.flush(bobUserInAliceSession);

    assertEquals(
        "received activies although the user is not registered on receiver side",
        0,
        bobReceivedActivitiesBuffer.size());
  }

  @Test(timeout = 30000)
  public void testSendAndReceiveWithDifferendSessionIDs() {

    aliceSequencer = new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    bobSessionId.set("4711");
    bobSequencer = new ActivitySequencer(sessionStubBob, bobTransmitter, bobReceiver, null);

    aliceSequencer.start();
    bobSequencer.start();

    aliceSequencer.registerUser(bobUserInAliceSession);
    bobSequencer.registerUser(aliceUserInBobSession);

    aliceSequencer.sendActivity(
        Collections.singletonList(bobUserInAliceSession),
        new NOPActivity(aliceUser, bobUserInAliceSession, 0));

    aliceSequencer.flush(bobUserInAliceSession);

    assertEquals(
        "received activies although the session id is different on local and remote side",
        0,
        bobReceivedActivitiesBuffer.size());
  }

  private static ISarosSession createSessionMock(
      final User host,
      final User client,
      final AtomicReference<String> sessionId,
      final List<IActivity> receivedActivitiesBuffer) {

    final ISarosSession session = EasyMock.createMock(ISarosSession.class);

    EasyMock.expect(session.getID()).andAnswer(() -> sessionId.get()).anyTimes();

    EasyMock.expect(session.getLocalUser()).andStubReturn(host);

    EasyMock.expect(session.getUsers()).andStubReturn(Arrays.asList(host, client));

    final Capture<List<IActivity>> capture = Capture.newInstance();

    session.exec(EasyMock.anyObject(JID.class), EasyMock.capture(capture));

    EasyMock.expectLastCall()
        .andAnswer(() -> receivedActivitiesBuffer.addAll(capture.getValue()))
        .anyTimes();

    EasyMock.replay(session);
    return session;
  }
}
