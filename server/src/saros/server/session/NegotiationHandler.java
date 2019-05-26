package saros.server.session;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import saros.filesystem.IReferencePoint;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.AbstractOutgoingProjectNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.NegotiationTools;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.ProjectNegotiation;
import saros.negotiation.SessionNegotiation;
import saros.server.ServerConfig;
import saros.server.progress.ConsoleProgressIndicator;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;
import saros.util.NamedThreadFactory;

public class NegotiationHandler implements INegotiationHandler {

  private static final Logger log = Logger.getLogger(NegotiationHandler.class);

  private final ISarosSessionManager sessionManager;
  private final ThreadPoolExecutor sessionExecutor =
      new ThreadPoolExecutor(
          0,
          4,
          60,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(),
          new NamedThreadFactory("ServerSessionNegotiation-"));
  private final ThreadPoolExecutor projectExecutor =
      new ThreadPoolExecutor(
          0,
          10,
          60,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(),
          new NamedThreadFactory("ServerProjectNegotiation-"));

  public NegotiationHandler(ISarosSessionManager sessionManager) {
    sessionManager.setNegotiationHandler(this);
    this.sessionManager = sessionManager;
  }

  @Override
  public void handleOutgoingSessionNegotiation(final OutgoingSessionNegotiation negotiation) {

    sessionExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            SessionNegotiation.Status status;
            if (ServerConfig.isInteractive()) {
              status = negotiation.start(new ConsoleProgressIndicator(System.out));
            } else {
              status = negotiation.start(new NullProgressMonitor());
            }

            switch (status) {
              case OK:
                sessionManager.startSharingProjects(negotiation.getPeer());
                break;
              case ERROR:
                log.error("ERROR running session negotiation: " + negotiation.getErrorMessage());
                break;
              case REMOTE_ERROR:
                log.error(
                    "REMOTE_ERROR running session negotiation: "
                        + negotiation.getErrorMessage()
                        + " at remote: "
                        + negotiation.getPeer().toString());
                break;
              case CANCEL:
                log.info("Session negotiation was cancelled locally");
                break;
              case REMOTE_CANCEL:
                log.info(
                    "Session negotiation was cancelled by remote: "
                        + negotiation.getPeer().toString());
                break;
            }
          }
        });
  }

  @Override
  public void handleIncomingSessionNegotiation(final IncomingSessionNegotiation negotiation) {

    sessionExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            negotiation.localCancel(
                "Server does not accept session invites",
                NegotiationTools.CancelOption.NOTIFY_PEER);
            negotiation.accept(new NullProgressMonitor());
          }
        });
  }

  @Override
  public void handleOutgoingProjectNegotiation(
      final AbstractOutgoingProjectNegotiation negotiation) {

    projectExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            ProjectNegotiation.Status status;
            if (ServerConfig.isInteractive()) {
              status = negotiation.run(new ConsoleProgressIndicator(System.out));
            } else {
              status = negotiation.run(new NullProgressMonitor());
            }

            switch (status) {
              case ERROR:
                log.error("ERROR running project negotiation: " + negotiation.getErrorMessage());
                break;
              case REMOTE_ERROR:
                log.error(
                    "REMOTE_ERROR running project negotiation: "
                        + negotiation.getErrorMessage()
                        + " at remote: "
                        + negotiation.getPeer().toString());
                break;
              case CANCEL:
                log.info("Project negotiation was cancelled locally");
                break;
              case REMOTE_CANCEL:
                log.info(
                    "Project negotiation was cancelled by remote: "
                        + negotiation.getPeer().toString());
                break;
            }
          }
        });
  }

  @Override
  public void handleIncomingProjectNegotiation(
      final AbstractIncomingProjectNegotiation negotiation) {

    projectExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            negotiation.localCancel(
                "Server does not accept incoming projects",
                NegotiationTools.CancelOption.NOTIFY_PEER);
            negotiation.run(new HashMap<String, IReferencePoint>(), new NullProgressMonitor());
          }
        });
  }
}
