package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This follow mode action is used to select the person to follow.
 * 
 * @author Christopher Oezbek
 * @author Edna Rosen
 */
@Component(module = "action")
public class FollowThisPersonAction extends SelectionProviderAction {

    public static final String ACTION_ID = FollowThisPersonAction.class
        .getName();

    private static final Logger log = Logger
        .getLogger(FollowThisPersonAction.class.getName());

    protected User selectedUser;

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            update();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            update();
        }
    };

    protected ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(User user) {
            update();
        }
    };

    protected SarosSessionManager sessionManager;

    protected EditorManager editorManager;

    protected Saros saros;

    public FollowThisPersonAction(ISelectionProvider provider, Saros saros,
        SarosSessionManager sessionManager, EditorManager editorManager) {
        super(provider, "Follow this buddy");

        this.saros = saros;
        this.sessionManager = sessionManager;
        this.editorManager = editorManager;

        setImageDescriptor(SarosUI.getImageDescriptor("/icons/monitor_add.png"));
        setToolTipText("Enable/disable follow mode");
        setId(ACTION_ID);

        sessionManager.addSarosSessionListener(sessionListener);
        editorManager.addSharedEditorListener(editorListener);

        update();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {

                User toFollow;
                if (editorManager.getFollowedUser() == selectedUser) {
                    toFollow = null;
                } else {
                    toFollow = selectedUser;
                }
                log.info("Following: " + toFollow);
                editorManager.setFollowing(toFollow);
            }
        });
    }

    /**
     * Returns <code>true</code> if the follow mode button should be enabled,
     * <code>false</code> otherwise.
     */
    protected boolean canFollow() {
        ISarosSession project = sessionManager.getSarosSession();

        if (project == null || selectedUser == null)
            return false;

        return selectedUser.isRemote();
    }

    protected void update() {

        boolean canFollow = canFollow();

        setEnabled(canFollow);

        if (!canFollow) {
            setText("Follow this buddy");
        } else {
            if (editorManager.getFollowedUser() == selectedUser) {
                setText("Stop following this buddy");
            } else {
                setText("Follow this buddy");
            }
        }
    }

    @Override
    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.selectedUser = (selection.size() == 1) ? (User) selection
            .getFirstElement() : null;
        update();
    }
}
