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

public class GiveExclusiveDriverRoleAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(GiveExclusiveDriverRoleAction.class.getName());

    protected User selectedUser;

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user, boolean replicated) {
            updateEnablemnet();
        }
    };

    @Inject
    protected SessionManager sessionManager;

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionEnded(ISharedProject sharedProject) {
            sharedProject.removeListener(projectListener);
        }

        @Override
        public void sessionStarted(ISharedProject sharedProject) {
            sharedProject.addListener(projectListener);
        }
    };

    public GiveExclusiveDriverRoleAction(ISelectionProvider provider,
        String text) {
        super(provider, text);
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Give the exclusive driver role to this user");

        Saros.getDefault().reinject(this);

        sessionManager.addSessionListener(sessionListener);

        updateEnablemnet();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runGiveExclusiveDriver();
            }
        });
    }

    public void runGiveExclusiveDriver() {

        ISharedProject project = sessionManager.getSharedProject();

        // set all participants other than the selected to observer
        for (User user : project.getParticipants()) {
            if ((user.isDriver() && !user.equals(this.selectedUser))) {
                project.setUserRole(user, UserRole.OBSERVER, false);
            }
        }

        // if selected user is not already driver give him driver role
        if (this.selectedUser.isObserver())
            project.setUserRole(this.selectedUser, UserRole.DRIVER, false);
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
            .getFirstElement() : null;
        updateEnablemnet();
    }

    private void updateEnablemnet() {
        ISharedProject project = sessionManager.getSharedProject();

        // Only the host can use this action
        boolean enabled = project != null && project.isHost();

        // Only enable if the user is observer or there are more than one driver
        enabled = enabled && (this.selectedUser != null);
        enabled = enabled
            && (this.selectedUser.isObserver() || !project.isExclusiveDriver());

        setEnabled(enabled);
    }
}