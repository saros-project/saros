package de.fu_berlin.inf.dpp.net.business;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.internal.ActivitiesExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;

/**
 * This class is responsible for parsing RequestForActivities and sending the
 * requested activityDataObjects back.
 * 
 */
@Component(module = "net")
public class RequestForActivityHandler {

    private static final Logger log = Logger
        .getLogger(RequestForActivityHandler.class.getName());

    @Inject
    protected XMPPTransmitter transmitter;

    @Inject
    protected ActivitiesExtensionProvider activitiesProvider;

    protected SessionIDObservable sessionID;

    protected SarosSessionManager sessionManager;

    protected Handler handler;

    public RequestForActivityHandler(SarosSessionManager sessionManager,
        XMPPReceiver receiver, SessionIDObservable sessionID) {

        this.sessionID = sessionID;
        this.sessionManager = sessionManager;
        this.handler = new Handler(sessionID);

        receiver.addPacketListener(handler, handler.getFilter());
    }

    protected class Handler extends RequestActivityExtension {

        public Handler(SessionIDObservable sessionID) {
            super(sessionID);
        }

        @Override
        public PacketFilter getFilter() {
            return new AndFilter(super.getFilter(),
                PacketExtensionUtils.getSessionIDPacketFilter(sessionID));
        }

        @Override
        public void requestForResendingActivitiesReceived(JID fromJID,
            int sequenceNumber, boolean andUp) {

            ISarosSession sarosSession = sessionManager.getSarosSession();

            if (sarosSession.getUser(fromJID) == null) {
                log.warn("Received Request for activityDataObject from buddy who"
                    + " is not part of our shared project session: " + fromJID);
                return;
            }

            List<TimedActivityDataObject> activities = sarosSession
                .getSequencer().getActivityHistory(fromJID, sequenceNumber,
                    andUp);

            log.info(String.format(
                "Received request for resending of timestamp%s %d%s.",
                andUp ? "s" : "", sequenceNumber, andUp ? " (andup)" : ""));

            if (activities.size() > 0) {
                log.info("I am sending back " + activities.size()
                    + " activityDataObjects.");
                transmitter.sendTimedActivities(fromJID, activities);
            } else {
                log.error("No matching activityDataObjects found");
            }
        }
    }
}