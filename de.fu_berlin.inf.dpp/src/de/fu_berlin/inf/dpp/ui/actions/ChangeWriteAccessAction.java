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
import org.eclipse.ui.PlatformUI;
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
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Base class for changing the write access of a session participant (granting
 * write access, restricting to read-only).
 * <p>
 * TODO Introduce two static methods (one for each Permission) for creating
 * preconfigured objects, instead of having two subclasses. Change
 * {@link SarosView}, so it does not rely on distinct Action classes.
 */
public abstract class ChangeWriteAccessAction extends Action implements
    Disposable {

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

    public ChangeWriteAccessAction(Permission permission, String text,
        String tooltip, final Image icon) {

        super(text);

        SarosPluginContext.initComponent(this);

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
        try {
            List<User> participants = SelectionRetrieverFactory
                .getSelectionRetriever(User.class).getSelection();

            boolean sessionRunning = (sessionManager.getSarosSession() != null);
            boolean selectedOneWithOppositePermission = (participants.size() == 1 && participants
                .get(0).getPermission() != permission);

            setEnabled(sessionRunning && selectedOneWithOppositePermission);
        } catch (NullPointerException e) {
            setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                LOG.error("Unexcepted error while updating enablement", e); //$NON-NLS-1$
        }
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