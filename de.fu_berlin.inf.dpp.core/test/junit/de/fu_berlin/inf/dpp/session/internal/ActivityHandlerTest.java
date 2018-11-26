package de.fu_berlin.inf.dpp.session.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import de.fu_berlin.inf.dpp.activities.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.ChecksumErrorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.FolderMovedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.ITargetedActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.PermissionActivity;
import de.fu_berlin.inf.dpp.activities.ProgressActivity;
import de.fu_berlin.inf.dpp.activities.QueueItem;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.StartFollowingActivity;
import de.fu_berlin.inf.dpp.activities.StopActivity;
import de.fu_berlin.inf.dpp.activities.StopFollowingActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.IActivityHandlerCallback;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.test.fakes.synchonize.NonUISynchronizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActivityHandlerTest {

  // SUT
  private ActivityHandler handler;
  private NonUISynchronizer synchronizer;

  // Results from Callback
  private IActivity transformedActivity;
  private IActivity localActivity;
  private volatile boolean willBeSent;
  private List<User> targets = new ArrayList<User>();

  // Input
  private List<IActivity> activities;

  // SessionUsers
  private List<User> participants;
  private List<User> remoteUsers;
  private List<User> remoteUsersWithProjects;

  private User alice;
  private User bob;
  private User carl;
  private User dave;

  // Needed to compare localActivities
  private volatile CountDownLatch gate;

  // Roles of the Users in this Test
  private User target;
  private User source;
  private boolean host;

  // Callback that is called from the ActivityHandler
  public IActivityHandlerCallback callback =
      new IActivityHandlerCallback() {

        @Override
        public void send(List<User> recipients, IActivity activity) {

          willBeSent = true;
          targets = recipients;
          transformedActivity = activity;
        }

        @Override
        public void execute(IActivity activity) {
          // As this is called by another Thread we have to inform the caller
          // when we are ready
          localActivity = activity;
          CountDownLatch gateToCountdown = gate;

          if (gateToCountdown != null) gateToCountdown.countDown();
        }
      };
  private SPath path;

  @Before
  public void setUp() {

    // Set up users
    createUsers();
    // Set up the SUT and create needed Mocks
    createSUT();

    // The setParameters-method should be called at the beginning of every
    // Testcase. This is the default-setting where the localUser is the Host
    // and source of Activities and bob is the target.
    setParameters(bob, alice, true);
    // start the SWT-Mock and the dispatchThread
    synchronizer.start();
    handler.start();
  }

  @After
  public void tearDown() {
    // stop the SWT-Mock and the dispatchThread
    handler.stop();
    synchronizer.stop();
  }

  /** This Test tests the handleOutgoingActivities-method produces only activities for the host. */
  @Test
  public void ClientSendTest() {

    // init Session
    setParameters(bob, alice, true);

    for (IActivity activity : activities) {

      willBeSent = false;

      // SUT-CALL
      handler.handleOutgoingActivities(Collections.singletonList(activity));

      if (!willBeSent) {
        fail("Activity: " + activity + " was not send.");
      } else if (targets.size() == 0) {
        fail("No target for " + activity);
      } else if (targets.size() > 1) {
        fail("To many targets for " + activity);
      } else if (!targets.get(0).equals(alice)) {
        fail(
            "Wrong target specified for "
                + activity
                + ". Should have been host but was "
                + targets.get(0));
      } else if (activity instanceof TextEditActivity) {
        assertTrue(
            "TextEditActivity was not transformed", transformedActivity instanceof JupiterActivity);
      } else {
        assertEquals("Wrong Activity after Transformation.", activity, transformedActivity);
      }
    }
  }

  /**
   * This tests if the handleIncomingActivities-method at the host produces the right activities to
   * send to others.
   */
  @Test
  public void ServerSendTest() {

    setParameters(bob, alice, true);

    for (IActivity activity : activities) {

      willBeSent = false;

      // SUT-CALL
      handler.handleIncomingActivities(Collections.singletonList(activity));

      if (!willBeSent) {
        fail("Activity: " + activity + " was not send.");
      } else if (activity instanceof ITargetedActivity) {
        assertEquals("Wrong target for" + activity, targets.get(0), bob);
        assertEquals("Wrong activity after Transformation.", activity, transformedActivity);
      } else {
        List<User> expectedUsers;
        if (activity instanceof IResourceActivity) {
          expectedUsers = remoteUsersWithProjects;
          assertFalse("User without Project received Activity" + activity, targets.contains(dave));
        } else {
          expectedUsers = remoteUsers;
        }
        assertTrue("Wrong targets for" + activity, targets.containsAll(expectedUsers));
        assertEquals("Wrong activity after Transformation.", activity, transformedActivity);
      }
    }
  }

  /**
   * This tests if the handleIncomingActivities-method at the host produces the right activities for
   * the localUser.
   */
  @Test
  public void ServerExecuteLocallyTest() {

    setParameters(alice, bob, true);

    for (IActivity activity : activities) {

      gate = new CountDownLatch(1);

      // SUT-CALL
      handler.handleIncomingActivities(Collections.singletonList(activity));

      // As the dispatching is performed by a different Thread we have
      // to wait for it to dispatch the activity
      try {
        if (!gate.await(10, TimeUnit.SECONDS)) {
          fail(activity + " was not dispatched");
        }
      } catch (InterruptedException e) {
        fail("Interupted");
      }

      if (activity instanceof JupiterActivity) {
        assertTrue(activity + " not Transformed", localActivity instanceof TextEditActivity);
      } else {
        assertEquals("Wrong Activity dispatched", activity, localActivity);
      }
    }
  }

  /**
   * This tests if the handleIncomingActivities-method at the client produces the right activities
   * for the localUser.
   */
  @Test
  public void ClientExecuteLocallyTest() {

    setParameters(alice, bob, false);
    // just checks that no activities where be send
    willBeSent = false;

    for (IActivity activity : activities) {

      gate = new CountDownLatch(1);

      // SUT-CALL
      handler.handleIncomingActivities(Collections.singletonList(activity));

      // As the dispatching is performed by a different Thread we have
      // to wait for it to dispatch the activity
      try {
        if (!gate.await(5, TimeUnit.SECONDS)) {
          fail(activity + " was not dispatched");
        }
      } catch (InterruptedException e) {
        fail("Interupted");
      }
      if (activity instanceof JupiterActivity) {
        assertTrue(activity + " not Transformed", localActivity instanceof TextEditActivity);
      } else {
        assertEquals("Wrong Activity dispatched", activity, localActivity);
      }
    }
    if (willBeSent) {
      fail("Some activity was send");
    }
  }

  /**
   * Specifies the roles of participants in the session. Should be called at the start of every test
   * case
   *
   * @param target The target for targeted Activities
   * @param source The source of Activities
   * @param host True if the localUser is supposed to be the host.
   */
  private void setParameters(User target, User source, boolean host) {
    this.target = target;
    this.source = source;
    this.host = host;
  }

  /** creates the Users used in this test */
  private void createUsers() {
    // local User
    alice = createUser(true);

    // remote users
    bob = createUser(false);
    carl = createUser(false);
    // User that can't process IResourceActivities
    dave = createUser(false);

    // Add users to the lists

    remoteUsersWithProjects = new ArrayList<User>();
    remoteUsersWithProjects.add(carl);
    remoteUsersWithProjects.add(bob);

    remoteUsers = new ArrayList<User>();
    remoteUsers.addAll(remoteUsersWithProjects);
    remoteUsers.add(dave);

    participants = new ArrayList<User>();
    participants.addAll(remoteUsers);
    participants.add(alice);
  }

  private User createUser(boolean local) {
    User user = EasyMock.createMock(User.class);
    EasyMock.expect(user.isLocal()).andStubReturn(local);
    EasyMock.expect(user.isInSession()).andStubReturn(true);
    EasyMock.replay(user);
    return user;
  }

  /** Sets up the session and the SUT. */
  private void createSUT() {

    synchronizer = new NonUISynchronizer();

    // Create SessionMock
    ISarosSession sessionMock = EasyMock.createMock(ISarosSession.class);
    EasyMock.expect(sessionMock.getLocalUser()).andStubReturn(alice);
    EasyMock.expect(sessionMock.getHost()).andStubReturn(alice);
    // read host-Variable at runtime.
    EasyMock.expect(sessionMock.isHost())
        .andAnswer(
            new IAnswer<Boolean>() {
              @Override
              public Boolean answer() throws Throwable {
                return host;
              }
            })
        .anyTimes();

    IProject project = EasyMock.createMock(IProject.class);

    EasyMock.expect(sessionMock.userHasProject(dave, project)).andStubReturn(false);
    for (User user : remoteUsersWithProjects) {
      EasyMock.expect(sessionMock.userHasProject(user, project)).andStubReturn(true);
    }

    EasyMock.expect(sessionMock.getUsers()).andStubReturn(participants);
    EasyMock.expect(sessionMock.getRemoteUsers()).andStubReturn(remoteUsers);
    EasyMock.replay(sessionMock);

    // create ActivityMocks
    activities = new ArrayList<IActivity>();
    activities.add(EasyMock.createNiceMock(ChangeColorActivity.class));
    activities.add(EasyMock.createNiceMock(ViewportActivity.class));
    activities.add(EasyMock.createNiceMock(TextSelectionActivity.class));
    activities.add(EasyMock.createNiceMock(PermissionActivity.class));
    activities.add(EasyMock.createNiceMock(FolderCreatedActivity.class));
    activities.add(EasyMock.createNiceMock(FolderDeletedActivity.class));
    activities.add(EasyMock.createNiceMock(FolderMovedActivity.class));
    activities.add(EasyMock.createNiceMock(FileActivity.class));
    activities.add(EasyMock.createNiceMock(EditorActivity.class));
    activities.add(EasyMock.createNiceMock(StopActivity.class));
    activities.add(EasyMock.createNiceMock(JupiterActivity.class));
    activities.add(EasyMock.createNiceMock(ChecksumErrorActivity.class));
    activities.add(EasyMock.createNiceMock(ProgressActivity.class));
    activities.add(EasyMock.createNiceMock(ChangeColorActivity.class));
    activities.add(EasyMock.createNiceMock(StartFollowingActivity.class));
    activities.add(EasyMock.createNiceMock(StopFollowingActivity.class));
    activities.add(EasyMock.createNiceMock(TextEditActivity.class));
    activities.add(EasyMock.createNiceMock(ChecksumActivity.class));

    path = EasyMock.createMock(SPath.class);
    EasyMock.expect(path.getProject()).andStubReturn(project);
    EasyMock.replay(path);

    // Assign Targets and Source to activities
    for (IActivity activity : activities) {
      if (activity instanceof ITargetedActivity) {
        // read target-Variable at runtime.
        EasyMock.expect(((ITargetedActivity) activity).getTarget())
            .andAnswer(
                new IAnswer<User>() {
                  @Override
                  public User answer() throws Throwable {
                    return target;
                  }
                })
            .anyTimes();
      }
      // read source-Variable at runtime.
      EasyMock.expect(activity.getSource())
          .andAnswer(
              new IAnswer<User>() {
                @Override
                public User answer() throws Throwable {
                  return source;
                }
              })
          .anyTimes();

      if (activity instanceof IResourceActivity) {
        EasyMock.expect(((IResourceActivity) activity).getPath()).andStubReturn(path);
      }
      EasyMock.replay(activity);
    }

    // create CDC-Mock
    ConcurrentDocumentClient client = EasyMock.createNiceMock(ConcurrentDocumentClient.class);

    // Mock transformToJupiter-method and transformFromJupiter-method
    for (IActivity activity : activities) {
      // transform TextEditActivity to JupiterActivities (outgoing)
      if (activity instanceof TextEditActivity) {
        EasyMock.expect(client.transformToJupiter(activity))
            .andReturn(EasyMock.createNiceMock(JupiterActivity.class));
        // return other activities
      } else {
        EasyMock.expect(client.transformToJupiter(activity)).andReturn(activity);
      }
      // transform JupiterActivities to TextEditActivity (incoming)
      if (activity instanceof JupiterActivity) {
        EasyMock.expect(client.transformFromJupiter(activity))
            .andReturn(
                Collections.singletonList(
                    (IActivity) EasyMock.createNiceMock(TextEditActivity.class)));
        // return other activities
      } else {
        EasyMock.expect(client.transformFromJupiter(activity))
            .andReturn(Collections.singletonList(activity));
      }
    }
    EasyMock.replay(client);

    // create CDS-Mock
    ConcurrentDocumentServer server = EasyMock.createNiceMock(ConcurrentDocumentServer.class);
    for (IActivity activity : activities) {
      // just return activities as the server doesn't change the type of
      // activities
      EasyMock.expect(server.transformIncoming(activity))
          .andReturn(Collections.singletonList(new QueueItem(participants, activity)));
    }

    EasyMock.replay(server);

    // create SUT
    handler = new ActivityHandler(sessionMock, callback, server, client, synchronizer);
  }
}
