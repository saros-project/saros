package saros.concurrent.management;

import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import saros.activities.JupiterActivity;
import saros.concurrent.jupiter.Algorithm;
import saros.concurrent.jupiter.internal.Jupiter;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.filesystem.IFile;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.session.User;
import saros.synchronize.UISynchronizer;
import saros.util.NamedThreadFactory;

/**
 * This class generates and sends periodically Jupiter-NoOperations to acknowledge operations.
 *
 * <p>See
 * https://www.researchgate.net/publication/220876978_High-Latency_Low-Bandwidth_Windowing_in_the_Jupiter_Collaboration_System
 *
 * <p>Some fine points: In our system, messages must be saved until they are acknowledged by the
 * other party, since they may be needed in order to fix up incoming messages. Normally, these
 * acknowledgments are piggy-backed on traffic going the other way. However, it is possible for the
 * traffic to a window to be one-sided (e.g., for a status display window being periodically
 * updated). Therefore, each side must periodically generate explicit acknowledgments (i.e. no-op
 * messages) to prevent the outgoing queues from growing forever.
 */
public class HeartbeatDispatcher extends AbstractActivityProducer implements Startable {

  private static final Logger log = Logger.getLogger(HeartbeatDispatcher.class);

  private final ISarosSession session;
  private final UISynchronizer uiSynchronizer;
  private final JupiterClient jupiterClient;

  private ScheduledThreadPoolExecutor heartbeatScheduledExecutor;

  public HeartbeatDispatcher(
      final ISarosSession session,
      final UISynchronizer uiSynchronizer,
      final ConcurrentDocumentClient documentClient) {

    this.session = session;
    this.uiSynchronizer = uiSynchronizer;
    this.jupiterClient = documentClient.getJupiterClient();
  }

  @Override
  public void start() {

    session.addActivityProducer(this);

    heartbeatScheduledExecutor =
        new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("JupiterHeartbeat"));

    /* Schedule the heart-beat once per minute, this should be absolutely sufficient.
     * NOTE: This might even be to frequent for a lot of open documents, but
     * that likely causes other performance problems anyway.
     * Suggestion: store a map of the documents. Invoke the heart-beat more frequently but only fire heart-beats for a subset of documents so
     * that we do not produce too much traffic at once.
     * E.G: We have 50 documents, on the first interval fire changes for the first 10 documents.
     * On the next interval fire changes for the next 10 documents on so on.
     */

    // client documents should only be accessed by the main thread
    heartbeatScheduledExecutor.scheduleWithFixedDelay(
        () -> uiSynchronizer.syncExec(this::dispatchHeartbeats), 1, 1, TimeUnit.MINUTES);
  }

  private void dispatchHeartbeats() {

    assert uiSynchronizer.isUIThread() : "invalid thread access";

    final User localUser = session.getLocalUser();

    for (Entry<IFile, Jupiter> entry : jupiterClient.getClientDocs().entrySet()) {

      final IFile file = entry.getKey();
      final Algorithm jupiterAlgorithm = entry.getValue();

      final JupiterActivity heartbeat =
          jupiterAlgorithm.generateJupiterActivity(new NoOperation(), localUser, file);

      fireActivity(heartbeat);
    }
  }

  @Override
  public void stop() {
    session.removeActivityProducer(this);
    heartbeatScheduledExecutor.shutdown();

    try {
      if (!heartbeatScheduledExecutor.awaitTermination(10, TimeUnit.SECONDS))
        log.error(heartbeatScheduledExecutor + " is still running");

    } catch (InterruptedException e) {
      log.warn("interrupted while waiting for " + heartbeatScheduledExecutor + " to terminate", e);
      Thread.currentThread().interrupt();
    }
  }
}
