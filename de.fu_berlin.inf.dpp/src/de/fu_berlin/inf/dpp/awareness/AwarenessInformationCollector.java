package de.fu_berlin.inf.dpp.awareness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditor;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

/**
 * Singleton that provides methods to collect and retrieve awareness information
 * for session participants (who is following who, which file is currently
 * opened, etc.)
 * 
 * All methods provided by the interface are <b>not</b> thread safe.
 * 
 * @author waldmann
 */
public class AwarenessInformationCollector {

    private static final Logger LOG = Logger
        .getLogger(AwarenessInformationCollector.class);

    private final EditorManager editorManager;
    private final ISarosSessionManager sessionManager;

    /**
     * Who is following who in the session?
     */
    private final Map<User, User> followModes = new ConcurrentHashMap<User, User>();

    public AwarenessInformationCollector(ISarosSessionManager sessionManager,
        final EditorManager editorManager) {

        this.sessionManager = sessionManager;
        this.editorManager = editorManager;
    }

    /**
     * Retrieve information about the progress of the invitation (if there is
     * any) and awareness information that benefits the users (like showing
     * which file the user is currently viewing, who he is following etc.)
     * 
     * TODO waldmann: move display "logic" to appropriate places? (e.g.
     * AwarenessTreeElement)
     */
    public List<String> getAwarenessDetails(User user) {
        List<String> details = new ArrayList<String>();

        final RemoteEditorManager rem = editorManager.getRemoteEditorManager();

        if (rem == null)
            return details;

        final RemoteEditor activeEditor = rem.getRemoteActiveEditor(user);
        /*
         * The other user has a non-shared editor open, i.e. the remote editor
         * shows a file which is not part of the session.
         */
        if (activeEditor == null) {
            details.add("non-shared file open");
            return details;
        }

        SPath activeFile = activeEditor.getPath();
        if (activeFile != null) {
            /*
             * path.getProjectRelativePath() could be too long, sometimes the
             * name would be enough...
             * 
             * TODO: make this configurable?
             */
            details.add(activeFile.getProject().getName() + ": "
                + activeFile.getFile().getProjectRelativePath().toString());
        }

        return details;
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

        LOG.debug("Remembering that User " + user + " is now following "
            + target);

        // forget any old states, in case there are any..
        followModes.remove(user);

        // remember which user he/she is following
        if (target != null) { // don't save null, this is not necessary
            followModes.put(user, target);
        }
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

        User followee = followModes.get(user);

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
}