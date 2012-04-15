package de.fu_berlin.inf.dpp.ui.model.rosterSession;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.ui.ImageManager;

/**
 * This is a tree element that can be displayed as a child element of the user
 * entry in the Saros session view {@link Viewer Viewers} showing information
 * about the state of that user / his past actions or whatever awareness
 * information migh help to be more productive in a session.
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class FollowModeInformationTreeElement extends
    AwarenessInformationTreeElement {
    private static final Logger log = Logger
        .getLogger(FollowModeInformationTreeElement.class);

    @Inject
    protected SarosSessionObservable sarosSession;

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

        JID followTarget = awarenessInformationCollector.getFollowedUser(user);
        if (followTarget != null) {
            // user is following someone: show it
            followTarget = sarosSession.getValue().getResourceQualifiedJID(
                followTarget);
            if (followTarget != null) {
                User followTargetUser = sarosSession.getValue().getUser(
                    followTarget);
                styledString.append("Following "
                    + followTargetUser.getShortHumanReadableName());
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
        return ImageManager.ICON_BUDDY_SAROS_FOLLOWMODE;
    }

    @Override
    public Object getUser() {
        return this.user;
    }
}
