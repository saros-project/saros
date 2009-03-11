package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class RemoveDriverRoleAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(RemoveDriverRoleAction.class.getName());

    private User selectedUser;

    private ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user, boolean replicated) {
            updateEnablemnet();
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

                public void sessionEnded(ISharedProject sharedProject) {
                    sharedProject.removeListener(projectListener);
                }

                public void sessionStarted(ISharedProject sharedProject) {
                    sharedProject.addListener(projectListener);
                }
            });

        updateEnablemnet();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runRemoveDriver();
            }
        });
    }

    public void runRemoveDriver() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        if (selectedUser.isDriver())
            project.setUserRole(selectedUser, UserRole.OBSERVER, false);
        updateEnablemnet();
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
            && project.isHost() && this.selectedUser.isDriver());
        setEnabled(enabled);
    }
}
