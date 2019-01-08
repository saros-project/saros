package de.fu_berlin.inf.dpp.ui.eventhandler;

import de.fu_berlin.inf.dpp.communication.extensions.SessionStatusRequestExtension;
import de.fu_berlin.inf.dpp.communication.extensions.SessionStatusResponseExtension;
import de.fu_berlin.inf.dpp.filesystem.EclipseReferencePointManager;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

public final class SessionStatusRequestHandler {

  private static final Logger LOG = Logger.getLogger(SessionStatusRequestHandler.class);

  private final ISarosSessionManager sessionManager;

  private final IReceiver receiver;

  private final ITransmitter transmitter;

  private final IPreferenceStore preferenceStore;

  private IReferencePointManager referencePointManager;

  private EclipseReferencePointManager eclipseReferencePointManager;

  private final PacketListener statusRequestListener =
      new PacketListener() {

        @Override
        public void processPacket(final Packet packet) {
          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {
                  handleStatusRequest(new JID(packet.getFrom()));
                }
              });
        }
      };

  public SessionStatusRequestHandler(
      ISarosSessionManager sessionManager,
      ITransmitter transmitter,
      IReceiver receiver,
      IPreferenceStore preferenceStore,
      EclipseReferencePointManager eclipseReferencePointManager) {
    this.sessionManager = sessionManager;
    this.transmitter = transmitter;
    this.receiver = receiver;
    this.preferenceStore = preferenceStore;
    this.eclipseReferencePointManager = eclipseReferencePointManager;

    if (Boolean.getBoolean("de.fu_berlin.inf.dpp.server.SUPPORTED")) {
      this.receiver.addPacketListener(
          statusRequestListener, SessionStatusRequestExtension.PROVIDER.getPacketFilter());
    }
  }

  private void handleStatusRequest(JID from) {
    if (!preferenceStore.getBoolean(EclipsePreferenceConstants.SERVER_ACTIVATED)) return;

    ISarosSession session = sessionManager.getSession();
    SessionStatusResponseExtension response;

    if (session == null) {
      response = new SessionStatusResponseExtension();
    } else {
      // Don't count the server
      int participants = session.getUsers().size() - 1;

      this.referencePointManager = session.getComponent(IReferencePointManager.class);

      response = new SessionStatusResponseExtension(participants, getSessionDescription(session));
    }

    transmitter.sendPacketExtension(from, SessionStatusResponseExtension.PROVIDER.create(response));
  }

  private String getSessionDescription(ISarosSession session) {
    String description = "Projects: ";

    Set<IReferencePoint> referencePoints = session.getReferencePoints();
    int i = 0;
    int numOfProjects = referencePoints.size();

    for (IReferencePoint referencePoint : referencePoints) {
      description += eclipseReferencePointManager.get(referencePoint).getName();

      if (!session.isCompletelyShared(referencePoint)) description += " (partial)";

      if (i < numOfProjects - 1) description += ", ";

      i++;
    }

    if (numOfProjects == 0) {
      description += "none";
    }

    return description;
  }
}
