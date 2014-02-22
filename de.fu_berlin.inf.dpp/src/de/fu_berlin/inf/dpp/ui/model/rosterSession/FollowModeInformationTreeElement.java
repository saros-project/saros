package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;

/**
 * This is a tree element that can be displayed as a child element of the user
 * entry in the Saros session view {@link Viewer Viewers} showing information
 * about the state of that user / his past actions or whatever awareness
 * information might help to be more productive in a session.
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class FollowModeInformationTreeElement extends
    AwarenessInformationTreeElement {
    private static final Logger log = Logger
        .getLogger(FollowModeInformationTreeElement.class);

    public FollowModeInformationTreeElement(User user) {
        super(user);
    }

    /**
     * Combines the available awareness informations to a styled string
     * 
     * TODO: (optional) create a new renderer that presents the information in a
     * more user friendly way, not just text
     */
    @Override
    public StyledString getStyledText() {
        log.debug("FollowModeInformation getStyledText()");

        StyledString styledString = new StyledString();
        final String following_paused = Messages.UserElement_following_paused;

        User followee = awarenessInformationCollector.getFollowedUser(user);
        if (followee != null) {
            if (awarenessInformationCollector.isActiveEditorShared(followee)) {
                styledString.append("following "
                    + followee.getShortHumanReadableName());
            } else {
                styledString.append(following_paused);
            }
        } else {
            styledString.append("Not following anyone");
        }
        return styledString;
    }

    /**
     * Display an appropriate image for this element depending on the awareness
     * information that is currently shown.
     * 
     * At the moment this is only a "file object" icon in case the user has a
     * file opened.
     * 
     * TODO: set icons properly depending on the state of the user/his actions.
     */
    @Override
    public Image getImage() {
        return ImageManager.ICON_USER_SAROS_FOLLOWMODE;
    }

    @Override
    public Object getUser() {
        return this.user;
    }
}
