package saros.server.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import saros.filesystem.IReferencePoint;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.AbstractOutgoingResourceNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.NegotiationTools;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.ProjectNegotiationData;
import saros.negotiation.ResourceNegotiation;
import saros.negotiation.SessionNegotiation;
import saros.net.xmpp.JID;
import saros.server.ServerConfig;
import saros.server.filesystem.ServerProjectImpl;
import saros.server.filesystem.ServerWorkspaceImpl;
import saros.server.progress.ConsoleProgressIndicator;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;
import saros.util.NamedThreadFactory;

public class NegotiationHandler implements INegotiationHandler {

  private static final Logger log = Logger.getLogger(NegotiationHandler.class);

  private final ISarosSessionManager sessionManager;
  private final ServerWorkspaceImpl workspace;
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

  public NegotiationHandler(ISarosSessionManager sessionManager, ServerWorkspaceImpl workspace) {
    sessionManager.setNegotiationHandler(this);
    this.sessionManager = sessionManager;
    this.workspace = workspace;
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
      final AbstractOutgoingResourceNegotiation negotiation) {

    projectExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            ResourceNegotiation.Status status;
            if (ServerConfig.isInteractive()) {
              status = negotiation.run(new ConsoleProgressIndicator(System.out));
            } else {
              status = negotiation.run(new NullProgressMonitor());
            }

            if (status != ResourceNegotiation.Status.OK)
              handleErrorStatus(status, negotiation.getErrorMessage(), negotiation.getPeer());
          }
        });
  }

  @Override
  public void handleIncomingProjectNegotiation(
      final AbstractIncomingProjectNegotiation negotiation) {

    Map<String, IReferencePoint> projectMapping = new HashMap<>();

    for (ProjectNegotiationData data : negotiation.getProjectNegotiationData()) {
      String projectName = data.getProjectName();
      IReferencePoint project = workspace.getProject(projectName);

      // TODO: The file path is currently dictated by the name, potentially resulting in CONFLICTS
      if (!project.exists()) {
        try {
          ((ServerProjectImpl) project).create();
        } catch (IOException e) {
          negotiation.localCancel(
              "Error creating project folder", NegotiationTools.CancelOption.NOTIFY_PEER);
          return;
        }
      }

      projectMapping.put(data.getProjectID(), project);
    }

    projectExecutor.execute(
        new Runnable() {
          @Override
          public void run() {
            ResourceNegotiation.Status status;
            if (ServerConfig.isInteractive()) {
              status = negotiation.run(projectMapping, new ConsoleProgressIndicator(System.out));
            } else {
              status = negotiation.run(projectMapping, new NullProgressMonitor());
            }

            if (status != ResourceNegotiation.Status.OK)
              handleErrorStatus(status, negotiation.getErrorMessage(), negotiation.getPeer());
          }
        });
  }

  private void handleErrorStatus(ResourceNegotiation.Status status, String errorMessage, JID peer) {
    switch (status) {
      case ERROR:
        log.error("ERROR running project negotiation: " + errorMessage);
        break;
      case REMOTE_ERROR:
        log.error(
            "REMOTE_ERROR running project negotiation: "
                + errorMessage
                + " at remote: "
                + peer.toString());
        break;
      case CANCEL:
        log.info("Project negotiation was cancelled locally");
        break;
      case REMOTE_CANCEL:
        log.info("Project negotiation was cancelled by remote: " + peer.toString());
        break;
      default:
        throw new UnsupportedOperationException(
            "Unknown ProjectNegotation.Status (" + status + "): " + errorMessage);
    }
  }
}
