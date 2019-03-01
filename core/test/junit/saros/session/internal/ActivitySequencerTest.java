package saros.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import saros.preferences.IPreferenceStore;
import saros.preferences.PreferenceStore;
import saros.session.User;
import saros.test.fakes.net.FakeConnectionFactory;
import saros.test.fakes.net.FakeConnectionFactory.FakeConnectionFactoryResult;
import saros.test.stubs.SarosSessionStub;

public class ActivitySequencerTest {

  private static class SequencerSessionStub extends SarosSessionStub {

    private List<IActivity> receivedActivities = new ArrayList<IActivity>();

    private Set<User> users = new HashSet<User>();

    private User localUser;

    private String id;

    public void setLocalUser(User localUser) {
      this.localUser = localUser;
      users.add(localUser);
    }

    public void setID(String id) {
      this.id = id;
    }

    /*
     * can rename to something else, does not need override addUser if
     * needed
     */
    @Override
    public void addUser(User user, IPreferenceStore properties) {
      users.add(user);
    }

    @Override
    public User getUser(JID jid) {
      for (User user : users) if (user.getJID().equals(jid)) return user;

      return null;
    }

    @Override
    public User getLocalUser() {
      return localUser;
    }

    @Override
    public synchronized void exec(List<IActivity> activities) {
      receivedActivities.addAll(activities);
    }

    @Override
    public String getID() {
      return id == null ? "0815" : id;
    }

    public synchronized List<IActivity> getReceivedActivities() {
      return new ArrayList<IActivity>(receivedActivities);
    }
  }

  private static final JID ALICE_JID = new JID("alice@test/Saros");
  private static final JID BOB_JID = new JID("bob@test/Saros");

  private SequencerSessionStub sessionStubAlice;
  private SequencerSessionStub sessionStubBob;

  private User aliceUser;
  private User bobUser;

  private ITransmitter aliceTransmitter;
  private ITransmitter bobTransmitter;

  private IReceiver aliceReceiver;
  private IReceiver bobReceiver;

  private ActivitySequencer aliceSequencer;
  private ActivitySequencer bobSequencer;

  @Before
  public void setUp() {
    sessionStubAlice = new SequencerSessionStub();
    sessionStubBob = new SequencerSessionStub();

    aliceUser = new User(ALICE_JID, true, true, 0, 0);
    bobUser = new User(BOB_JID, false, true, 0, 0);

    sessionStubAlice.setLocalUser(aliceUser);
    sessionStubBob.setLocalUser(bobUser);

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

    User bobUserInAliceSession = new User(BOB_JID, false, false, 0, 0);

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

    User bobUserInAliceSession = new User(BOB_JID, false, false, 0, 0);
    User aliceUserInBobSession = new User(ALICE_JID, true, false, 0, 0);

    sessionStubAlice.addUser(bobUserInAliceSession, new PreferenceStore());
    sessionStubBob.addUser(aliceUserInBobSession, new PreferenceStore());

    aliceSequencer.registerUser(bobUserInAliceSession);
    bobSequencer.registerUser(aliceUserInBobSession);

    for (int i = 0; i < activityCount; i++)
      aliceSequencer.sendActivity(
          Collections.singletonList(bobUserInAliceSession),
          new NOPActivity(aliceUser, bobUserInAliceSession, i));

    aliceSequencer.flush(bobUserInAliceSession);

    List<IActivity> receivedActivities = sessionStubBob.getReceivedActivities();

    assertEquals("not all activies received", activityCount, receivedActivities.size());

    for (int i = 0; i < activityCount; i++) {
      NOPActivity activity = (NOPActivity) receivedActivities.get(i);
      assertEquals("activity is out of order", i, activity.getID());
    }
  }

  @Test(timeout = 30000)
  public void testSendWithoutRegisteredUser() {

    aliceSequencer = new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    bobSequencer = new ActivitySequencer(sessionStubBob, bobTransmitter, bobReceiver, null);

    aliceSequencer.start();
    bobSequencer.start();

    User bobUserInAliceSession = new User(BOB_JID, false, false, 0, 0);
    User aliceUserInBobSession = new User(ALICE_JID, true, false, 0, 0);

    sessionStubAlice.addUser(bobUserInAliceSession, new PreferenceStore());
    sessionStubBob.addUser(aliceUserInBobSession, new PreferenceStore());

    bobSequencer.registerUser(aliceUserInBobSession);

    aliceSequencer.sendActivity(
        Collections.singletonList(bobUserInAliceSession),
        new NOPActivity(aliceUser, bobUserInAliceSession, 0));

    aliceSequencer.flush(bobUserInAliceSession);

    List<IActivity> receivedActivities = sessionStubBob.getReceivedActivities();

    assertEquals(
        "received activies although the user is not registered on sender side",
        0,
        receivedActivities.size());
  }

  @Test(timeout = 30000)
  public void testReceiveWithoutRegisteredUser() {

    aliceSequencer = new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    bobSequencer = new ActivitySequencer(sessionStubBob, bobTransmitter, bobReceiver, null);

    aliceSequencer.start();
    bobSequencer.start();

    User bobUserInAliceSession = new User(BOB_JID, false, false, 0, 0);
    User aliceUserInBobSession = new User(ALICE_JID, true, false, 0, 0);

    sessionStubAlice.addUser(bobUserInAliceSession, new PreferenceStore());
    sessionStubBob.addUser(aliceUserInBobSession, new PreferenceStore());

    aliceSequencer.registerUser(bobUserInAliceSession);

    aliceSequencer.sendActivity(
        Collections.singletonList(bobUserInAliceSession),
        new NOPActivity(aliceUser, bobUserInAliceSession, 0));

    aliceSequencer.flush(bobUserInAliceSession);

    List<IActivity> receivedActivities = sessionStubBob.getReceivedActivities();

    assertEquals(
        "received activies although the user is not registered on receiver side",
        0,
        receivedActivities.size());
  }

  @Test(timeout = 30000)
  public void testSendAndReceiveWithDifferendSessionIDs() {

    aliceSequencer = new ActivitySequencer(sessionStubAlice, aliceTransmitter, aliceReceiver, null);

    sessionStubBob.setID("4711");
    bobSequencer = new ActivitySequencer(sessionStubBob, bobTransmitter, bobReceiver, null);

    aliceSequencer.start();
    bobSequencer.start();

    User bobUserInAliceSession = new User(BOB_JID, false, false, 0, 0);
    User aliceUserInBobSession = new User(ALICE_JID, true, false, 0, 0);

    sessionStubAlice.addUser(bobUserInAliceSession, new PreferenceStore());
    sessionStubBob.addUser(aliceUserInBobSession, new PreferenceStore());

    aliceSequencer.registerUser(bobUserInAliceSession);
    bobSequencer.registerUser(aliceUserInBobSession);

    aliceSequencer.sendActivity(
        Collections.singletonList(bobUserInAliceSession),
        new NOPActivity(aliceUser, bobUserInAliceSession, 0));

    aliceSequencer.flush(bobUserInAliceSession);

    List<IActivity> receivedActivities = sessionStubBob.getReceivedActivities();

    assertEquals(
        "received activies although the session id is different on local and remote side",
        0,
        receivedActivities.size());
  }
}
