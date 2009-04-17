package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

public class RemoveDriverRoleAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(RemoveDriverRoleAction.class.getName());

    protected User selectedUser;

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user, boolean replicated) {
            updateEnablement();
        }
    };

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionStarted(ISharedProject sharedProject) {
            sharedProject.addListener(projectListener);
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISharedProject sharedProject) {
            sharedProject.removeListener(projectListener);
        }
    };

    @Inject
    protected SessionManager sessionManager;

    public RemoveDriverRoleAction(ISelectionProvider provider) {
        super(provider, "Remove driver role");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user.png"));
        setToolTipText("Remove the driver role from this user.");

        Saros.getDefault().reinject(this);
        sessionManager.addSessionListener(sessionListener);
        updateEnablement();
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
        if (selectedUser.isDriver()) {
            sessionManager.getSharedProject().setUserRole(selectedUser,
                UserRole.OBSERVER, false);
        } else {
            log.warn("User is no driver: " + selectedUser);
        }
        updateEnablement();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
            .getFirstElement() : null;
        updateEnablement();
    }

    protected void updateEnablement() {
        ISharedProject project = sessionManager.getSharedProject();

        boolean enabled = ((project != null) && (this.selectedUser != null)
            && project.isHost() && this.selectedUser.isDriver());
        setEnabled(enabled);
    }
}
