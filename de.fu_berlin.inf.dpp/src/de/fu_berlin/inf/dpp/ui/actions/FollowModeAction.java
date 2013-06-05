package de.fu_berlin.inf.dpp.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Action to enter into FollowMode
 * 
 * TODO Rename to GlobalFollowModeAction
 */
@Component(module = "action")
public class FollowModeAction extends Action implements Disposable {

    public static final String ACTION_ID = FollowModeAction.class.getName();

    private static final Logger log = Logger.getLogger(FollowModeAction.class
        .getName());

    private ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    private ISharedProjectListener userListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(final User user) {
            SWTUtils.runSafeSWTAsync(log, new Runnable() {

                @Override
                public void run() {
                    if (user.equals(currentlyFollowedUser)) {
                        currentlyFollowedUser = null;
                        updateEnablement();
                    }
                }
            });
        }

        @Override
        public void userJoined(User user) {
            updateEnablementSWTAsync();
        }
    };

    private ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {

            session.addListener(userListener);

            SWTUtils.runSafeSWTAsync(log, new Runnable() {

                @Override
                public void run() {

                    FollowModeAction.this.session = session;

                    if (preferenceStore
                        .getBoolean(PreferenceConstants.AUTO_FOLLOW_MODE))
                        startAutoFollowHost();

                    updateEnablement();
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(userListener);
            SWTUtils.runSafeSWTAsync(log, new Runnable() {

                @Override
                public void run() {
                    FollowModeAction.this.session = null;
                    updateEnablement();
                }
            });
        }
    };

    private ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(final User user, final boolean isFollowed) {
            SWTUtils.runSafeSWTAsync(log, new Runnable() {

                @Override
                public void run() {
                    currentlyFollowedUser = user;

                    if (!isFollowed)
                        currentlyFollowedUser = null;

                    updateEnablement();
                }
            });
        }
    };

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private EditorManager editorManager;

    @Inject
    private IPreferenceStore preferenceStore;

    private ISarosSession session;

    private User currentlyFollowedUser;

    public FollowModeAction() {
        super(null, AS_CHECK_BOX);

        SarosPluginContext.initComponent(this);
        setImageDescriptor(new ImageDescriptor() {
            @Override
            public ImageData getImageData() {
                return ImageManager.ICON_BUDDY_SAROS_FOLLOWMODE.getImageData();
            }
        });

        setToolTipText(Messages.FollowModeAction_enable_disable_tooltip);
        setId(ACTION_ID);

        session = sessionManager.getSarosSession();

        sessionManager.addSarosSessionListener(sessionListener);
        editorManager.addSharedEditorListener(editorListener);

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);

        updateEnablement();
    }

    @Override
    public void run() {

        if (session == null)
            return;

        if (!isEnabled())
            return;

        if (currentlyFollowedUser != null) {
            currentlyFollowedUser = null;
            editorManager.setFollowing(null);
            return;
        }

        User selectedUser;

        List<User> remoteUsers = session.getRemoteUsers();

        if (remoteUsers.size() == 1)
            selectedUser = remoteUsers.get(0);
        else
            selectedUser = getSelectedUser();

        if (selectedUser == null || selectedUser.equals(session.getLocalUser())
            || !session.getRemoteUsers().contains(selectedUser))
            return;

        currentlyFollowedUser = selectedUser;

        editorManager.setFollowing(currentlyFollowedUser);
    }

    @Override
    public void dispose() {
        sessionManager.removeSarosSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }

    private void updateEnablement() {

        User selectedUser = getSelectedUser();

        int remoteUserCount = 0;

        List<User> remoteUsers = new ArrayList<User>();

        if (session != null)
            remoteUsers = session.getRemoteUsers();

        remoteUserCount = remoteUsers.size();

        if (session == null || remoteUserCount == 0) {
            disableAction();
            return;
        }

        setChecked(currentlyFollowedUser != null);

        if (isChecked()) {
            setToolTipText(MessageFormat.format(
                Messages.FollowModeAction_disable_followmode_tooltip,
                currentlyFollowedUser.getShortHumanReadableName()));
            return;
        }

        if (selectedUser != null && selectedUser.equals(session.getLocalUser())
            && remoteUserCount != 1) {
            disableAction();
            return;
        }

        if (selectedUser == null && remoteUserCount != 1) {
            disableAction();
            return;
        }

        if (selectedUser == null || remoteUserCount == 1)
            selectedUser = remoteUsers.get(0);

        setToolTipText(MessageFormat.format(
            Messages.FollowModeAction_enable_followmode_tooltip,
            selectedUser.getShortHumanReadableName()));

        setEnabled(true);
    }

    private void disableAction() {
        setChecked(false);
        setEnabled(false);
        currentlyFollowedUser = null;
        setToolTipText(Messages.FollowModeAction_enable_disable_tooltip);
    }

    private void updateEnablementSWTAsync() {
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                updateEnablement();
            }
        });
    }

    private User getSelectedUser() {
        List<User> users = SelectionRetrieverFactory.getSelectionRetriever(
            User.class).getSelection();

        if (users.isEmpty() || users.size() > 1)
            return null;

        return users.get(0);
    }

    private void startAutoFollowHost() {

        if (session == null || session.isHost())
            return;

        /*
         * TODO Running this action too early might cause warnings if the
         * viewport information have not yet arrived!
         * 
         * In the worst case, this might be called before the EditorManager has
         * been initialized, which would probably cause undefined behavior.
         * 
         * Suggested Solution: 1.) We should make sure that ISessionListeners
         * are sorted in a sane order.
         * 
         * 2.) We should think about making initial state information part of
         * the Invitation process.
         * 
         * As a HACK, we run this action 1s after the listener was called.
         */

        Display display = SWTUtils.getDisplay();

        if (display.isDisposed())
            return;

        display.timerExec(1000, Utils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                editorManager.setFollowing(session.getHost());
            }
        }));
    }
}
