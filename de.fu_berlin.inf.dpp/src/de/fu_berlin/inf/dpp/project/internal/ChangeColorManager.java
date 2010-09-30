package de.fu_berlin.inf.dpp.project.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.RGB;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.ChangeColorDialog;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This manager is responsible for handling color changes.
 * 
 * @author cnk and tobi
 */
@Component(module = "core")
public class ChangeColorManager implements IActivityProvider {

    private static final Logger log = Logger.getLogger(ChangeColorManager.class
        .getName());

    protected final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();
    protected SessionManager sessionManager;
    protected ISarosSession sarosSession;
    @Inject
    protected EditorManager editorManager;
    @Inject
    protected Saros saros;
    protected RGB rgbOfNewParticipant;

    public ChangeColorManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        sessionManager.addSessionListener(sessionListener);
    }

    public void addActivityListener(IActivityListener listener) {
        this.activityListeners.add(listener);
    }

    public void exec(IActivity activity) {
        if (activity instanceof ChangeColorActivity) {
            ChangeColorActivity changeColorActivity = (ChangeColorActivity) activity;
            User user = changeColorActivity.getSource();
            if (!user.isInSarosSession()) {
                throw new IllegalArgumentException("User " + user
                    + " is not a participant in this shared project");
            }

            log.info("received color: " + changeColorActivity.getColor()
                + " from user: " + user);
            SarosAnnotation.setUserColor(user, changeColorActivity.getColor());

            editorManager.colorChangend();
            editorManager.refreshAnnotations();
        }
    }

    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISarosSession project) {
            project.addActivityProvider(ChangeColorManager.this);
            sarosSession = project;
            sarosSession.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession project) {
            project.removeActivityProvider(ChangeColorManager.this);
            sarosSession = null;
        }
    };

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        @Override
        public void userJoined(User user) {
            User localUser = sarosSession.getLocalUser();
            if (localUser.isHost()) {
                rgbOfNewParticipant = SarosAnnotation.getUserColor(user)
                    .getRGB();
                Map<User, RGB> sessionRGBS = lookUpColorsAndCheck(user);
                sendColors(sessionRGBS, user);
            }
        }

        private Map<User, RGB> lookUpColorsAndCheck(final User newUser) {
            Map<User, RGB> sessionRGBS = new HashMap<User, RGB>();
            boolean done = false;

            while (!done) {
                Collection<User> userList = new Vector<User>();
                userList.addAll(sarosSession.getParticipants());
                userList.remove(newUser);

                if (!ChangeColorManager.checkColor(rgbOfNewParticipant,
                    userList)) {
                    Util.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            ChangeColorDialog changeColor = new ChangeColorDialog(
                                EditorAPI.getShell(), sessionManager
                                    .getSarosSession(),
                                "Color Conflict! Please choose a new color for "
                                    + newUser.getHumanReadableName());
                            rgbOfNewParticipant = changeColor.open();
                        }
                    });
                } else {
                    done = true;
                }
            }

            SarosAnnotation.setUserColor(newUser, rgbOfNewParticipant);

            for (User user : sarosSession.getParticipants()) {
                RGB rgb = SarosAnnotation.getUserColor(user).getRGB();
                sessionRGBS.put(user, rgb);
            }

            return sessionRGBS;
        }

        private void sendColors(Map<User, RGB> sessionRGBS, User newUser) {
            Collection<User> userList = new Vector<User>();
            userList.addAll(sarosSession.getParticipants());
            userList.remove(newUser);

            for (User user : userList) {
                sarosSession.sendActivity(user, new ChangeColorActivity(
                    newUser, user, sessionRGBS.get(newUser)));
            }

            for (User user : sarosSession.getParticipants()) {
                sarosSession.sendActivity(newUser, new ChangeColorActivity(
                    user, newUser, sessionRGBS.get(user)));
            }
        }
    };

    public static boolean checkColor(RGB color, java.util.Collection<User> users) {
        for (User user : users) {
            RGB rgb = SarosAnnotation.getUserColor(user).getRGB();
            int x1, x2, x3;
            double i;
            x1 = rgb.red - color.red;
            x2 = rgb.green - color.green;
            x3 = rgb.blue - color.blue;
            i = x1 * x1 + x2 * x2 + x3 * x3;
            i = Math.sqrt(i);
            if (i < 100)
                return false;
        }
        return true;
    }
}
