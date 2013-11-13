package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

@Component(module = "action")
public class GiveWriteAccessAction extends Action implements Disposable {

    private static final Logger log = Logger
        .getLogger(GiveWriteAccessAction.class.getName());

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

    protected ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected ISarosSessionManager sessionManager;

    public GiveWriteAccessAction() {
        super(Messages.GiveWriteAccessAction_title);
        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ICON_CONTACT_SAROS_SUPPORT.getImageData();
            }
        });
        setToolTipText(Messages.GiveWriteAccessAction_tooltip);

        SarosPluginContext.initComponent(this);
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
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                ISarosSession session = sessionManager.getSarosSession();

                if (session == null)
                    return;

                List<User> participants = SelectionRetrieverFactory
                    .getSelectionRetriever(User.class).getSelection();
                if (participants.size() == 1) {
                    if (!participants.get(0).hasWriteAccess()) {
                        sarosUI.performPermissionChange(session,
                            participants.get(0), Permission.WRITE_ACCESS);
                        updateEnablement();
                    } else {
                        log.warn("Participant has already write access: " //$NON-NLS-1$
                            + participants.get(0));
                    }
                } else {
                    log.warn("More than one participant selected."); //$NON-NLS-1$
                }
            }
        });
    }

    protected void updateEnablement() {
        try {
            List<User> participants = SelectionRetrieverFactory
                .getSelectionRetriever(User.class).getSelection();
            setEnabled(sessionManager.getSarosSession() != null
                && participants.size() == 1
                && participants.get(0).hasReadOnlyAccess());
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e); //$NON-NLS-1$
        }
    }

    @Override
    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
        sessionManager.removeSarosSessionListener(sessionListener);
    }
}
