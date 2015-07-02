package de.fu_berlin.inf.dpp.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.AbstractSharedEditorListener;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;

/**
 * Action to enter into FollowMode via toolbar.
 * 
 */
public class FollowModeAction extends Action implements IMenuCreator,
    Disposable {

    public static final String ACTION_ID = FollowModeAction.class.getName();

    private static final Logger LOG = Logger.getLogger(FollowModeAction.class);

    private ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    private ISharedProjectListener userListener = new AbstractSharedProjectListener() {
        @Override
        public void userLeft(final User user) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    currentRemoteSessionUsers.remove(user);

                    if (user.equals(currentlyFollowedUser)) {
                        currentlyFollowedUser = null;
                        updateEnablement();
                    }
                }
            });
        }

        @Override
        public void userJoined(final User user) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    currentRemoteSessionUsers.add(user);
                    updateEnablement();

                }
            });
        }
    };

    private ISarosSessionListener sessionListener = new NullSarosSessionListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {

            session.addListener(userListener);

            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    FollowModeAction.this.session = session;
                    currentRemoteSessionUsers.clear();
                    currentRemoteSessionUsers.addAll(session.getRemoteUsers());
                    updateEnablement();
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(userListener);
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    FollowModeAction.this.session = null;
                    currentRemoteSessionUsers.clear();
                    updateEnablement();
                }
            });
        }
    };

    private ISharedEditorListener editorListener = new AbstractSharedEditorListener() {
        @Override
        public void followModeChanged(final User user, final boolean isFollowed) {
            SWTUtils.runSafeSWTAsync(LOG, new Runnable() {

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

    private ISarosSession session;

    private User currentlyFollowedUser;

    private Menu followUserMenu;

    private final Set<User> currentRemoteSessionUsers = new LinkedHashSet<User>();

    private final ImageDescriptor followModeEnabledImageDescriptor = new ImageDescriptor() {
        @Override
        public ImageData getImageData() {
            return ImageManager.ICON_USER_SAROS_FOLLOWMODE.getImageData();
        }
    };

    private final ImageDescriptor followModeDisabledImageDescriptor = new ImageDescriptor() {
        @Override
        public ImageData getImageData() {
            return ImageManager.ICON_USER_SAROS_FOLLOWMODE_DISABLED
                .getImageData();
        }
    };

    public FollowModeAction() {

        SarosPluginContext.initComponent(this);

        setId(ACTION_ID);
        setText(Messages.FollowModeAction_enter_followmode);
        setMenuCreator(this);

        session = sessionManager.getSarosSession();

        sessionManager.addSarosSessionListener(sessionListener);
        editorManager.addSharedEditorListener(editorListener);
        currentlyFollowedUser = editorManager.getFollowedUser();

        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);

        if (session != null)
            currentRemoteSessionUsers.addAll(session.getRemoteUsers());

        updateEnablement();
    }

    @Override
    public void run() {

        if (session == null)
            return;

        if (!isEnabled())
            return;

        followUser(getNextUserToFollow());
    }

    @Override
    public void dispose() {
        if (followUserMenu != null)
            followUserMenu.dispose();

        sessionManager.removeSarosSessionListener(sessionListener);
        editorManager.removeSharedEditorListener(editorListener);
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
    }

    @Override
    public Menu getMenu(Control parent) {
        if (followUserMenu != null)
            followUserMenu.dispose();

        followUserMenu = null;

        if (session == null)
            return null;

        followUserMenu = new Menu(parent);

        List<User> users = new ArrayList<User>(currentRemoteSessionUsers);

        for (User user : users)
            addActionToMenu(followUserMenu, createAction(user));

        new MenuItem(followUserMenu, SWT.SEPARATOR);

        Action disableFollowModeAction = new Action(
            Messages.FollowModeAction_leave_followmode) {
            @Override
            public void run() {
                followUser(null);
            }
        };

        disableFollowModeAction
            .setImageDescriptor(followModeDisabledImageDescriptor);

        addActionToMenu(followUserMenu, disableFollowModeAction);
        return followUserMenu;
    }

    @Override
    public Menu getMenu(Menu parent) {
        return null;
    }

    private void followUser(User user) {
        currentlyFollowedUser = user;
        editorManager.setFollowing(currentlyFollowedUser);
    }

    private Action createAction(final User user) {
        // The additional @ is needed because @ has special meaning in
        // Action#setText(), see JavaDoc of Action().

        String followUserMessage = getFollowUserMessage(user);

        if (followUserMessage.contains("@"))
            followUserMessage += "@";

        Action action = new Action(followUserMessage) {

            @Override
            public void run() {
                followUser(user);
            }
        };

        action.setImageDescriptor(followModeEnabledImageDescriptor);
        return action;
    }

    private void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    /**
     * Returns the next user to follow or <code>null</code> if follow mode
     * should be disabled.
     */
    private User getNextUserToFollow() {
        if (currentRemoteSessionUsers.isEmpty())
            return null;

        if (currentlyFollowedUser == null)
            return currentRemoteSessionUsers.iterator().next();

        User nextUser = null;

        for (Iterator<User> it = currentRemoteSessionUsers.iterator(); it
            .hasNext();) {
            User user = it.next();
            if (user.equals(currentlyFollowedUser)) {
                if (it.hasNext())
                    nextUser = it.next();

                break;
            }
        }

        return nextUser;
    }

    private void updateEnablement() {
        setEnabled(session != null && !currentRemoteSessionUsers.isEmpty());
        setImageDescriptor(currentlyFollowedUser != null ? followModeEnabledImageDescriptor
            : followModeDisabledImageDescriptor);

        if (isEnabled()) {
            User nextUserToFollow = getNextUserToFollow();
            setToolTipText(nextUserToFollow == null ? Messages.FollowModeAction_leave_followmode
                : getFollowUserMessage(nextUserToFollow));
        } else
            // display default text
            setToolTipText(null);
    }

    private String getFollowUserMessage(User user) {
        return ModelFormatUtils.format(Messages.FollowModeAction_follow_user,
            user);
    }
}
