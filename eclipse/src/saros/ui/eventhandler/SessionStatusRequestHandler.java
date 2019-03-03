package saros.ui.eventhandler;

import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import saros.communication.extensions.SessionStatusRequestExtension;
import saros.communication.extensions.SessionStatusResponseExtension;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IReferencePointManager;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.preferences.EclipsePreferenceConstants;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.SWTUtils;

public final class SessionStatusRequestHandler {

  private static final Logger LOG = Logger.getLogger(SessionStatusRequestHandler.class);

  private final ISarosSessionManager sessionManager;

  private final IReceiver receiver;

  private final ITransmitter transmitter;

  private final IPreferenceStore preferenceStore;

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
      IPreferenceStore preferenceStore) {
    this.sessionManager = sessionManager;
    this.transmitter = transmitter;
    this.receiver = receiver;
    this.preferenceStore = preferenceStore;

    if (Boolean.getBoolean("saros.server.SUPPORTED")) {
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

      response = new SessionStatusResponseExtension(participants, getSessionDescription(session));
    }

    transmitter.sendPacketExtension(from, SessionStatusResponseExtension.PROVIDER.create(response));
  }

  private String getSessionDescription(ISarosSession session) {
    String description = "Projects: ";

    IReferencePointManager referencePointManager =
        session.getComponent(IReferencePointManager.class);

    Set<IReferencePoint> referencePoints = session.getReferencePoints();
    int i = 0;
    int numOfReferencePoints = referencePoints.size();

    for (IReferencePoint referencePoint : referencePoints) {
      description += referencePointManager.getName(referencePoint);

      if (!session.isCompletelyShared(referencePoint)) description += " (partial)";

      if (i < numOfReferencePoints - 1) description += ", ";

      i++;
    }

    if (numOfReferencePoints == 0) {
      description += "none";
    }

    return description;
  }
}
