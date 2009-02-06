package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class OpenInviteInterface extends Action implements
    ISharedProjectListener, ISessionListener {

    public OpenInviteInterface() {
        super();
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/invites.png"));
        setToolTipText("Open invitation interface");

        Saros.getDefault().getSessionManager().addSessionListener(this);
        updateEnablement();
    }

    @Override
    public void run() {
        Saros.getDefault().getSessionManager().getSharedProject()
            .startInvitation(null);
    }

    public void sessionStarted(ISharedProject session) {
        session.addListener(this);
        updateEnablement();
    }

    public void sessionEnded(ISharedProject session) {
        session.removeListener(this);
        updateEnablement();
    }

    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    private void updateEnablement() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        setEnabled((project != null) && project.isHost());
    }

    public void driverChanged(JID driver, boolean replicated) {
        updateEnablement();
    }

    public void userJoined(JID user) {
        // ignore
    }

    public void userLeft(JID user) {
        // ignore
    }
}
