package saros.git;

import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import saros.activities.GitCollectActivity;
import saros.activities.GitRequestActivity;
import saros.activities.GitSendBundleActivity;
import saros.activities.IActivity;
import saros.net.xmpp.JID;
import saros.session.IActivityConsumer;
import saros.session.IActivityListener;
import saros.session.User;
import saros.session.internal.SarosSession;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SarosSession.class)
public class GitManagerTest {

  private SarosSession alicesSession;
  private IActivityConsumer alicesConsumer;
  private User alicesAlice;
  private User alicesBob;

  private SarosSession bobsSession;
  private IActivityConsumer bobsConsumer;
  private User bobsAlice;
  private User bobsBob;

  @Before
  public void createSessionsMocks() {
    alicesSession = PowerMock.createNiceMock(SarosSession.class);
    alicesSession.addActivityProducer(isA(GitManager.class));
    alicesSession.removeActivityProducer(isA(GitManager.class));

    alicesSession.addActivityConsumer(
        isA(IActivityConsumer.class), isA(IActivityConsumer.Priority.class));
    EasyMock.expectLastCall()
        .andStubAnswer(
            new IAnswer<Object>() {
              @Override
              public Object answer() throws Throwable {
                alicesConsumer = (IActivityConsumer) EasyMock.getCurrentArguments()[0];
                return null;
              }
            });
    alicesSession.removeActivityConsumer(isA(IActivityConsumer.class));

    alicesAlice = new User(new JID("alice"), true, true, 1, -1);
    alicesBob = new User(new JID("bob"), false, false, 2, -1);

    alicesAlice.setInSession(true);
    alicesBob.setInSession(true);

    alicesSession.getLocalUser();
    EasyMock.expectLastCall().andStubReturn(alicesAlice);

    EasyMock.replay(alicesSession);

    bobsSession = PowerMock.createNiceMock(SarosSession.class);

    bobsSession.addActivityProducer(isA(GitManager.class));
    bobsSession.removeActivityProducer(isA(GitManager.class));

    bobsSession.addActivityConsumer(
        isA(IActivityConsumer.class), isA(IActivityConsumer.Priority.class));
    EasyMock.expectLastCall()
        .andStubAnswer(
            new IAnswer<Object>() {
              @Override
              public Object answer() throws Throwable {
                bobsConsumer = (IActivityConsumer) EasyMock.getCurrentArguments()[0];
                return null;
              }
            });
    bobsSession.removeActivityConsumer(isA(IActivityConsumer.class));

    bobsAlice = new User(new JID("alice"), true, false, 1, -1);
    bobsBob = new User(new JID("bob"), false, true, 2, -1);

    bobsAlice.setInSession(true);
    bobsBob.setInSession(true);

    bobsSession.getLocalUser();
    EasyMock.expectLastCall().andStubReturn(bobsBob);

    EasyMock.replay(bobsSession);
  }

  @Test
  public void testCreation() {
    GitManager gitManager = new GitManager(alicesSession);
    gitManager.start();
    gitManager.stop();
    EasyMock.verify(alicesSession);
  }

  @Test
  public void testActivityCreationAndCancelation() {
    IActivityListener listener = EasyMock.createMock(IActivityListener.class);
    listener.created(isA(GitRequestActivity.class));
    EasyMock.expectLastCall()
        .andAnswer(
            new IAnswer<Object>() {

              @Override
              public Object answer() throws Throwable {
                GitRequestActivity activity =
                    (GitRequestActivity) EasyMock.getCurrentArguments()[0];
                Assert.assertEquals(alicesBob, activity.getSource());
                return null;
              }
            })
        .once();

    EasyMock.replay(listener);
  }

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  // alice is creating the bundle and bob is receiving and fetching it afterwards
  private File alicesWorkDirTree;
  private File bobsWorkDirTree;

  // alices HEAD will be 1 commit ahead bob's HEAD after the set up
  @Before
  public void setUp() throws IllegalArgumentException, Exception {
    alicesWorkDirTree = tempFolder.newFolder("TempDir1");

    JGitFacadeTest.initNewRepo(alicesWorkDirTree);
    JGitFacadeTest.writeCommitToRepo(alicesWorkDirTree, 2);

    bobsWorkDirTree = tempFolder.newFolder("TempDir2");

    JGitFacadeTest.cloneFromRepo(alicesWorkDirTree, bobsWorkDirTree);

    JGitFacadeTest.writeCommitToRepo(alicesWorkDirTree, 3);
  }

  @Test
  public void testChangeWorkDir() throws IOException {
    GitManager gitManager = new GitManager(alicesSession);
    gitManager.start();

    assertNull(gitManager.jGitFacade);

    gitManager.changeWorkDirTree(alicesWorkDirTree);

    assertNotNull(gitManager.jGitFacade);

    gitManager.stop();
  }

  @Test
  public void testRequestSendHashAndCommit() throws IllegalArgumentException, Exception {
    final GitManager alicesGitManager = new GitManager(alicesSession);
    alicesGitManager.start();
    final GitManager bobsGitManager = new GitManager(bobsSession);
    bobsGitManager.start();

    IActivityListener alicesListener = createForwarder(bobsConsumer);

    alicesGitManager.addActivityListener(alicesListener);

    IActivityListener bobsListener = createForwarder(alicesConsumer);

    bobsGitManager.addActivityListener(bobsListener);

    alicesGitManager.changeWorkDirTree(alicesWorkDirTree);
    bobsGitManager.changeWorkDirTree(bobsWorkDirTree);

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(alicesWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(bobsWorkDirTree, "FETCH_HEAD"));

    alicesGitManager.sendCommitRequest();

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(alicesWorkDirTree, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(bobsWorkDirTree, "FETCH_HEAD"));

    alicesGitManager.stop();
    EasyMock.verify(alicesSession);
    bobsGitManager.stop();
    EasyMock.verify(bobsSession);
  }

  private static IActivityListener createForwarder(final IActivityConsumer target) {
    return new IActivityListener() {
      @Override
      public void created(IActivity activity) {
        if (activity instanceof GitRequestActivity) {
          target.exec(rewriteGitRequestActivity((GitRequestActivity) activity));
        } else if (activity instanceof GitCollectActivity) {
          target.exec(rewriteGitCollectActivity((GitCollectActivity) activity));
        } else if (activity instanceof GitSendBundleActivity) {
          target.exec(rewriteGitSendBundleActivity((GitSendBundleActivity) activity));
        } else {
          return;
        }
      }
    };
  }

  private static GitRequestActivity rewriteGitRequestActivity(GitRequestActivity inActivity) {
    return new GitRequestActivity(rewriteUser(inActivity.getSource()));
  }

  private static GitCollectActivity rewriteGitCollectActivity(GitCollectActivity inActivity) {
    return new GitCollectActivity(rewriteUser(inActivity.getSource()), inActivity.getBasis());
  }

  private static GitSendBundleActivity rewriteGitSendBundleActivity(
      GitSendBundleActivity inActivity) {
    return new GitSendBundleActivity(rewriteUser(inActivity.getSource()), inActivity.getBundle());
  }

  private static User rewriteUser(User user) {
    User copy = new User(user.getJID(), user.isHost(), !user.isLocal(), user.getColorID(), -1);
    copy.setInSession(true);
    return copy;
  }
}
