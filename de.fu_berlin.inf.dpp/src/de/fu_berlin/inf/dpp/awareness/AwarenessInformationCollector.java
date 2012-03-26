package de.fu_berlin.inf.dpp.awareness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditor;
import de.fu_berlin.inf.dpp.editor.RemoteEditorManager.RemoteEditorState;
import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * Singleton that provides methods to collect and retrieve awareness information
 * for session participants (who is following who, which file is currently
 * opened, etc.)
 * 
 * All methods provided by the interface are <b>not</b> thread safe.
 * 
 * @author waldmann
 */
@Component(module = "observables")
public class AwarenessInformationCollector {
    private static final Logger log = Logger
        .getLogger(AwarenessInformationCollector.class);

    protected EditorManager editorManager;
    protected ProjectNegotiationObservable projectNegotiationObservable;
    protected SarosSessionObservable sarosSession;

    /**
     * Who is following who in the session?
     */
    protected Map<JID, JID> followModes = new ConcurrentHashMap<JID, JID>();

    public AwarenessInformationCollector(SarosSessionObservable sarosSession,
        ProjectNegotiationObservable projectNegotiationObservable,
        EditorManager editorManager) {

        this.sarosSession = sarosSession;
        this.projectNegotiationObservable = projectNegotiationObservable;
        this.editorManager = editorManager;
    }

    /**
     * Returns a dash-separated string describing the current user state
     * 
     * @param user
     * @return
     */
    public String getAwarenessDetailString(User user) {
        List<String> details = getAwarenessDetails(user);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String detail : details) {
            sb.append((first ? "" : " - ") + detail);
            first = false;
        }
        return sb.toString();
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

        // Differentiate between "invitation in progress" and awareness
        // information shown while the session is running
        if (!user.isInvitationComplete()) {
            details.add("Joining");
        }
        ProjectNegotiation negotiation = projectNegotiationObservable
            .getProjectExchangeProcess(user.getJID());

        if (negotiation != null
            && negotiation instanceof OutgoingProjectNegotiation) {
            /*
             * a negotiation is still running, i.e. the user is not 100% ready
             * to work yet
             */
            details.add("in session synchronization");
        } else {
            RemoteEditorManager rem = editorManager.getRemoteEditorManager();
            if (rem != null) {
                RemoteEditorState res = editorManager.getRemoteEditorManager()
                    .getEditorState(user);
                if (res != null) {
                    RemoteEditor activeEditor = res.getActiveEditor();
                    if (activeEditor != null) {
                        SPath activeFile = res.getActiveEditor().getPath();
                        if (activeFile != null) {
                            /*
                             * path.getProjectRelativePath() could be too long,
                             * sometimes the name would be enough...
                             * 
                             * TODO: make this configurable?
                             */
                            details.add(activeFile.getProject().getName()
                                + ": "
                                + activeFile.getFile().getProjectRelativePath()
                                    .toString());
                        }
                    }
                    if (editorManager.getRemoteOpenEditors(user).size() == 0) {
                        details.add("No shared files opened");
                    }
                }
            }
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

        log.debug("Remembering that User " + user + " is now following "
            + target);

        // forget any old states, in case there are any..
        followModes.remove(user.getJID());

        // remember which user he/she is following
        if (target != null) { // don't save null, this is not necessary
            followModes.put(user.getJID(), target.getJID());
        }
    }

    /**
     * Returns the JID of the user that the given user is following, or null if
     * that user does not follow anyone at the moment, or there is no active
     * session.
     * 
     * @param user
     * @return
     */
    public JID getFollowedUser(User user) {
        assert user != null;

        ISarosSession session = sarosSession.getValue();
        // should not be called outside of a running session
        if (session == null)
            return null;
        JID resJID = session.getResourceQualifiedJID(user.getJID());
        return resJID == null ? resJID : followModes.get(resJID);
    }
}