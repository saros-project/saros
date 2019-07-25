package saros.concurrent.management;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import saros.activities.JupiterActivity;
import saros.concurrent.jupiter.internal.text.NoOperation;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.synchronize.UISynchronizer;
import saros.util.NamedThreadFactory;

/**
 * This class sends periodically JupiterOperations to ack other clients Activities.
 *
 * <p>This fixes a logical memory leak, that is present in the Jupiter-implementation, if a client
 * does not participate in the session actively (usually Saros/S). This causes other clients to hold
 * JupiterActivities indefinitely to be able to merge them at all times. Sending 'NoOperation's
 * fixes this issue by acking received activities though the operation's vectorTime.
 */
public class HeartbeatDispatcher extends AbstractActivityProducer implements Startable {
  private ScheduledThreadPoolExecutor heartbeatScheduledExecutor;
  private final ISarosSession session;
  private final UISynchronizer uiSynchronizer;
  private final JupiterClient jupiter;

  public HeartbeatDispatcher(
      ISarosSession sarosSession,
      UISynchronizer uiSynchronizer,
      ConcurrentDocumentClient cdClient) {
    this.session = sarosSession;
    this.uiSynchronizer = uiSynchronizer;
    this.jupiter = cdClient.getJupiterClient();
  }

  @Override
  public void start() {
    session.addActivityProducer(this);
    heartbeatScheduledExecutor =
        new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("JupiterHeartbeat"));
    // Schedule the heartbeat once per minute, this should be absolutely
    // sufficient.
    // NOTE: This might even be to frequent for a lot of open documents, but
    // that likely
    // causes other performance problems anyway.
    heartbeatScheduledExecutor.scheduleWithFixedDelay(
        this::dispatchHeartbeats, 1, 1, TimeUnit.MINUTES);
  }

  private void dispatchHeartbeats() {
    // clientDocs should only be accessed by the main thread
    // FIXME syncExec not asyncExec
    uiSynchronizer.asyncExec(
        () ->
            jupiter
                .getClientDocs()
                .forEach(
                    (path, jupiter) -> {
                      JupiterActivity heartbeat =
                          jupiter.generateJupiterActivity(
                              new NoOperation(), session.getLocalUser(), path);
                      fireActivity(heartbeat);
                    }));
  }

  @Override
  public void stop() {
    session.removeActivityProducer(this);
    heartbeatScheduledExecutor.shutdown();
    // FIXME wait here until everything is finished
  }
}
