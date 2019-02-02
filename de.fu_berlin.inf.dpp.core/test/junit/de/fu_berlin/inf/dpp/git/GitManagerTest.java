package de.fu_berlin.inf.dpp.git;

import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import de.fu_berlin.inf.dpp.activities.GitCollectActivity;
import de.fu_berlin.inf.dpp.activities.GitRequestActivity;
import de.fu_berlin.inf.dpp.activities.GitSendBundleActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import java.io.File;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    alicesSession = EasyMock.createMock(SarosSession.class);

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

    bobsSession = EasyMock.createMock(SarosSession.class);

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
  public void testChangeWorkDir() {
    File testfile = new File(".");
    GitManager gitManager = new GitManager(alicesSession);
    gitManager.start();
    File oldFile = gitManager.getWorkDir();
    gitManager.changeWorkDir(testfile);
    assertNotEquals(oldFile, gitManager.getWorkDir());
    gitManager.stop();
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

  // alice create the bundle
  private File alicesWorkDir;
  private File bobsWorkDir;

  // alices HEAD will be 1 commit ahead bob HEAD after the set up
  @Before
  public void setUp() throws IllegalArgumentException, Exception {
    alicesWorkDir = tempFolder.newFolder("TempDir1");
    bobsWorkDir = tempFolder.newFolder("TempDir2");

    JGitFacadeTest.initNewRepo(alicesWorkDir);
    JGitFacadeTest.writeCommitToRepo(alicesWorkDir, 2);

    JGitFacade.cloneFromRepo(alicesWorkDir, bobsWorkDir);

    JGitFacadeTest.writeCommitToRepo(alicesWorkDir, 3);
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

    // here starts the test of the new added logic
    alicesGitManager.changeWorkDir(alicesWorkDir);
    bobsGitManager.changeWorkDir(bobsWorkDir);

    assertNotEquals(
        JGitFacadeTest.getObjectIdByRevisionString(alicesWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(bobsWorkDir, "FETCH_HEAD"));

    alicesGitManager.sendCommitRequest();

    assertEquals(
        JGitFacadeTest.getObjectIdByRevisionString(alicesWorkDir, "HEAD"),
        JGitFacadeTest.getObjectIdByRevisionString(bobsWorkDir, "FETCH_HEAD"));

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
    return new GitSendBundleActivity(
        rewriteUser(inActivity.getSource()), inActivity.getBundleFile());
  }

  private static User rewriteUser(User user) {
    User copy = new User(user.getJID(), user.isHost(), !user.isLocal(), user.getColorID(), -1);
    copy.setInSession(true);
    return copy;
  }
}
