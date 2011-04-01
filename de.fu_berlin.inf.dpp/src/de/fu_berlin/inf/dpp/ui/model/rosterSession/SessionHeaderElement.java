package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;

/**
 * Container {@link TreeElement}Êfor a {@link SarosSession}
 * 
 * @author bkahlert
 */
public class SessionHeaderElement extends HeaderElement {
    protected RosterSessionInput rosterSessionInput;

    public SessionHeaderElement(Font font, RosterSessionInput rosterSessionInput) {
        super(font);
        this.rosterSessionInput = rosterSessionInput;
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();
        if (rosterSessionInput == null
            || rosterSessionInput.getSarosSession() == null) {
            styledString.append("No Session Running", boldStyler);
        } else {
            styledString.append("Session", boldStyler);
        }
        return styledString;
    }

    @Override
    public Image getImage() {
        return ImageManager.ELCL_PROJECT_SHARE;
    }

    @Override
    public boolean hasChildren() {
        return rosterSessionInput != null
            && rosterSessionInput.getSarosSession() != null;
    }

    @Override
    public Object[] getChildren() {
        if (rosterSessionInput != null
            && rosterSessionInput.getSarosSession() != null) {
            List<UserElement> userElements = new ArrayList<UserElement>();

            Collection<User> users = rosterSessionInput.getSarosSession()
                .getParticipants();
            for (User user : users) {
                UserElement userElement = new UserElement(user,
                    rosterSessionInput.getRoster());
                userElements.add(userElement);
            }

            return userElements.toArray();
        } else {
            return new Object[0];
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
            * result
            + ((rosterSessionInput == null) ? 0 : rosterSessionInput.hashCode());
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
        SessionHeaderElement other = (SessionHeaderElement) obj;
        if (rosterSessionInput == null) {
            if (other.rosterSessionInput != null)
                return false;
        } else if (!rosterSessionInput.equals(other.rosterSessionInput))
            return false;
        return true;
    }
}
