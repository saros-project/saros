package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class OpenInviteInterface extends Action {

    private static final Logger log = Logger
        .getLogger(OpenInviteInterface.class.getName());

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionStarted(ISharedProject sharedProject) {
            setEnabled(sharedProject.isHost());
        }

        @Override
        public void sessionEnded(ISharedProject sharedProject) {
            setEnabled(false);
        }
    };

    @Inject
    protected SessionManager sessionManager;

    public OpenInviteInterface() {
        super();
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/invites.png"));
        setToolTipText("Open invitation interface");

        Saros.getDefault().reinject(this);
        sessionManager.addSessionListener(sessionListener);

        // Needed when the Interface is created during a session
        ISharedProject project = sessionManager.getSharedProject();
        setEnabled((project != null) && project.isHost());
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                sessionManager.getSharedProject().startInvitation(null);
            }
        });
    }

}
