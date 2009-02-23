package de.fu_berlin.inf.dpp.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class RemoveDriverRoleAction extends SelectionProviderAction {

    private User selectedUser;

    private ISharedProjectListener projectListener = new ISharedProjectListener() {

        public void roleChanged(JID user, boolean replicated) {
            updateEnablemnet();
        }

        public void userJoined(JID user) {
            // ignore
        }

        public void userLeft(JID user) {
            // ignore
        }

    };

    public RemoveDriverRoleAction(ISelectionProvider provider) {
        super(provider, "Remove driver role");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user.png"));
        setToolTipText("Remove the driver role from this user.");

        Saros.getDefault().getSessionManager().addSessionListener(
            new ISessionListener() {

                public void invitationReceived(
                    IIncomingInvitationProcess invitation) {
                    // ignore
                }

                public void sessionEnded(ISharedProject session) {
                    session.removeListener(projectListener);
                }

                public void sessionStarted(ISharedProject session) {
                    session.addListener(projectListener);
                }
            });

        updateEnablemnet();
    }

    @Override
    public void run() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        if (project.isDriver(selectedUser))
            project.toggleUserRole(selectedUser, false);
        updateEnablemnet();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
            .getFirstElement() : null;
    }

    private void updateEnablemnet() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        boolean enabled = ((project != null) && (this.selectedUser != null)
            && project.isHost() && project.isDriver(this.selectedUser));
        setEnabled(enabled);
    }
}
