package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class OpenInviteInterface extends Action implements ISessionListener {

    public OpenInviteInterface() {
        super();
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/invites.png"));
        setToolTipText("Open invitation interface");

        Saros.getDefault().getSessionManager().addSessionListener(this);

        // Needed when the Interface is created during a session
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        setEnabled((project != null) && project.isHost());
    }

    @Override
    public void run() {
        Saros.getDefault().getSessionManager().getSharedProject()
            .startInvitation(null);
    }

    public void sessionStarted(ISharedProject session) {
        setEnabled(session.isHost());
    }

    public void sessionEnded(ISharedProject session) {
        setEnabled(false);
    }

    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }
}
