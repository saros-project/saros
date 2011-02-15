package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Rename to OpenInvitationDialogAction
 */
@Component(module = "action")
public class OpenInviteInterface extends Action {

    private static final Logger log = Logger
        .getLogger(OpenInviteInterface.class.getName());

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            setEnabled(newSarosSession.isHost());
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            setEnabled(false);
        }
    };

    protected SarosSessionManager sessionManager;

    public OpenInviteInterface(SarosSessionManager sessionManager) {
        super();
        this.sessionManager = sessionManager;

        setImageDescriptor(SarosUI
            .getImageDescriptor("/icons/elcl16/project_share_tsk.png"));
        setToolTipText("Open invitation interface");

        sessionManager.addSarosSessionListener(sessionListener);

        // Needed when the Interface is created during a session
        ISarosSession sarosSession = sessionManager.getSarosSession();
        setEnabled((sarosSession != null) && sarosSession.isHost());
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                sessionManager.openInviteDialog(null);
            }
        });
    }

}
