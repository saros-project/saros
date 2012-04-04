package de.fu_berlin.inf.dpp.ui.eventhandler;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Checks if the host remains alone after a user left the session. If so, ask if
 * the session should be closed (optionally remember choice for workspace...)
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class HostLeftAloneInSessionHandler {

    private static Logger log = Logger
        .getLogger(HostLeftAloneInSessionHandler.class);

    @Inject
    Saros saros;

    protected final SarosSessionManager sessionManager;
    private ISharedProjectListener projectListener;

    public HostLeftAloneInSessionHandler(SarosSessionManager sManager,
        final ProjectNegotiationObservable processes) {
        sessionManager = sManager;
        projectListener = new AbstractSharedProjectListener() {
            @Override
            public void userLeft(User user) {
                log.debug("sessionManager.userLeft");
                ISarosSession session = sessionManager.getSarosSession();
                if (session.getParticipants().size() == 1) {
                    // only ask to close session if there are no running
                    // negotiation processes because if there are, and the last
                    // user "left", it was because he cancelled an
                    // IncomingProjectNegotiation, and the session will be
                    // closed anyway.
                    if (processes.getProcesses().size() == 0) {
                        handleHostLeftAlone();
                    }
                }
            }
        };

        // register our sharedProjectListener when a session is started..
        sessionManager
            .addSarosSessionListener(new AbstractSarosSessionListener() {
                @Override
                public void sessionEnded(ISarosSession oldSarosSession) {
                    // we need to clear any open notifications because there
                    // might be stuff left, like follow mode notifications,
                    // or "buddy joined" notification in case a buddy joined
                    // the session but aborted the incoming project
                    // negotiation...
                    SarosView.clearNotifications();
                }

                @Override
                public void sessionStarted(ISarosSession newSarosSession) {
                    sessionManager.getSarosSession().addListener(
                        projectListener);
                }
            });
    }

    public void handleHostLeftAlone() {
        String stopSessionPreference = saros.getPreferenceStore().getString(
            PreferenceConstants.STOP_EMPTY_SESSIONS);

        boolean stopSession = true;

        // if user did not save a decision in preferences yet: ask!
        if (!stopSessionPreference.equals("false")
            && !stopSessionPreference.equals("true")) {
            stopSession = Utils.popUpRememberDecisionDialog(
                Messages.HostLeftAloneInSessionDialog_title,
                Messages.HostLeftAloneInSessionDialog_message, saros,
                PreferenceConstants.STOP_EMPTY_SESSIONS);
        } else {
            stopSession = stopSessionPreference.equals("true");
        }

        if (stopSession) {
            CollaborationUtils.leaveSession(sessionManager);
        }
    }
}
