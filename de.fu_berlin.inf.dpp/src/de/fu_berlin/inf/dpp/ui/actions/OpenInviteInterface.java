package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Rename to OpenInvitationDialogAction
 */
@Component(module = "action")
public class OpenInviteInterface extends Action {

    private static final Logger log = Logger
        .getLogger(OpenInviteInterface.class.getName());

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            setEnabled(newSarosSession.isHost());
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            setEnabled(false);
        }
    };

    protected SessionManager sessionManager;

    public OpenInviteInterface(SessionManager sessionManager) {
        super();
        this.sessionManager = sessionManager;

        setImageDescriptor(SarosUI.getImageDescriptor("/icons/invites.png"));
        setToolTipText("Open invitation interface");

        sessionManager.addSessionListener(sessionListener);

        // Needed when the Interface is created during a session
        ISarosSession sarosSession = sessionManager.getSarosSession();
        setEnabled((sarosSession != null) && sarosSession.isHost());
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                sessionManager.openInviteDialog(null);
            }
        });
    }

}
