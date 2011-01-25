package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
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
public class RestrictToReadOnlyAccessAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(RestrictToReadOnlyAccessAction.class.getName());

    protected User selectedUser;

    @Inject
    protected SarosUI sarosUI;

    protected ISharedProjectListener projectListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(User user) {
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

    public RestrictToReadOnlyAccessAction(SarosSessionManager sessionManager,
        ISelectionProvider provider) {
        super(provider, "Restrict to read-only access");
        this.sessionManager = sessionManager;

        setImageDescriptor(SarosUI
            .getImageDescriptor("icons/elcl16/restricttoreadonlyaccess.png"));

        setToolTipText("Restrict To Read-Only Access");

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
                runRestriction();
            }
        });
    }

    public void runRestriction() {
        if (selectedUser.hasWriteAccess()) {
            sarosUI.performPermissionChange(selectedUser,
                Permission.READONLY_ACCESS);
        } else {
            log.warn("Buddy is has no write access: " + selectedUser);
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
            .hasWriteAccess());
        setEnabled(enabled);
    }
}
