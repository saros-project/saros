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
public class RemoveDriverRoleAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(RemoveDriverRoleAction.class.getName());

    protected User selectedUser;

    @Inject
    protected SarosUI sarosUI;

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void roleChanged(User user) {
            updateEnablement();
        }
    };

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            newSarosSession.addListener(projectListener);
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(projectListener);
        }
    };

    protected SarosSessionManager sessionManager;

    public RemoveDriverRoleAction(SarosSessionManager sessionManager,
        ISelectionProvider provider) {
        super(provider, "Remove driver role");
        this.sessionManager = sessionManager;

        setImageDescriptor(SarosUI.getImageDescriptor("icons/user.png"));
        setToolTipText("Remove the driver role from this user.");

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
                runRemoveDriver();
            }
        });
    }

    public void runRemoveDriver() {
        if (selectedUser.isDriver()) {
            sarosUI.performRoleChange(selectedUser, UserRole.OBSERVER);
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
        ISarosSession sarosSession = sessionManager.getSarosSession();

        boolean enabled = ((sarosSession != null)
            && (this.selectedUser != null) && sarosSession.isHost() && this.selectedUser
            .isDriver());
        setEnabled(enabled);
    }
}
