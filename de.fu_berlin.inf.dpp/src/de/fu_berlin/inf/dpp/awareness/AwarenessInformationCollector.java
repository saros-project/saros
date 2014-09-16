package de.fu_berlin.inf.dpp.awareness;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.fu_berlin.inf.dpp.activities.IDEInteractionActivity.Element;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Singleton that provides methods to collect and retrieve awareness information
 * for session participants (who is following who, which file is currently
 * opened, which dialog was opened or which view was activated).
 * 
 * All methods provided by the interface are <b>not</b> thread safe.
 * 
 * @author waldmann
 */
public class AwarenessInformationCollector {

    private final EditorManager editorManager;
    private final ISarosSessionManager sessionManager;

    /**
     * Who is following who in the session?
     */
    private final Map<User, User> followModes = new ConcurrentHashMap<User, User>();

    /**
     * Stores the title of the activated IDE element (title of dialog or view)
     * activated by the user <code>JID</code>.
     */
    private Map<User, String> activeIDEElement = new HashMap<User, String>();

    /**
     * Stores the type of the activated IDE element ({@link Element}) activated
     * by the user <code>JID</code>.
     */
    private Map<User, Element> activeIDEElementType = new HashMap<User, Element>();

    public AwarenessInformationCollector(ISarosSessionManager sessionManager,
        final EditorManager editorManager) {

        this.sessionManager = sessionManager;
        this.editorManager = editorManager;
    }

    /**
     * Make sure to call this, when a session ends, or when a session starts to
     * avoid having outdated information
     */
    public void flushFollowModes() {
        followModes.clear();
    }

    /**
     * Remember that "user" is following "target" in the currently running
     * session.
     * 
     * @param user
     * @param target
     */
    public void setUserFollowing(User user, User target) {
        assert user != null;
        assert !(user.equals(target));

        followModes.remove(user);

        if (target != null) // null is not allowed in CHM
            followModes.put(user, target);
    }

    /**
     * Returns the followee of the given user, or <code>null</code> if that user
     * does not follow anyone at the moment, or there is no active session.
     * 
     * @param user
     * @return
     */
    public User getFollowedUser(User user) {
        assert user != null;

        final ISarosSession session = sessionManager.getSarosSession();

        // should not be called outside of a running session
        if (session == null)
            return null;

        final User followee = followModes.get(user);

        if (followee == null)
            return null;

        /*
         * FIXME this should not be done here, it should be the responsibility
         * of the class that calls setUserFollowing to correctly clear this map
         * entries !
         */
        return session.getUser(followee.getJID());
    }

    /**
     * Checks if the currently active editor of the given user is shared. The
     * user can be the local or remote one.
     * 
     * @return <code>true</code>, if the active editor of the given user is
     *         shared, <code>false</code> otherwise
     */
    public boolean isActiveEditorShared(User user) {
        boolean editorActive = false;

        RemoteEditorManager rem = editorManager.getRemoteEditorManager();
        if (rem != null && user != null) {
            if (user.isLocal() && editorManager.isActiveEditorShared()
                || rem.isRemoteActiveEditorShared(user)) {
                editorActive = true;
            }
        }
        return editorActive;
    }

    /**
     * Stores the title of the open dialog (e.g. "New Java Class", "Search",
     * etc.) or active view (e.g. "Saros", "Package Explorer" etc.) the given
     * user is currently interacting with.
     * 
     * @param user
     *            The user who created this activity.
     * @param title
     *            The title of the opened dialog or activated view.
     * @param element
     *            The type of IDE element, which can be a dialog or a view (
     *            {@link Element}).
     * */
    public synchronized void updateOpenIDEElement(User user, String title,
        Element element) {
        if (user == null || title == null || title.isEmpty()) {
            // Sometimes, there are empty titles in Eclipse dialogs, but this is
            // very rare.
            return;
        }
        activeIDEElement.put(user, title);
        activeIDEElementType.put(user, element);
    }

    /**
     * Removes the title of the closed dialog (e.g. "New Java Class", "Search",
     * etc.) or deactivated view (e.g. "Saros", "Package Explorer" etc.) the
     * given user has currently stopped to interacting with.
     * 
     * @param user
     *            The user who created this activity.
     * @param element
     *            The type of IDE element, which can be a dialog or a view (
     *            {@link Element}).
     * */
    public synchronized void updateCloseIDEElement(User user, Element element) {
        if (user == null) {
            return;
        }
        activeIDEElement.remove(user);
        activeIDEElementType.remove(user);
    }

    /**
     * Returns the title of the currently open dialog or active view for the
     * given user.
     * 
     * @param user
     *            The user who created this activity.
     * @return The title of the currently open dialog or active view for the
     *         given user or <code>null</code> if <code>user</code> is
     *         <code>null</code>.
     * */
    public synchronized String getOpenIDEElementTitle(User user) {
        if (user == null) {
            return null;
        }
        return activeIDEElement.get(user);
    }

    /**
     * Returns the type of the IDE element ({@link Element}) for the given user,
     * thus if the user interacted with a dialog or a view.
     * 
     * @param user
     *            The user who created this activity.
     * @return The type of the IDE element ({@link Element}) with which the user
     *         interacted, thus a dialog or a view or <code>null</code> if
     *         <code>user</code> is <code>null</code>.
     * */
    public synchronized Element getOpenIDEElementType(User user) {
        if (user == null) {
            return null;
        }
        return activeIDEElementType.get(user);
    }
}
