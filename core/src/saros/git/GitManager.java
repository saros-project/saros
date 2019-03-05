package saros.git;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;
import saros.activities.GitCollectActivity;
import saros.activities.GitRequestActivity;
import saros.activities.GitSendBundleActivity;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.internal.SarosSession;

/**
 * This Class is responsible for the negotiation between a user that want to send,receive and fetch
 * commits.
 *
 * <p>In order to start the negotiation the user needs to call the method {@code
 * sendCommitRequest()}.
 *
 * <p>Before starting negotiation the user need to set up his working directory tree by calling the
 * method {@code changeWorkDirTree(File workDirTree)}. After calling it the first time the Manager
 * hold a {@link JGitFacade} object.
 */
public class GitManager extends AbstractActivityProducer implements Startable {

  private static final Logger log = Logger.getLogger(GitManager.class.getName());

  private final SarosSession session;

  JGitFacade jGitFacade;

  public GitManager(SarosSession session) {
    this.session = session;
  }

  @Override
  public void start() {
    session.addActivityProducer(this);
    session.addActivityConsumer(consumer, Priority.ACTIVE);
  }

  @Override
  public void stop() {
    session.removeActivityProducer(this);
    session.removeActivityConsumer(consumer);
  }

  public void sendCommitRequest() {
    fireActivity(new GitRequestActivity(session.getLocalUser()));
  }

  public void changeWorkDirTree(File workDirTree) throws IOException {
    if (jGitFacade == null) {
      this.jGitFacade = new JGitFacade(workDirTree);
    } else {
      this.jGitFacade.setWorkDirTree(workDirTree);
    }
  }

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(GitRequestActivity activity) {
          if (jGitFacade == null) return;

          try {
            String basis = jGitFacade.getSHA1HashByRevisionString("HEAD");
            fireActivity(new GitCollectActivity(session.getLocalUser(), basis));

          } catch (IOException e) {
            log.debug(e);
          }
        }

        @Override
        public void receive(GitCollectActivity activity) {
          if (jGitFacade == null) return;

          String basis = activity.getBasis();

          try {
            byte[] bundle = jGitFacade.createBundle("HEAD", basis);
            fireActivity(new GitSendBundleActivity(session.getLocalUser(), bundle));

          } catch (IOException e) {
            log.debug(e);
          } catch (IllegalArgumentException e) {
            log.debug(e);
          }
        }

        @Override
        public void receive(GitSendBundleActivity activity) {
          if (jGitFacade == null) return;

          byte[] bundle = activity.getBundle();

          try {
            jGitFacade.fetchFromBundle(bundle);
          } catch (Exception e) {
            log.debug(e);
          }
        }
      };
}
