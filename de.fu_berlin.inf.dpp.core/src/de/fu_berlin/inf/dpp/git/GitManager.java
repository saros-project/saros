package de.fu_berlin.inf.dpp.git;

import de.fu_berlin.inf.dpp.activities.GitCollectActivity;
import de.fu_berlin.inf.dpp.activities.GitRequestActivity;
import de.fu_berlin.inf.dpp.activities.GitSendBundleActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;

@Component(module = "core")
public class GitManager extends AbstractActivityProducer implements Startable {

  private static final Logger log = Logger.getLogger(GitManager.class.getName());

  private final SarosSession session;

  private File workDir = null;

  public GitManager(SarosSession session) {
    this.session = session;
  }

  @Override
  public synchronized void start() {
    session.addActivityConsumer(consumer, Priority.ACTIVE);
    session.addActivityProducer(this);
  }

  @Override
  public synchronized void stop() {
    session.removeActivityConsumer(consumer);
    session.removeActivityProducer(this);
  }

  public void sendCommitRequest() {
    log.debug("Sending Request");
    fireActivity(new GitRequestActivity(session.getLocalUser()));
  }

  public void changeWorkDir(File workDir) {
    this.workDir = workDir;
  }

  public File getWorkDir() {
    return workDir;
  }

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void receive(GitRequestActivity activity) {
          try {
            String basis = JGitFacade.getSHA1HashByRevisionString(workDir, "HEAD");
            fireActivity(new GitCollectActivity(session.getLocalUser(), basis));
          } catch (IOException e) {
            log.debug(session.getLocalUser().toString() + " can't access the Git Directory");
          }
        }

        @Override
        public void receive(GitCollectActivity activity) {
          String basis = activity.getBasis();
          try {
            File bundle = JGitFacade.createBundle(workDir, "HEAD", basis);
            if (bundle == null) {
              log.debug("bundle == NULL");
            } else {
              log.debug("bundle == not NULL");
            }
            fireActivity(new GitSendBundleActivity(session.getLocalUser(), bundle));
          } catch (IOException e) {
            log.debug("failed at create bundle");
          } catch (IllegalArgumentException e) {
            log.debug(e);
          }
        }

        @Override
        public void receive(GitSendBundleActivity activity) {
          File bundleFile = activity.getBundleFile();
          try {
            log.debug(session.getLocalUser() + "is fetching");
            JGitFacade.fetchFromBundle(workDir, bundleFile);
          } catch (Exception e) {
            log.debug(e);
          }
        }
      };
}
