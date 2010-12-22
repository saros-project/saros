package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
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

    protected SarosSessionManager sessionManager;

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            newSarosSession.addListener(projectListener);
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(projectListener);
            updateEnablement();
        }

    };

    public GiveExclusiveDriverRoleAction(SarosSessionManager sessionManager,
        ISelectionProvider provider) {
        super(provider, "Give exclusive driver role");

        this.sessionManager = sessionManager;
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Give the exclusive driver role to this user");

        /*
         * if SessionView is not "visible" on session start up this constructor
         * will be called after session started (and the user uses this view)
         * That's why the method sessionListener.sessionStarted has to be called
         * manually. If not the sharedProjectListener is not added to the
         * session and the action enablement cannot be updated.
         */
        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }

        sessionManager.addSarosSessionListener(sessionListener);

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

        ISarosSession sarosSession = sessionManager.getSarosSession();

        // set all participants other than the selected to observer
        for (User user : sarosSession.getParticipants()) {
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
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return false;

        // Only the host can use this action
        if (!sarosSession.isHost())
            return false;

        // Only enable if the user is observer or there is more than one driver
        return this.selectedUser.isObserver()
            || !sarosSession.isExclusiveDriver();
    }
}