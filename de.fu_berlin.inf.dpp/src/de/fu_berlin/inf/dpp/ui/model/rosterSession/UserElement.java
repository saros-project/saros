package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.awareness.AwarenessInformationCollector;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;
import de.fu_berlin.inf.dpp.ui.util.SWTBoldStyler;

/**
 * Wrapper for {@link UserElement} in use with {@link Viewer Viewers}
 * 
 * @author bkahlert
 */
public class UserElement extends TreeElement {

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected AwarenessInformationCollector awarenessInformationCollector;

    protected User user;
    protected Roster roster;

    /**
     * Holds the children for this user element - in the future there might be
     * multiple elements for each user, showing more details than one
     * TreeElement could hold.
     */
    protected List<AwarenessInformationTreeElement> awarenessInformation = new ArrayList<AwarenessInformationTreeElement>();

    public UserElement(User user, Roster roster) {
        SarosPluginContext.initComponent(this);

        this.user = user;
        this.roster = roster;

        // Comment this check if you want to display awareness information about
        // the local user to himself..
        updateChildren();
    }

    public Object getUser() {
        return this.user;
    }

    public boolean getExpanded() {
        return false;

    }

    protected void updateChildren() {
        this.awarenessInformation.clear();
        // Comment this check if you want to display awareness information about
        // the local user to himself..
        if (!user.isLocal()) {
            this.awarenessInformation.add(new AwarenessInformationTreeElement(
                this.user));
            FollowModeInformationTreeElement followModeIndicator;
            if (awarenessInformationCollector.getFollowedJID(user) != null) {
                followModeIndicator = new FollowModeInformationTreeElement(user);
                this.awarenessInformation.add(followModeIndicator);
            }
        }
    }

    @Override
    public Object[] getChildren() {
        return this.awarenessInformation.toArray();
    }

    @Override
    public boolean hasChildren() {
        return this.awarenessInformation.size() != 0;
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();
        final String read_only = Messages.UserElement_read_only;
        final String following = Messages.UserElement_following;
        final String following_paused = Messages.UserElement_following_paused;
        final String joining = Messages.UserElement_joining;
        final String host = Messages.UserElement_host;

        /*
         * Blank space in the front for the highlighting color square
         */
        styledString.append("    ");

        if (user.isHost()) {
            styledString.append(host, StyledString.COUNTER_STYLER);
        }

        /*
         * Name of user without server-part (if no alias is set), because only
         * users from the same XMPP server can be in a session anyway..
         */
        styledString.append(user.getShortHumanReadableName());

        /*
         * Right level
         */
        if (user.hasReadOnlyAccess()) {
            styledString.append(" " + read_only, StyledString.COUNTER_STYLER);
        }

        /*
         * Follow Mode: Who am I following? If this equals the user element we
         * are looking at, append the follow information to the user. Don't
         * append this info for any other users, because they have a
         * FollowModeTreeElement for this.
         */
        User followee = editorManager.getFollowedUser();
        if (user.equals(followee)) {
            if (awarenessInformationCollector.isActiveEditorShared(user)) {
                styledString.append(" " + following, SWTBoldStyler.STYLER);
            } else {
                styledString.append(" " + following_paused,
                    SWTBoldStyler.STYLER);
            }
        }

        return styledString;
    }

    @Override
    public Image getImage() {
        if (roster != null && !user.isLocal()
            && roster.getPresence(user.getJID().toString()).isAway()) {
            if (user.hasWriteAccess())
                return ImageManager.ICON_BUDDY_SAROS_AWAY;
            else
                return ImageManager.ICON_BUDDY_SAROS_READONLY_AWAY;
        } else {
            if (user.hasWriteAccess())
                return ImageManager.ICON_BUDDY_SAROS;
            else
                return ImageManager.ICON_BUDDY_SAROS_READONLY;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserElement other = (UserElement) obj;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }
}
