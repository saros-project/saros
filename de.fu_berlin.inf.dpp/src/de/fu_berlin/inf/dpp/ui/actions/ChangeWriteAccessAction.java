package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.User.Permission;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Change the write access of a session participant (granting write access,
 * restricting to read-only).
 */
public class ChangeWriteAccessAction extends Action implements Disposable {

    public static final String ACTION_ID = ChangeWriteAccessAction.class
        .getName();

    private static final Logger LOG = Logger
        .getLogger(ChangeWriteAccessAction.class);

    private Permission permission;

    @Inject
    private SarosUI sarosUI;

    @Inject
    private ISarosSessionManager sessionManager;

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            newSarosSession.addListener(permissionListener);
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(permissionListener);
        }
    };

    private ISharedProjectListener permissionListener = new AbstractSharedProjectListener() {
        @Override
        public void permissionChanged(User user) {
            updateEnablement();
        }
    };

    private ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    public final static ChangeWriteAccessAction forWriteAccess = new ChangeWriteAccessAction(
        Permission.WRITE_ACCESS, Messages.GiveWriteAccessAction_title,
        Messages.GiveWriteAccessAction_tooltip,
        ImageManager.ICON_CONTACT_SAROS_SUPPORT);

    public final static ChangeWriteAccessAction forReadOnly = new ChangeWriteAccessAction(
        Permission.READONLY_ACCESS,
        Messages.RestrictToReadOnlyAccessAction_title,
        Messages.RestrictToReadOnlyAccessAction_tooltip,
        ImageManager.ICON_USER_SAROS_READONLY);

    private ChangeWriteAccessAction(Permission permission, String text,
        String tooltip, final Image icon) {

        super(text);

        SarosPluginContext.initComponent(this);

        setId(ACTION_ID + "." + permission);

        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return icon.getImageData();
            }
        });

        setToolTipText(tooltip);

        this.permission = permission;

        /*
         * if SessionView is not "visible" on session start up this constructor
         * will be called after session started (and the user uses this view)
         * That's why the method sessionListener.sessionStarted has to be called
         * manually. If the permissionListener is not added to the session and
         * the action enablement cannot be updated.
         */
        if (sessionManager.getSarosSession() != null) {
            sessionListener.sessionStarted(sessionManager.getSarosSession());
        }

        sessionManager.addSarosSessionListener(sessionListener);
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    private void updateEnablement() {
        List<User> participants = SelectionRetrieverFactory
            .getSelectionRetriever(User.class).getSelection();

        boolean sessionRunning = (sessionManager.getSarosSession() != null);
        boolean selectedOneWithOppositePermission = (participants.size() == 1 && participants
            .get(0).getPermission() != permission);

        setEnabled(sessionRunning && selectedOneWithOppositePermission);
    }

    @Override
    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
        sessionManager.removeSarosSessionListener(sessionListener);
    }

    @Override
    public void run() {
        ThreadUtils.runSafeSync(LOG, new Runnable() {
            @Override
            public void run() {
                ISarosSession session = sessionManager.getSarosSession();

                if (session == null)
                    return;

                List<User> participants = SelectionRetrieverFactory
                    .getSelectionRetriever(User.class).getSelection();
                if (participants.size() == 1) {
                    User selected = participants.get(0);
                    if (selected.getPermission() != permission) {
                        sarosUI.performPermissionChange(session, selected,
                            permission);
                        updateEnablement();
                    } else {
                        LOG.warn("Did not change write access of " + selected
                            + ", because it's already set.");
                    }
                } else {
                    LOG.warn("More than one participant selected."); //$NON-NLS-1$
                }
            }
        });
    }

}