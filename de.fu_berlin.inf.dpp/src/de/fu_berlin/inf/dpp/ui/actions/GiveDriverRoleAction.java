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

public class GiveDriverRoleAction extends SelectionProviderAction {

    protected User selectedUser;

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

    public GiveDriverRoleAction(ISelectionProvider provider, String text) {
        super(provider, text);
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Give the driver role to this user");

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
        project.toggleUserRole(this.selectedUser, false);
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
            .getFirstElement() : null;
        updateEnablemnet();
    }

    private void updateEnablemnet() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        boolean enabled = ((project != null) && (this.selectedUser != null)
            && project.isHost() && !project.isDriver(this.selectedUser));
        setEnabled(enabled);
    }
}
