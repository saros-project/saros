package de.fu_berlin.inf.dpp.project.internal;

import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SessionView;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This manager is responsible for handling driver changes.
 * 
 * @author rdjemili
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class RoleManager implements IActivityProvider {

    private final List<IActivityListener> activityListeners = new LinkedList<IActivityListener>();

    private ISharedProject sharedProject;

    private ISharedProjectListener sharedProjectListener = new ISharedProjectListener() {

        public void roleChanged(User user, boolean replicated) {
            if (!replicated) {
                IActivity activity = new RoleActivity(Saros.getDefault()
                    .getMyJID().toString(), user.getJID().toString(), user
                    .getUserRole());
                for (IActivityListener listener : RoleManager.this.activityListeners) {
                    listener.activityCreated(activity);
                }
            }
            /*
             * Not nice to have GUI stuff here, but it can't be handled in
             * SessionView because it is not guaranteed there actually is a
             * session view open.
             */
            SessionView.showNotification("Role changed", String.format(
                "%s %s now %s of this session.", Util.getName(user), user
                    .isLocal() ? "are" : "is", user.isDriver() ? "a driver"
                    : "an observer"));
        }

        public void userJoined(User user) {
            SessionView.showNotification("User joined", Util.getName(user)
                + " joined the session.");
        }

        public void userLeft(User user) {
            SessionView.showNotification("User left", Util.getName(user)
                + " left the session.");
        }
    };

    public RoleManager(SessionManager sessionManager) {
        sessionManager.addSessionListener(sessionListener);
    }

    public final ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            sharedProject.addListener(sharedProjectListener);
            sharedProject.getActivityManager().addProvider(RoleManager.this);
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            assert sharedProject == project;
            sharedProject.removeListener(sharedProjectListener);
            sharedProject.getActivityManager().removeProvider(RoleManager.this);
            sharedProject = null;
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
        this.activityListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
        this.activityListeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(IActivity activity) {
        if (activity instanceof RoleActivity) {
            RoleActivity roleActivity = (RoleActivity) activity;
            User user = this.sharedProject.getParticipant(roleActivity
                .getAffectedUser());
            UserRole role = roleActivity.getRole();
            this.sharedProject.setUserRole(user, role, true);
        }
    }
}
