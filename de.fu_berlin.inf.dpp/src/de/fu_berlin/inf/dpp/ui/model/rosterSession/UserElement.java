package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;

/**
 * Wrapper for {@link UserElement} in use with {@link Viewer Viewers}
 * 
 * @author bkahlert
 */
public class UserElement extends TreeElement {
    @Inject
    protected EditorManager editorManager;

    protected User user;
    protected Roster roster;

    public UserElement(User user, Roster roster) {
        SarosPluginContext.initComponent(this);

        this.user = user;
        this.roster = roster;
    }

    public Object getUser() {
        return this.user;
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();

        /*
         * Name
         */
        styledString.append(user.getHumanReadableName());

        /*
         * Right level
         */
        if (user.hasReadOnlyAccess()) {
            styledString.append(" (read-only)", StyledString.COUNTER_STYLER);
        }

        /*
         * Other
         */
        if (user.equals(editorManager.getFollowedUser())) {
            styledString.append(" (following)", StyledString.QUALIFIER_STYLER);
        }

        if (!user.isInvitationComplete()) {
            styledString.append(" (joining...)", StyledString.COUNTER_STYLER);
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
