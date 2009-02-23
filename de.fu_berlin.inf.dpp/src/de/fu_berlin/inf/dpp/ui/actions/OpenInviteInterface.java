package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class OpenInviteInterface extends Action {

    private static final Logger log = Logger
        .getLogger(OpenInviteInterface.class.getName());

    public OpenInviteInterface() {
        super();
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/invites.png"));
        setToolTipText("Open invitation interface");

        Saros.getDefault().getSessionManager().addSessionListener(
            new ISessionListener() {

                public void sessionStarted(ISharedProject session) {
                    setEnabled(session.isHost());
                }

                public void sessionEnded(ISharedProject session) {
                    setEnabled(false);
                }

                public void invitationReceived(
                    IIncomingInvitationProcess process) {
                    // ignore
                }
            });

        // Needed when the Interface is created during a session
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        setEnabled((project != null) && project.isHost());
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                Saros.getDefault().getSessionManager().getSharedProject()
                    .startInvitation(null);
            }
        });
    }

}
