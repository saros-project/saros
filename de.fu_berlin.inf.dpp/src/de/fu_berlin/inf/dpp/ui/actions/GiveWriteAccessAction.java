package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
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
import de.fu_berlin.inf.dpp.util.Utils;

@Component(module = "action")
public class GiveWriteAccessAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(GiveWriteAccessAction.class.getName());

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
            updateEnablement();
        }

    };

    @Inject
    protected SarosSessionManager sessionManager;

    public GiveWriteAccessAction(ISelectionProvider provider) {
        super(provider, "Grant write access");
        setImageDescriptor(SarosUI
            .getImageDescriptor("icons/elcl16/grantwriteaccess.png"));
        setToolTipText("Grant write access to this buddy");

        Saros.injectDependenciesOnly(this);
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
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                sarosUI.performPermissionChange(
                    GiveWriteAccessAction.this.selectedUser,
                    Permission.WRITE_ACCESS);
            }
        });
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
            .getFirstElement() : null;
        updateEnablement();
    }

    protected void updateEnablement() {
        ISarosSession project = sessionManager.getSarosSession();

        boolean enabled = ((project != null) && (this.selectedUser != null)
            && project.isHost() && this.selectedUser.hasReadOnlyAccess());
        setEnabled(enabled);
    }
}
