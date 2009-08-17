package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

@Component(module = "action")
public class GiveExclusiveDriverRoleAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(GiveExclusiveDriverRoleAction.class.getName());

    protected User selectedUser;

    @Inject
    protected SarosUI sarosUI;

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user) {
            updateEnablement();
        }
    };

    protected SessionManager sessionManager;

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionEnded(ISharedProject sharedProject) {
            sharedProject.removeListener(projectListener);
            updateEnablement();
        }

        @Override
        public void sessionStarted(ISharedProject sharedProject) {
            sharedProject.addListener(projectListener);
            updateEnablement();
        }
    };

    public GiveExclusiveDriverRoleAction(SessionManager sessionManager,
        ISelectionProvider provider) {
        super(provider, "Give exclusive driver role");

        this.sessionManager = sessionManager;
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Give the exclusive driver role to this user");

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
                runGiveExclusiveDriver();
            }
        });
    }

    public void runGiveExclusiveDriver() {

        ISharedProject project = sessionManager.getSharedProject();

        // set all participants other than the selected to observer
        for (User user : project.getParticipants()) {
            if ((user.isDriver() && !user.equals(this.selectedUser))) {
                sarosUI.performRoleChange(user, UserRole.OBSERVER);
            }
        }

        // if selected user is not already driver give him driver role
        if (this.selectedUser.isObserver())
            sarosUI.performRoleChange(this.selectedUser, UserRole.DRIVER);
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
            .getFirstElement() : null;
        updateEnablement();
    }

    protected void updateEnablement() {
        setEnabled(shouldBeEnabled());
    }

    protected boolean shouldBeEnabled() {

        // Nobody selected
        if (this.selectedUser == null)
            return false;

        // Not in a shared project
        ISharedProject project = sessionManager.getSharedProject();
        if (project == null)
            return false;

        // Only the host can use this action
        if (!project.isHost())
            return false;

        // Only enable if the user is observer or there is more than one driver
        return this.selectedUser.isObserver() || !project.isExclusiveDriver();
    }
}