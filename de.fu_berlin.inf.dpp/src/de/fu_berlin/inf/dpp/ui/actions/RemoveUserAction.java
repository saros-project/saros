package de.fu_berlin.inf.dpp.ui.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.rosterSession.UserElement;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;

public class RemoveUserAction extends Action {

    @Inject
    private ISarosSessionManager sessionManager;

    private volatile ISarosSession session;

    private ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            session = newSarosSession;
            SWTUtils.runSafeSWTAsync(null, new Runnable() {
                @Override
                public void run() {
                    updateEnablement();
                }
            });
        }

        @Override
        public void sessionEnding(ISarosSession oldSarosSession) {
            session = null;
            SWTUtils.runSafeSWTAsync(null, new Runnable() {
                @Override
                public void run() {
                    updateEnablement();
                }
            });
        }
    };

    public RemoveUserAction() {
        super("Remove from Session");
        SarosPluginContext.initComponent(this);

        this.setImageDescriptor(ImageManager
            .getImageDescriptor("icons/elcl16/buddy_remove_tsk.png"));

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);

        sessionManager.addSarosSessionListener(sessionListener);
        session = sessionManager.getSarosSession();

        updateEnablement();
    }

    @Override
    public void run() {

        ISarosSession currentSession = session;

        if (currentSession == null)
            return;

        List<UserElement> participants = SelectionRetrieverFactory
            .getSelectionRetriever(UserElement.class).getSelection();

        if (!canRemoveUsers(participants))
            return;

        for (UserElement e : participants)
            session.kickUser((User) e.getUser());
    }

    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);

        sessionManager.removeSarosSessionListener(sessionListener);

    }

    private void updateEnablement() {
        ISarosSession currentSession = session;

        List<UserElement> participants = SelectionRetrieverFactory
            .getSelectionRetriever(UserElement.class).getSelection();

        setEnabled(currentSession != null && currentSession.isHost()
            && canRemoveUsers(participants));
    }

    private boolean canRemoveUsers(List<UserElement> users) {

        if (users.size() == 0)
            return false;

        for (UserElement e : users) {
            if (((User) e.getUser()).isHost()) {
                return false;
            }
        }
        return true;
    }
}
