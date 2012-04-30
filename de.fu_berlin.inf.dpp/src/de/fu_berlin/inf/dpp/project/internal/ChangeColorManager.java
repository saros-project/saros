package de.fu_berlin.inf.dpp.project.internal;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.business.AbstractActivityReceiver;
import de.fu_berlin.inf.dpp.activities.business.ChangeColorActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * This manager is responsible for handling color changes.
 * 
 * @author cnk and tobi
 */
@Component(module = "core")
public class ChangeColorManager extends AbstractActivityProvider {

    private static final Logger log = Logger
        .getLogger(ChangeColorManager.class);

    protected ISarosSessionManager sessionManager;
    protected ISarosSession sarosSession;
    protected EditorManager editorManager;

    protected RGB rgbOfNewParticipant;

    public ChangeColorManager(ISarosSessionManager sessionManager,
        EditorManager editorManager) {
        this.sessionManager = sessionManager;
        this.editorManager = editorManager;
        sessionManager.addSarosSessionListener(sessionListener);
    }

    @Override
    public void exec(IActivity activity) {
        activity.dispatch(receiver);
    }

    protected AbstractActivityReceiver receiver = new AbstractActivityReceiver() {

        @Override
        public void receive(ChangeColorActivity activity) {
            handleChangeColorActivity(activity);
        }
    };

    protected void handleChangeColorActivity(ChangeColorActivity activity) {

        User user = activity.getSource();
        if (!user.isInSarosSession()) {
            throw new IllegalArgumentException(MessageFormat.format(
                Messages.ChangeColorManager_buddy_no_participant, user));
        }

        log.info("received color: " + activity.getColor() + " from buddy: " //$NON-NLS-1$ //$NON-NLS-2$
            + user);
        SarosAnnotation.setUserColor(user, activity.getColor());

        editorManager.colorChanged();
        editorManager.refreshAnnotations();
    }

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession session) {
            sarosSession = session;
            sarosSession.addActivityProvider(ChangeColorManager.this);
            sarosSession.addListener(sharedProjectListener);
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            session.removeActivityProvider(ChangeColorManager.this);
            sarosSession = null;
        }
    };

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {

        @Override
        public void userJoined(User user) {
            log.info("User Joined:" + user.toString());
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
                    Utils.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            ColorDialog changeColor = new ColorDialog(EditorAPI
                                .getShell());
                            changeColor.setText(MessageFormat.format(
                                Messages.ChangeColorManager_color_conflict,
                                newUser.getHumanReadableName()));
                            RGB newColor = changeColor.open();
                            if (newColor != null)
                                rgbOfNewParticipant = newColor;
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

                // Set color so that the different colors for selection and
                // contribution are displayed

                // FIXME if we do not update user colors HERE, different
                // highlighting
                // color will be only activated if user manually changes color
                // again.
                SarosAnnotation.setUserColor(user, rgb);
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
            if (i < 30)
                return false;
        }
        return true;
    }
}
